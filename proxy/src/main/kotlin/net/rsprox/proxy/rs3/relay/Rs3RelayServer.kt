package net.rsprox.proxy.rs3.relay

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.resolver.HostsFileEntriesResolver
import io.netty.resolver.ResolvedAddressTypes
import io.netty.resolver.dns.DnsNameResolverBuilder
import io.netty.util.concurrent.Future
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3PlayerInfoInitPending
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.filters.UnmodifiablePropertyFilterSet
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.rs3.Rs3DecoderLoader
import net.rsprox.proxy.rs3.Rs3SessionMonitor
import net.rsprox.proxy.rs3.binary.Rs3BinaryRecorder
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.proxy.rs3.login.Rs3ClientLoginRsaSwapHandler
import net.rsprox.proxy.rs3.login.Rs3LoginFrame
import net.rsprox.proxy.rs3.login.Rs3LoginSuccessFramer
import net.rsprox.proxy.rs3.login.Rs3ServerLoginResponseFramer
import net.rsprox.proxy.rs3.login.Rs3WorldContinueAckSkipper
import net.rsprox.proxy.rs3.login.Rs3WorldLoginResponseFramer
import net.rsprox.proxy.rs3.privacy.Rs3LivePacketDecoder
import net.rsprox.proxy.rs3.privacy.Rs3PacketSanitizer
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder
import net.rsprox.proxy.rsa.Rsa
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.NopSettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.rs3.Rs3TranscriberSession
import net.rsprox.transcriber.rs3.text.TextRs3TranscriberProvider
import net.rsprox.transcriber.text.MonitoredMessageConsumerContainer
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private class CipherHolder {
    @Volatile
    var pair: StreamCipherPair? = null
}

/** One logical login, surviving replacement sockets. Confined to this client's relay event loop. */
private class Rs3RelaySession(
    val world: Boolean,
    val endpoint: Int,
    val serverAttributes: AttributeMap,
    val clientAttributes: AttributeMap,
    val transcriber: Rs3TranscriberSession,
    val recording: Rs3ConnectionRecording,
    var channel: Channel,
) {
    var localPlayerIndex = -1
    var userId = -1L
    var userHash = -1L
    var name: String? = null
    var successful = false
    var ended = false
    var expiry: ScheduledFuture<*>? = null
    var previousChannel: Channel? = null
}

