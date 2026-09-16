package net.rsprox.proxy.rs3.relay

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.resolver.dns.DnsNameResolverBuilder
import java.math.BigInteger
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3PlayerInfoInitPending
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.unknown.RawUnknownServerPacket
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.filters.UnmodifiablePropertyFilterSet
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.rs3.login.*
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder
import net.rsprox.proxy.rs3.Rs3DecoderLoader
import net.rsprox.proxy.rs3.transcriber.Rs3SessionMonitor
import net.rsprox.proxy.rs3.transcriber.Rs3TranscriberSession
import net.rsprox.proxy.rs3.transcriber.text.TextRs3TranscriberProvider
import net.rsprox.proxy.rsa.Rsa
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.NopSettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.text.MonitoredMessageConsumerContainer
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters

private class CipherHolder {
    @Volatile
    var pair: StreamCipherPair? = null
}

public class Rs3RelayServer(
    private val proxyPrivateKey: RSAPrivateCrtKeyParameters,
    private val sessionMonitor: Rs3SessionMonitor = Rs3SessionMonitor(),
    realServerModulusHex: String,
    private val revision: Int,
    private val resolveUpstream: () -> Pair<String, Int>,
    private val packetDefinitions: Rs3PacketDefinitions? = null,
    private val filterSetStore: PropertyFilterSetStore =
        DefaultPropertyFilterSetStore(Path.of("."), mutableListOf(UnmodifiablePropertyFilterSet())),
    private val settingSetStore: SettingSetStore =
        DefaultSettingSetStore(Path.of("."), mutableListOf(NopSettingSet)),
) {
    private val realServerPublicKey: RSAKeyParameters =
        RSAKeyParameters(false, BigInteger(realServerModulusHex, 16), Rsa.PUBLIC_EXPONENT)

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()
    private val discoveredWorldHost = AtomicReference<String?>(null)

    private val dnsResolver =
        DnsNameResolverBuilder(workerGroup.next())
            .channelType(io.netty.channel.socket.nio.NioDatagramChannel::class.java)
            .queryTimeoutMillis(5_000)
            .hostsFileEntriesResolver(
                object : io.netty.resolver.HostsFileEntriesResolver {
                    override fun address(
                        inetHost: String,
                        resolvedAddressTypes: io.netty.resolver.ResolvedAddressTypes,
                    ): java.net.InetAddress? = null
                },
            )
            .build()

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

        val cipherHolder = CipherHolder()
        val serverAttributes = AttributeMap()
        val clientAttributes = AttributeMap()
        Session(-1, serverAttributes).rs3AppearanceDefinitions = packetDefinitions
        Session(-1, serverAttributes).rs3PacketDefinitions = packetDefinitions
        Session(-1, clientAttributes).rs3PacketDefinitions = packetDefinitions
        var localPlayerIndex = -1

        val rs3Decoder =
            Rs3DecoderLoader.load(revision, HuffmanProvider.get()) { cipherHolder.pair?.decodeCipher }
        val clientDecoderService = rs3Decoder.clientPacketDecoder
        val serverDecoderService = rs3Decoder.serverPacketDecoder

        val monitoredContainer: MessageConsumerContainer =
            MonitoredMessageConsumerContainer(
                TextMessageConsumerContainer(emptyList()),
                sessionMonitor,
            )
        val transcriberSession: Rs3TranscriberSession =
            TextRs3TranscriberProvider().provide(
                container = monitoredContainer,
                filters = filterSetStore,
                settings = settingSetStore,
            )

        val serverToClientDecoder =
            Rs3ProtDecoder(
                table = rs3Decoder.serverProtTable,
                cipher = { cipherHolder.pair?.decodeCipher },
            ) { opcode, bytes ->
                val prot = rs3Decoder.gameServerProtProvider[opcode]
                val buffer = Unpooled.wrappedBuffer(bytes).toJagByteBuf()
                val session = Session(localPlayerIndex, serverAttributes)

                val message: IncomingMessage =
                    try {
                        serverDecoderService.decode(opcode, buffer, session)
                    } catch (exception: Exception) {
                        RawUnknownServerPacket(
                            opcode,
                            prot.toString(),
                            bytes,
                            "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}",
                        )
                    }

                transcriberSession.onServerPacket(prot, message)
            }

        val clientToServerDecoder =
            Rs3ProtDecoder(
                table = rs3Decoder.clientProtTable,
                supportsExtendedOpcodes = false,
                cipher = { cipherHolder.pair?.encoderCipher },
            ) { opcode, bytes ->
                val prot = rs3Decoder.gameClientProtProvider[opcode]
                val buffer = Unpooled.wrappedBuffer(bytes).toJagByteBuf()
                val session = Session(localPlayerIndex, clientAttributes)
                val message: IncomingMessage =
                    try {
                        clientDecoderService.decode(opcode, buffer, session)
                    } catch (exception: Exception) {
                        RawUnknownClientPacket(
                            opcode,
                            prot.toString(),
                            bytes,
                            "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}",
                        )
                    }
                transcriberSession.onClientProt(prot, message)
            }

        var skipNextClientToServerChunk = false
        var loginResponseFramer: Rs3LoginResponseFramer? = null
        var worldContinueAckSkipper: Rs3WorldContinueAckSkipper? = null

        val outboundBootstrap =
            Bootstrap()
                // Both directions share session/decoder state and must run on the same event loop.
                .group(clientChannel.eventLoop())
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .handler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(serverChannel: SocketChannel) {
                            serverChannel.pipeline().addLast(
                                RelayHandler(clientChannel) { bytes ->
                                    // todo: Just grabbing first world found for now
                                    scanForWorldHost(bytes)
                                    val framer = loginResponseFramer
                                    if (framer != null && !framer.isDone) {
                                        val leftover = framer.consume(bytes)
                                        if (framer.isDone && framer is Rs3WorldLoginResponseFramer) {
                                            val ownIndex = framer.ownIndex
                                            if (ownIndex != null) {
                                                localPlayerIndex = ownIndex
                                                transcriberSession.sessionState.localPlayerIndex = ownIndex
                                                Session(localPlayerIndex, serverAttributes).rs3PlayerInfoInitPending = true
                                            }
                                        }
                                        if (leftover != null && leftover.isNotEmpty()) {
                                            serverToClientDecoder.accept(leftover)
                                        }
                                    } else {
                                        serverToClientDecoder.accept(bytes)
                                    }
                                },
                            )
                        }
                    },
                )

        outboundBootstrap
            .connect(upstreamHost, upstreamPort)
            .addListener(
                ChannelFutureListener { future ->
                    if (!future.isSuccess) {
                        clientChannel.close()
                        return@ChannelFutureListener
                    }
                    val serverChannel = future.channel()
                    clientChannel.pipeline().addLast(
                        Rs3ClientLoginRsaSwapHandler(proxyPrivateKey, realServerPublicKey) { ciphers, _ ->
                            cipherHolder.pair = ciphers
                            skipNextClientToServerChunk = true
                            loginResponseFramer =
                                if (isWorldConnection) {
                                    Rs3WorldLoginResponseFramer()
                                } else {
                                    Rs3ServerLoginResponseFramer()
                                }
                            worldContinueAckSkipper = if (isWorldConnection) Rs3WorldContinueAckSkipper() else null
                        },
                    )
                    clientChannel.pipeline().addLast(
                        RelayHandler(serverChannel) { bytes ->
                            if (skipNextClientToServerChunk) {
                                skipNextClientToServerChunk = false
                            } else {
                                val ackSkipper = worldContinueAckSkipper
                                if (ackSkipper != null && !ackSkipper.isDone) {
                                    val leftover = ackSkipper.consume(bytes)
                                    if (leftover != null && leftover.isNotEmpty()) {
                                        clientToServerDecoder.accept(leftover)
                                    }
                                } else {
                                    clientToServerDecoder.accept(bytes)
                                }
                            }
                        },
                    )
                    clientChannel.config().isAutoRead = true
                    serverChannel.config().isAutoRead = true
                },
            )
    }

    private fun scanForWorldHost(bytes: ByteArray) {
        if (discoveredWorldHost.get() != null) return

        val text = String(bytes, Charsets.ISO_8859_1)
        val match = WORLD_HOST_REGEX.find(text) ?: return
        val host = match.value

        val resolveFuture: io.netty.util.concurrent.Future<java.net.InetAddress> = dnsResolver.resolve(host)
        resolveFuture.addListener { future ->
            if (!future.isSuccess) return@addListener
            discoveredWorldHost.set(resolveFuture.now.hostAddress)
        }
    }

    public fun shutdown() {
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }

    private companion object {
        private val WORLD_HOST_REGEX = Regex("""[a-zA-Z0-9-]+\.runescape\.com""")
    }
}

private class RelayHandler(
    private val destination: Channel,
    private val onChunk: ((ByteArray) -> Unit)?,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        if (msg is ByteBuf) {
            val fullBytes = ByteArray(msg.readableBytes())
            msg.getBytes(msg.readerIndex(), fullBytes)
            onChunk?.invoke(fullBytes)
        }

        if (destination.isActive) {
            destination.writeAndFlush(msg).addListener { future ->
                if (!future.isSuccess) {
                    ctx.channel().close()
                }
            }
        } else {
            (msg as? ByteBuf)?.release()
            ctx.channel().close()
        }
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        destination.config().isAutoRead = ctx.channel().isWritable
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        destination.close()
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        ctx.close()
    }
}