public class Rs3RelayServer(
    private val localPorts: Rs3RelayPorts,
    private val proxyPrivateKey: RSAPrivateCrtKeyParameters,
    private val sessionMonitor: Rs3SessionMonitor = Rs3SessionMonitor(),
    realServerModulusHex: String,
    private val revision: Int,
    private val resolveUpstream: () -> Pair<String, Int>,
    masterIndex: ByteArray = ByteArray(0),
    private val packetDefinitions: Rs3PacketDefinitions? = null,
    private val clientScripts: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider.EMPTY,
    private val filterSetStore: PropertyFilterSetStore =
        DefaultPropertyFilterSetStore(Path.of("."), mutableListOf(UnmodifiablePropertyFilterSet())),
    private val settingSetStore: SettingSetStore =
        DefaultSettingSetStore(Path.of("."), mutableListOf(NopSettingSet)),
) {
    private val realServerPublicKey: RSAKeyParameters =
        RSAKeyParameters(false, BigInteger(realServerModulusHex, 16), Rsa.PUBLIC_EXPONENT)

    private val bossGroup = NioEventLoopGroup(1)

    // All sockets for one launched client share mutable protocol state across reconnects.
    private val workerGroup = NioEventLoopGroup(1)
    private var gameSession: Rs3RelaySession? = null
    private val discoveredWorldHost = AtomicReference<String?>(null)
    private val mappedListeners = ConcurrentHashMap<InetSocketAddress, ChannelFuture>()
    private val mappedRoutes = AtomicReference<Map<InetSocketAddress, Rs3RelayRoute>>(emptyMap())
    private val huffman by lazy {
        HuffmanProvider.load()
        HuffmanProvider.get()
    }
    private var shuttingDown = false
    private val terminated = CompletableFuture<Unit>()
    private val recordings = Rs3BinaryRecorder(revision, masterIndex.copyOf())
    private val bandwidthUpdates =
        workerGroup.next().scheduleAtFixedRate(sessionMonitor::updateBandwidth, 1, 1, TimeUnit.SECONDS)

    init {
        packetDefinitions?.let(sessionMonitor::onPacketDefinitionsUpdate)
        sessionMonitor.onClientScriptsUpdate(clientScripts)
    }

    private val dnsResolver =
        DnsNameResolverBuilder(workerGroup.next())
            .channelType(NioDatagramChannel::class.java)
            .queryTimeoutMillis(5_000)
            .hostsFileEntriesResolver(
                object : HostsFileEntriesResolver {
                    override fun address(
                        inetHost: String,
                        resolvedAddressTypes: ResolvedAddressTypes,
                    ): InetAddress? = null
                },
            ).build()

    public fun bind(port: Int): Channel {
        HuffmanProvider.load()

        val bootstrap =
            ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(clientChannel: SocketChannel) {
                            connectUpstream(clientChannel, port)
                        }
                    },
                )
        return bootstrap.bind(port).sync().channel()
    }

    /**
     * Binds only this endpoint. No wildcard listener, hostname scan or next-world slot is
     * involved. A caller rewriting an endpoint must wait for the returned future to succeed.
     * This is separate from the legacy launcher until all handoff sources are rewritten.
     */
    @Synchronized
    public fun bind(route: Rs3RelayRoute): ChannelFuture {
        check(!shuttingDown) { "RS3 relay is shutting down" }
        check(!mappedListeners.containsKey(route.localAddress)) { "Route already bound: ${route.localAddress}" }
        val binding = bindMappedListener(route.localAddress)
        val published = binding.channel().newPromise()
        registerRoutes(listOf(route)).whenComplete { _, error ->
            if (error == null) published.setSuccess() else published.setFailure(error)
        }
        return published
    }

    /** Publish one immutable snapshot only once every required socket has bound successfully. */
    @Synchronized
    internal fun registerRoutes(routes: List<Rs3RelayRoute>): CompletableFuture<Unit> {
        check(!shuttingDown) { "RS3 relay is shutting down" }
        require(routes.all { it.localPort == localPorts.primary || it.localPort == localPorts.alternate }) {
            "Route does not belong to this RS3 client's port pair"
        }
        val unique = routes.associateBy { it.localAddress }
        require(
            routes.groupBy { it.localAddress }.values.all { entries ->
                val first = entries.first()
                entries.all {
                    it.upstreamHost == first.upstreamHost &&
                        it.upstreamPort == first.upstreamPort &&
                        it.endpoint == first.endpoint
                }
            },
        ) { "Conflicting routes in endpoint update" }
        val bindings =
            unique.keys.map { address ->
                val binding = mappedListeners[address] ?: bindMappedListener(address)
                val complete = CompletableFuture<Unit>()
                binding.addListener { result ->
                    if (result.isSuccess) {
                        complete.complete(Unit)
                    } else {
                        complete.completeExceptionally(
                            IllegalStateException("Unable to bind RS3 local relay at $address", result.cause()),
                        )
                    }
                }
                complete
            }
        return CompletableFuture.allOf(*bindings.toTypedArray()).thenApply {
            synchronized(this) {
                check(!shuttingDown && unique.keys.all { mappedListeners[it]?.channel()?.isActive == true }) {
                    "Listener closed before endpoint publication"
                }
                mappedRoutes.set(mappedRoutes.get() + unique)
            }
            Unit
        }
    }

    private fun bindMappedListener(address: InetSocketAddress): ChannelFuture {
        val future =
            ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(clientChannel: SocketChannel) {
                            val route = mappedRoutes.get()[address]
                            if (route == null) {
                                clientChannel.close()
                                return
                            }
                            logger.debug {
                                "Mapped RS3 ${route.endpoint}: ${route.localAddress} -> " +
                                    "${route.upstreamHost}:${route.upstreamPort}"
                            }
                            connectUpstream(
                                clientChannel,
                                route.upstreamHost,
                                route.upstreamPort,
                                route.endpoint is Rs3Endpoint.World,
                                mapped = true,
                                addresses = route.addressSpace,
                                endpoint = route.endpoint,
                            )
                        }
                    },
                ).bind(address)
        mappedListeners[address] = future
        future.channel().closeFuture().addListener {
            synchronized(this) {
                if (mappedListeners.remove(address, future)) mappedRoutes.set(mappedRoutes.get() - address)
            }
        }
        return future
    }

    private fun connectUpstream(
        clientChannel: SocketChannel,
        gamePort: Int,
    ) {
        val discovered = discoveredWorldHost.getAndSet(null)
        val isWorldConnection = discovered != null
        val (upstreamHost, upstreamPort) =
            if (discovered != null) {
                discovered to gamePort
            } else {
                try {
                    resolveUpstream()
                } catch (_: Exception) {
                    clientChannel.close()
                    return
                }
            }

        connectUpstream(clientChannel, upstreamHost, upstreamPort, isWorldConnection, mapped = false)
    }

    private fun connectUpstream(
        clientChannel: SocketChannel,
        upstreamHost: String,
        upstreamPort: Int,
        isWorldConnection: Boolean,
        mapped: Boolean,
        addresses: Rs3LocalAddressSpace? = null,
        endpoint: Rs3Endpoint? = null,
    ) {
        val cipherHolder = CipherHolder()
        val serverAttributes = AttributeMap()
        val clientAttributes = AttributeMap()
        Session(-1, serverAttributes).rs3AppearanceDefinitions = packetDefinitions
        Session(-1, serverAttributes).rs3PacketDefinitions = packetDefinitions
        Session(-1, clientAttributes).rs3PacketDefinitions = packetDefinitions

        val rs3Decoder =
            Rs3DecoderLoader.load(revision, huffman) {
                if (revision == 950) NopStreamCipher else cipherHolder.pair?.decodeCipher
            }
        val liveDecoder =
            Rs3LivePacketDecoder(rs3Decoder, Rs3PacketSanitizer(huffman)) { cipherHolder.pair?.decodeCipher }
        val initialRecording =
            Rs3ConnectionRecording(
                recordings,
                recordings.connection(
                    isWorldConnection,
                    endpoint?.id ?: -1,
                    upstreamHost,
                    upstreamPort,
                ),
                revision,
                rs3Decoder.serverProtTable,
                rs3Decoder.clientProtTable,
                huffman,
            )
        val serverDecoderService = rs3Decoder.serverPacketDecoder

        val monitoredContainer: MessageConsumerContainer =
            MonitoredMessageConsumerContainer(
                TextMessageConsumerContainer(emptyList()),
                sessionMonitor,
            )
        val transcriberSession: Rs3TranscriberSession =
            TextRs3TranscriberProvider(Rs3GamevalLookup).provide(
                container = monitoredContainer,
                filters = filterSetStore,
                settings = settingSetStore,
                clientScripts = clientScripts,
                isLobby = !isWorldConnection,
            )

        var state =
            Rs3RelaySession(
                isWorldConnection,
                endpoint?.id ?: -1,
                serverAttributes,
                clientAttributes,
                transcriberSession,
                initialRecording,
                clientChannel,
            )
        var reconnect = false
        var loginResponseFramer: Rs3LoginSuccessFramer? = null
        clientChannel.closeFuture().addListener {
            if (state.channel === clientChannel && !state.ended) {
                if (state.world && state.successful && !shuttingDown) {
                    suspendSession(state)
                } else {
                    finishSession(state)
                }
            }
            sessionMonitor.onConnectionClosed(clientChannel.id())
        }

        fun acceptPacketBytes(
            server: Boolean,
            bytes: ByteArray,
        ) {
            sessionMonitor.onBytes(clientChannel.id(), server, bytes.size)
            state.recording.accept(server, bytes)
        }

        val serverToClientDecoder =
            Rs3ProtDecoder(
                table = rs3Decoder.serverProtTable,
                cipher = { cipherHolder.pair?.decodeCipher },
            ) packet@{ opcode, bytes ->
                val prot = rs3Decoder.gameServerProtProvider[opcode]
                val session = Session(state.localPlayerIndex, state.serverAttributes)
                val message = liveDecoder.decode(true, opcode, bytes, session) ?: return@packet

                state.transcriber.onServerPacket(prot, message)
                val transcript = state.transcriber.sessionState
                transcript.getPlayerOrNull(transcript.localPlayerIndex)?.name?.let {
                    state.name = it
                    sessionMonitor.onConnectionNameUpdate(clientChannel.id(), it)
                }
                if (prot.toString() in SESSION_END_PACKETS) {
                    finishSession(state)
                }
            }

        val clientToServerDecoder =
            Rs3ProtDecoder(
                table = rs3Decoder.clientProtTable,
                supportsExtendedOpcodes = false,
                cipher = { cipherHolder.pair?.encoderCipher },
            ) packet@{ opcode, bytes ->
                val prot = rs3Decoder.gameClientProtProvider[opcode]
                val session = Session(state.localPlayerIndex, state.clientAttributes)
                val message = liveDecoder.decode(false, opcode, bytes, session) ?: return@packet
                state.transcriber.onClientProt(prot, message)
            }

        var worldContinueAckSkipper: Rs3WorldContinueAckSkipper? = null
        var transportCiphers: StreamCipherPair? = null
        val transportAcknowledgement = if (mapped && isWorldConnection) Rs3WorldContinueAckSkipper() else null
        val transportDecoder =
            if (mapped) {
                check(revision == 950) { "Mapped routing is currently verified only for revision 950" }
                Rs3DecoderLoader.load(revision, huffman) { transportCiphers?.decodeCipher }
            } else {
                null
            }
        var mappedServer: Rs3MappedServerHandler? = null

        fun createMappedServer(
            serverChannel: Channel,
            relay: RelayHandler,
        ): Rs3MappedServerHandler {
            val rewriter =
                Rs3EndpointRewriter(
                    checkNotNull(addresses),
                    localPorts,
                    ::registerRoutes,
                    recordings::worldDefinitions,
                )
            val stream =
                Rs3PacketStream(rs3Decoder.serverProtTable, { checkNotNull(transportCiphers).decodeCipher }, true) {
                    entry, payload ->
                    // These payloads draw from the opcode ISAAC too. Decode on a private transport copy.
                    if (entry.name == "URL_OPEN" || entry.name == "SOCIAL_NETWORK_LOGOUT") {
                        val opcode =
                            rs3Decoder.serverProtTable.entries
                                .single { it.value.name == entry.name }
                                .key
                        val buffer = Unpooled.wrappedBuffer(payload)
                        try {
                            checkNotNull(transportDecoder).serverPacketDecoder.decode(
                                opcode,
                                buffer.toJagByteBuf(),
                                Session(-1, AttributeMap()),
                            )
                            check(!buffer.isReadable) { "Unconsumed cipher-bearing payload" }
                        } finally {
                            buffer.release()
                        }
                    }
                }
            return Rs3MappedServerHandler(
                isWorldConnection,
                rewriter,
                stream,
                { checkNotNull(transportCiphers).decodeCipher },
                { relay.forward(serverChannel, it) },
                relay::closeAfterFlush,
            ) {
                checkNotNull(transportAcknowledgement).expect()
            }
        }

        // The server pipeline may never be created if upstream DNS/connect fails.
        clientChannel.closeFuture().addListener { mappedServer?.dispose() }
        val outboundBootstrap =
            Bootstrap()
                // Both directions share session/decoder state and must run on the same event loop.
                .group(clientChannel.eventLoop())
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .handler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(serverChannel: SocketChannel) {
                            val relay =
                                RelayHandler(clientChannel) response@{ bytes ->
                                    if (state.channel !== clientChannel || state.ended) return@response
                                    // todo: Just grabbing first world found for now
                                    if (!mapped) scanForWorldHost(bytes)
                                    val framer = loginResponseFramer
                                    if (framer != null && !framer.isDone) {
                                        val leftover = framer.consume(bytes)
                                        if (framer.isDone && framer.isSuccessful) {
                                            if (reconnect) {
                                                val payload = checkNotNull(framer.reconnectData)
                                                val buffer = Unpooled.wrappedBuffer(payload)
                                                try {
                                                    val message =
                                                        serverDecoderService.decode(
                                                            0xFF,
                                                            buffer.toJagByteBuf(),
                                                            Session(state.localPlayerIndex, state.serverAttributes),
                                                        )
                                                    state.recording.reconnect(payload)
                                                    state.transcriber.onServerPacket(
                                                        rs3Decoder.gameServerProtProvider[0xFF],
                                                        message,
                                                    )
                                                } finally {
                                                    buffer.release()
                                                }
                                                state.expiry?.cancel(false)
                                                state.expiry = null
                                                state.previousChannel?.close()
                                                state.previousChannel = null
                                                logger.info { "RS3 reconnected to world ${state.endpoint}" }
                                            } else {
                                                gameSession
                                                    ?.takeIf {
                                                        it !== state &&
                                                            (isWorldConnection || !it.channel.isActive)
                                                    }?.let(::finishSession)
                                                state.recording.login(framer)
                                                state.userId = framer.userId ?: -1
                                                state.userHash = framer.userHash ?: -1
                                                state.name = (framer as? Rs3ServerLoginResponseFramer)?.displayName
                                                state.successful = true
                                                if (isWorldConnection) gameSession = state
                                            }
                                            sessionMonitor.onConnectionLogin(
                                                clientChannel.id(),
                                                isWorldConnection,
                                                endpoint?.id ?: -1,
                                                state.name,
                                                state.userId,
                                                state.userHash,
                                            )
                                            repeat(framer.initialCipherDraws) {
                                                checkNotNull(cipherHolder.pair).decodeCipher.nextInt()
                                            }
                                        }
                                        if (framer.isSuccessful && framer is Rs3WorldLoginResponseFramer) {
                                            val ownIndex = framer.ownIndex
                                            if (ownIndex != null) {
                                                state.localPlayerIndex = ownIndex
                                                sessionMonitor.onGameLogin(endpoint?.id ?: -1, ownIndex)
                                                state.transcriber.sessionState.localPlayerIndex = ownIndex
                                                Session(ownIndex, state.serverAttributes).rs3PlayerInfoInitPending =
                                                    true
                                            }
                                        }
                                        if (leftover != null && leftover.isNotEmpty()) {
                                            acceptPacketBytes(true, leftover)
                                            serverToClientDecoder.accept(leftover)
                                        }
                                    } else if (framer?.isSuccessful == true) {
                                        acceptPacketBytes(true, bytes)
                                        serverToClientDecoder.accept(bytes)
                                    }
                                }
                            if (mapped) {
                                val handler = createMappedServer(serverChannel, relay)
                                mappedServer = handler
                                serverChannel.pipeline().addLast(handler)
                            }
                            serverChannel.pipeline().addLast(relay)
                        }
                    },
                )

        val onConnected =
            ChannelFutureListener { future ->
                if (!future.isSuccess) {
                    logger.warn(future.cause()) { "RS3 upstream connection failed: $upstreamHost:$upstreamPort" }
                    clientChannel.close()
                    return@ChannelFutureListener
                }
                val serverChannel = future.channel()
                if (!clientChannel.isActive) {
                    serverChannel.close()
                    return@ChannelFutureListener
                }
                clientChannel.pipeline().addLast(
                    Rs3ClientLoginRsaSwapHandler(
                        proxyPrivateKey,
                        realServerPublicKey,
                        { pair, major, minor -> state.recording.begin(pair, major, minor) },
                        { resuming ->
                            reconnect = resuming
                            if (resuming) {
                                check(isWorldConnection && revision == 950) { "Unsupported reconnect target" }
                                val previous = checkNotNull(gameSession) { "Reconnect has no previous game session" }
                                check(
                                    !previous.ended &&
                                        previous.endpoint == state.endpoint &&
                                        previous.localPlayerIndex in 1..2047,
                                ) {
                                    "Reconnect does not match the previous world session"
                                }
                                initialRecording.close()
                                val oldChannel = previous.channel
                                state = previous
                                state.channel = clientChannel
                                suspendSession(state)
                                // Native clientdrop keeps the old socket until the new login is accepted.
                                if (oldChannel.isActive && state.previousChannel?.isActive != true) {
                                    state.previousChannel = oldChannel
                                } else {
                                    oldChannel.close()
                                }
                                transportAcknowledgement?.skipForReconnect()
                            }
                        },
                    ) { ciphers, diagnostic ->
                        transportCiphers = ciphers
                        cipherHolder.pair = diagnostic
                        mappedServer?.beginLogin(reconnect)
                        val acknowledgement =
                            if (isWorldConnection && !reconnect) Rs3WorldContinueAckSkipper() else null
                        worldContinueAckSkipper = acknowledgement
                        loginResponseFramer =
                            if (isWorldConnection) {
                                Rs3WorldLoginResponseFramer(retainVariables = true, reconnect = reconnect) {
                                    checkNotNull(acknowledgement).expect()
                                }
                            } else {
                                Rs3ServerLoginResponseFramer()
                            }
                    },
                )
                if (mapped) {
                    val stream =
                        Rs3PacketStream(
                            rs3Decoder.clientProtTable,
                            { checkNotNull(transportCiphers).encoderCipher },
                            false,
                        )
                    clientChannel.pipeline().addLast(Rs3MappedClientHandler(stream, transportAcknowledgement))
                }
                val requestRelay =
                    RelayHandler(serverChannel) request@{ bytes ->
                        if (state.channel !== clientChannel || state.ended) return@request
                        val ackSkipper = worldContinueAckSkipper
                        if (ackSkipper != null && !ackSkipper.isDone) {
                            val leftover = ackSkipper.consume(bytes)
                            if (leftover != null && leftover.isNotEmpty()) {
                                acceptPacketBytes(false, leftover)
                                clientToServerDecoder.accept(leftover)
                            }
                        } else {
                            acceptPacketBytes(false, bytes)
                            clientToServerDecoder.accept(bytes)
                        }
                    }
                clientChannel.pipeline().addLast(requestRelay)
                clientChannel.config().isAutoRead = true
                serverChannel.config().isAutoRead = true
            }
        if (mapped) {
            // Resolve the real host without consulting per-world hosts-file overrides.
            val resolution = dnsResolver.resolve(upstreamHost)
            resolution.addListener { result ->
                if (!result.isSuccess) {
                    logger.warn(result.cause()) { "RS3 upstream DNS failed: $upstreamHost" }
                    clientChannel.close()
                    return@addListener
                }
                val address = resolution.now
                if (address.isLoopbackAddress || address.isAnyLocalAddress) {
                    clientChannel.close()
                } else if (clientChannel.isActive) {
                    outboundBootstrap.connect(address, upstreamPort).addListener(onConnected)
                }
            }
        } else {
            outboundBootstrap.connect(upstreamHost, upstreamPort).addListener(onConnected)
        }
    }

    private fun scanForWorldHost(bytes: ByteArray) {
        if (discoveredWorldHost.get() != null) return

        val text = String(bytes, Charsets.ISO_8859_1)
        val match = WORLD_HOST_REGEX.find(text) ?: return
        val host = match.value

        val resolveFuture: Future<InetAddress> = dnsResolver.resolve(host)
        resolveFuture.addListener { future ->
            if (!future.isSuccess) return@addListener
            discoveredWorldHost.set(resolveFuture.now.hostAddress)
        }
    }

    private fun suspendSession(session: Rs3RelaySession) {
        session.recording.suspend()
        if (session.expiry == null) {
            session.expiry = session.channel.eventLoop().schedule({ finishSession(session) }, 90, TimeUnit.SECONDS)
        }
    }

    private fun finishSession(session: Rs3RelaySession) {
        if (session.ended) return
        session.ended = true
        session.previousChannel?.close()
        session.previousChannel = null
        session.expiry?.cancel(false)
        session.expiry = null
        session.recording.close()
        if (gameSession === session) gameSession = null
    }

    @Synchronized
    public fun shutdown(): CompletableFuture<Unit> {
        if (shuttingDown) return terminated
        shuttingDown = true
        bandwidthUpdates.cancel(false)
        sessionMonitor.onLogout(Unit)
        mappedListeners.values.forEach { it.channel().close() }
        mappedListeners.clear()
        mappedRoutes.set(emptyMap())
        dnsResolver.close()
        val workers = workerGroup.shutdownGracefully()
        val bosses = bossGroup.shutdownGracefully()
        workers.addListener {
            bosses.addListener {
                sessionMonitor.onLogout(Unit)
                recordings.shutdown().whenComplete { _, error ->
                    if (error == null) terminated.complete(Unit) else terminated.completeExceptionally(error)
                }
            }
        }
        return terminated
    }

    private companion object {
        private val logger = InlineLogger()
        private val SESSION_END_PACKETS = setOf("LOGOUT", "LOGOUT_FULL", "LOGOUT_TRANSFER")
        private val WORLD_HOST_REGEX = Regex("""[a-zA-Z0-9-]+\.runescape\.com""")
    }
}

internal class RelayHandler(
    private val destination: Channel,
    private val onChunk: ((ByteArray) -> Unit)?,
) : ChannelInboundHandlerAdapter() {
    private var finishing = false

    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        forward(ctx.channel(), msg)
    }

    fun forward(
        source: Channel,
        msg: Any,
    ) {
        // Login framing is explicit, independent of TCP fragmentation/coalescing.
        if (msg is Rs3LoginFrame) {
            val content = msg.content().retain()
            msg.release()
            write(source, content)
            return
        }
        if (msg is ByteBuf) {
            try {
                val fullBytes = ByteArray(msg.readableBytes())
                msg.getBytes(msg.readerIndex(), fullBytes)
                onChunk?.invoke(fullBytes)
            } catch (exception: Exception) {
                msg.release()
                throw exception
            }
        }

        write(source, msg)
    }

    private fun write(
        source: Channel,
        msg: Any,
    ) {
        if (destination.isActive && !finishing) {
            destination.writeAndFlush(msg).addListener { future ->
                if (!future.isSuccess) {
                    source.close()
                    destination.close()
                }
            }
        } else {
            (msg as? ByteBuf)?.release()
            source.close()
        }
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        destination.config().isAutoRead = ctx.channel().isWritable
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        closeAfterFlush()
    }

    fun closeAfterFlush() {
        if (finishing) return
        finishing = true
        if (!destination.isActive) return
        // This barrier completes only after prior writes have reached the socket.
        // A stalled peer must not hold the paired connection open indefinitely.
        val timeout = destination.eventLoop().schedule({ destination.close() }, 5, TimeUnit.SECONDS)
        destination.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener {
            timeout.cancel(false)
            destination.close()
        }
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        logger.warn(cause) { "RS3 relay failed on ${ctx.channel().localAddress()}" }
        ctx.close()
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
