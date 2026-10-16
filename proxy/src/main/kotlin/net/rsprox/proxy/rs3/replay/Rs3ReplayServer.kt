package net.rsprox.proxy.rs3.replay

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.concurrent.GlobalEventExecutor
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.protocol.Prot
import net.rsprox.cache.rs3.Rs3ReplayGroupStore
import net.rsprox.proxy.replay.ReplayFrame
import net.rsprox.proxy.replay.ReplayPlaybackController
import net.rsprox.proxy.replay.ReplayPlaybackSnapshot
import net.rsprox.proxy.replay.ReplayPlaybackState
import net.rsprox.proxy.replay.ReplayScheduledTask
import net.rsprox.proxy.replay.ReplayScheduler
import net.rsprox.proxy.rs3.Rs3ProtocolRevision
import net.rsprox.proxy.rs3.login.Rs3LoginBlock
import net.rsprox.proxy.rs3.relay.Rs3EndpointRewriter
import net.rsprox.proxy.rs3.relay.Rs3LocalAddressSpace
import net.rsprox.proxy.rs3.relay.Rs3PacketStream
import net.rsprox.proxy.rs3.relay.Rs3RelayPorts
import net.rsprox.proxy.rs3.relay.Rs3WirePacket
import net.rsprox.proxy.rs3.relay.Rs3WorldListRewrite
import net.rsprox.proxy.rsa.rsa
import net.rsprox.shared.StreamDirection
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Local-only replay transport. There is deliberately no upstream game-server connection. */
public class Rs3ReplayServer(
    public val recording: Rs3ReplayRecording,
    private val privateKey: RSAPrivateCrtKeyParameters,
    public val host: String,
    public val ports: Rs3RelayPorts,
    private val cache: Rs3ReplayGroupStore,
    private val onFailure: (Throwable) -> Unit,
) : AutoCloseable {
    private val group = NioEventLoopGroup(1)
    private val channels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    private val closed = AtomicBoolean()
    private val logger = InlineLogger()
    private var lobbyChannel: Channel? = null

    @Volatile private var gameChannel: Channel? = null
    public val hasGameClient: Boolean get() = gameChannel?.isActive == true
    private var gameStarted = false

    private data class Rebuild(
        val tick: Int,
        val resume: Boolean,
    )

    @Volatile private var pendingRebuild: Rebuild? = null
    private var pendingRecordedReconnect: ByteArray? = null
    private var reconnectTimeout: ScheduledFuture<*>? = null
    public val isRebuilding: Boolean get() =
        pendingRebuild != null ||
            playbackSnapshot.state == ReplayPlaybackState.SEEKING

    private val scheduler =
        ReplayScheduler { delay, task ->
            val future = group.next().schedule({ task() }, delay, TimeUnit.MILLISECONDS)
            ReplayScheduledTask { future.cancel(false) }
        }
    private var lobbySink: ((ReplayFrame) -> Unit)? = null
    private var gameSink: ((ReplayFrame) -> Unit)? = null
    private val lobbyController = ReplayPlaybackController(recording.lobby, scheduler) { emit(lobbySink, it) }
    public val controller: ReplayPlaybackController =
        ReplayPlaybackController(
            recording.game,
            scheduler,
            // RS3's player/NPC deltas and scene transitions must all be applied in order.
            skipRedundantFastForwardFrames = false,
            fastForwardBatchSize = 256,
            // Apply every rebuild, but only wait for the final scene during a seek.
            waitForIntermediateMapBuilds = false,
            onReconnect = { emit(gameSink, it) },
        ) { emit(gameSink, it) }

    // GUI polling must not wait for the controller's lock while it sends a seek batch.
    @Volatile public var playbackSnapshot: ReplayPlaybackSnapshot = controller.snapshot()
        private set
    private val rewriter =
        Rs3EndpointRewriter(
            Rs3LocalAddressSpace(0),
            ports,
            { CompletableFuture.completedFuture(Unit) },
            replayAddress = { host },
        )

    init {
        require(InetAddress.getByName(host).isLoopbackAddress) { "Replay must bind to loopback" }
        group.next().scheduleAtFixedRate({ playbackSnapshot = controller.snapshot() }, 0, 100, TimeUnit.MILLISECONDS)
    }

    private fun isRebuildingNow(): Boolean =
        pendingRebuild != null || controller.snapshot().state == ReplayPlaybackState.SEEKING

    private fun control(action: () -> Unit) {
        if (!closed.get()) {
            group.next().execute {
                if (!closed.get()) {
                    try {
                        action()
                    } catch (error: Exception) {
                        onFailure(error)
                    }
                }
            }
        }
    }

    public fun play(): Unit =
        control {
            if (pendingRebuild == null && pendingRecordedReconnect == null) {
                if (controller.snapshot().state == ReplayPlaybackState.FINISHED) {
                    rebuild(0, true)
                } else {
                    controller.play()
                }
            }
        }

    public fun pause(): Unit = control { controller.pause() }

    public fun setSpeed(speed: Double): Unit = control { controller.setSpeed(speed) }

    public fun stopPlayback(): Unit = control { rebuild(0, false) }

    public fun stepForward(): Unit =
        control {
            if (!isRebuildingNow() && hasGameClient) controller.stepForwardTick()
        }

    public fun stepBack(): Unit = control { seek((controller.snapshot().currentTick - 1).coerceAtLeast(0), false) }

    public fun seekToTick(tick: Int): Unit =
        control {
            seek(tick, controller.snapshot().state == ReplayPlaybackState.PLAYING)
        }

    private fun seek(
        tick: Int,
        resume: Boolean,
    ) {
        if (isRebuildingNow() || !hasGameClient) return
        val snapshot = controller.snapshot()
        val target = tick.coerceIn(0, snapshot.totalTicks)
        if (target < snapshot.currentTick) {
            rebuild(target, resume)
        } else if (target > snapshot.currentTick) {
            controller.fastForwardToTick(target, resume)
        }
    }

    private fun rebuild(
        tick: Int,
        resume: Boolean,
    ) {
        if (isRebuildingNow() || !hasGameClient) return
        pendingRebuild = Rebuild(tick, resume)
        controller.beginSeeking()
        gameSink = null
        val previous = gameChannel
        gameChannel = null
        previous?.close()
        awaitReconnect()
    }

    private fun awaitReconnect() {
        reconnectTimeout?.cancel(false)
        reconnectTimeout =
            group.next().schedule({
                if (pendingRebuild != null || pendingRecordedReconnect != null) {
                    pendingRebuild = null
                    pendingRecordedReconnect = null
                    onFailure(IllegalStateException("The replay client did not reconnect"))
                    close()
                }
            }, 30, TimeUnit.SECONDS)
    }

    private fun emit(
        sink: ((ReplayFrame) -> Unit)?,
        frame: ReplayFrame,
    ) {
        try {
            sink?.invoke(frame)
        } catch (error: Exception) {
            onFailure(error)
            close()
        }
    }

    /** Call on a launch worker, never a Netty event loop. */
    public fun start() {
        try {
            for (port in listOf(ports.primary, ports.alternate)) {
                val bootstrap =
                    ServerBootstrap()
                        .group(group)
                        .channel(NioServerSocketChannel::class.java)
                        .childHandler(
                            object : ChannelInitializer<SocketChannel>() {
                                override fun initChannel(channel: SocketChannel) {
                                    channels.add(channel)
                                    channel.pipeline().addLast(Login())
                                    channel.pipeline().addLast(
                                        object : ChannelInboundHandlerAdapter() {
                                            override fun exceptionCaught(
                                                ctx: ChannelHandlerContext,
                                                cause: Throwable,
                                            ) {
                                                logger.warn(cause) { "RS3 replay connection failed" }
                                                onFailure(cause)
                                                ctx.close()
                                            }
                                        },
                                    )
                                }
                            },
                        )
                channels.add(bootstrap.bind(InetSocketAddress(host, port)).sync().channel())
            }
        } catch (error: Exception) {
            close()
            throw error
        }
    }

    private inner class Login : ByteToMessageDecoder() {
        private var handshake = -1
        private var loginType = -1
        private var length = -1
        private var awaitingContinue = false
        private var ciphers: StreamCipherPair? = null
        private var stream: Rs3PacketStream? = null
        private val worldLists = Rs3WorldListRewrite(rewriter)
        private var writes = CompletableFuture.completedFuture(Unit)
        private var keepalive: ScheduledFuture<*>? = null

        override fun decode(
            ctx: ChannelHandlerContext,
            input: ByteBuf,
            out: MutableList<Any>,
        ) {
            if (handshake == -1) {
                if (!input.isReadable) return
                handshake = input.readUnsignedByte().toInt()
                require(handshake == 14 || handshake == 15) { "Unsupported replay handshake $handshake" }
                if (handshake == 14) {
                    ctx.writeAndFlush(
                        ctx
                            .alloc()
                            .buffer(9)
                            .writeByte(0)
                            .writeLong(System.nanoTime()),
                    )
                }
            }
            if (handshake == 15) {
                if (length == -1) {
                    if (!input.isReadable) return
                    length = input.readUnsignedByte().toInt()
                    require(length >= 10) { "Truncated RS3 cache handshake" }
                }
                if (!input.isReadable(length)) return
                val body = input.readSlice(length)
                require(body.readInt() == Rs3ProtocolRevision(recording.header.revision).wireRevision)
                ctx.writeAndFlush(ctx.alloc().buffer(1).writeByte(0))
                ctx.pipeline().addAfter(ctx.name(), "rs3-replay-js5", Rs3ReplayJs5Handler(cache))
                ctx.pipeline().remove(this)
                return
            }
            if (awaitingContinue) {
                if (!input.isReadable) return
                require(input.readUnsignedByte().toInt() == 0x1A) { "Missing world-login continue acknowledgement" }
                awaitingContinue = false
                success(ctx, recording.gameSuccess)
                attachGame(ctx)
            }
            val packets = stream
            if (packets != null) {
                val bytes = ByteArray(input.readableBytes())
                input.readBytes(bytes)
                packets.accept(bytes) { packet ->
                    if (ctx.channel() == gameChannel && packet.entry.name == "MAP_BUILD_COMPLETE") {
                        controller.mapBuildComplete()
                    }
                    if (loginType == 19 && packet.entry.name == "WORLDLIST_FETCH") {
                        check(recording.lobbyWorldList.isNotEmpty()) { "Recording has no complete lobby world list" }
                        recording.lobbyWorldList.forEach { send(ctx, it, fromRecording = false) }
                    }
                }
                return
            }
            if (loginType == -1) {
                if (!input.isReadable(3)) return
                loginType = input.readUnsignedByte().toInt()
                length = input.readUnsignedShort()
                require(loginType == 19 || loginType == 16) { "Unsupported RS3 replay login" }
            }
            if (!input.isReadable(length)) return
            val body = input.readSlice(length)
            require(body.readInt() == Rs3ProtocolRevision(recording.header.revision).wireRevision)
            body.readInt() // Build subrevision is not the beta protocol key.
            if (loginType == 16) {
                val reconnect = body.readUnsignedByte().toInt()
                require(reconnect in 0..1) { "Invalid reconnect flag" }
                require(reconnect == 0 || pendingRebuild != null || pendingRecordedReconnect != null) {
                    "Unexpected replay reconnect"
                }
                require(pendingRecordedReconnect == null || reconnect == 1) { "Expected replay reconnect login" }
            }
            val rsa = body.readSlice(body.readUnsignedShort()).rsa(privateKey)
            try {
                ciphers = Rs3LoginBlock.decode(rsa).buildStreamCipherPair()
            } finally {
                rsa.release()
            }
            if (loginType == 19) {
                check(lobbyChannel == null && !gameStarted) { "Replay lobby already started" }
                lobbyChannel = ctx.channel()
                rewriter.lobbySuccess(recording.lobbySuccess).whenComplete { payload, error ->
                    ctx.executor().execute {
                        if (error != null) {
                            ctx.fireExceptionCaught(error)
                        } else {
                            success(ctx, payload)
                            attachPackets()
                            startKeepalive(ctx)
                            lobbySink = { send(ctx, it) }
                            lobbyController.play()
                        }
                    }
                }
            } else if (pendingRecordedReconnect != null) {
                val payload = checkNotNull(pendingRecordedReconnect)
                // Response 15 is login framing, not an ISAAC-encrypted game packet.
                ctx.writeAndFlush(
                    ctx
                        .alloc()
                        .buffer(3 + payload.size)
                        .writeByte(15)
                        .writeShort(payload.size)
                        .writeBytes(payload),
                )
                pendingRecordedReconnect = null
                attachGame(ctx, recordedReconnect = true)
            } else {
                check(
                    lobbyChannel != null && (!gameStarted || pendingRebuild != null),
                ) { "Game login requires replay lobby" }
                gameStarted = true
                // Finish the captured lobby baseline before the game initialization.
                lobbyController.fastForwardToTick(recording.lobby.totalTicks, false)
                val response = ctx.alloc().buffer().writeByte(2)
                recording.variableBlocks.forEach { response.writeShort(it.size).writeBytes(it) }
                awaitingContinue = true
                ctx.writeAndFlush(response)
            }
        }

        private fun success(
            ctx: ChannelHandlerContext,
            payload: ByteArray,
        ) {
            ctx.writeAndFlush(
                ctx
                    .alloc()
                    .buffer(2 + payload.size)
                    .writeByte(2)
                    .writeByte(payload.size)
                    .writeBytes(payload),
            )
        }

        private fun attachPackets() {
            stream = Rs3PacketStream(recording.decoder.clientProtTable, { checkNotNull(ciphers).encoderCipher }, false)
        }

        private fun attachGame(
            ctx: ChannelHandlerContext,
            recordedReconnect: Boolean = false,
        ) {
            gameChannel = ctx.channel()
            attachPackets()
            gameSink = { send(ctx, it) }
            val rebuild = pendingRebuild
            pendingRebuild = null
            reconnectTimeout?.cancel(false)
            reconnectTimeout = null
            startKeepalive(ctx)
            if (recordedReconnect) {
                controller.resumeAfterReconnect()
            } else if (rebuild == null) {
                controller.play()
            } else {
                controller.stop()
                // The first game tick includes the player table and initial scene required by login.
                controller.fastForwardToTick(maxOf(1, rebuild.tick), rebuild.resume)
            }
        }

        private fun startKeepalive(ctx: ChannelHandlerContext) {
            val ping =
                recording.decoder.gameServerProtProvider
                    .allProts()
                    .filterIsInstance<Prot>()
                    .single { it.toString() == "NO_TIMEOUT" }
            keepalive =
                ctx.executor().scheduleAtFixedRate({
                    if (ctx.channel().isActive) {
                        send(ctx, ReplayFrame(-1, 0, 0, 0, StreamDirection.SERVER_TO_CLIENT, ping, ByteArray(0)))
                    }
                }, 5, 5, TimeUnit.SECONDS)
        }

        private fun send(
            ctx: ChannelHandlerContext,
            frame: ReplayFrame,
            fromRecording: Boolean = true,
        ) {
            if (frame.direction != StreamDirection.SERVER_TO_CLIENT || !ctx.channel().isActive) return
            // An old timed reply cannot acknowledge a later Play/world-select request.
            // Serve complete replies on demand, without consuming ISAAC for skipped recorded chunks.
            if (loginType == 19 && fromRecording && frame.prot.toString() == "WORLDLIST_FETCH_REPLY") return
            // Playback must not open a recorded URL or navigate away to another live session.
            if (frame.prot.toString() in
                setOf("URL_OPEN", "SOCIAL_NETWORK_LOGOUT", "LOGOUT", "LOGOUT_FULL", "LOGOUT_TRANSFER", "CHANGE_LOBBY")
            ) {
                return
            }
            if (frame.prot.toString() == "RECONNECT") {
                check(ctx.channel() == gameChannel && pendingRecordedReconnect == null && pendingRebuild == null)
                require(frame.payload.size in 1..65535) { "Invalid recorded reconnect initialization length" }
                pendingRecordedReconnect = frame.payload.copyOf()
                gameChannel = null
                gameSink = null
                keepalive?.cancel(false)
                // Finish queued packet writes before closing the old transport.
                writes.whenComplete { _, error ->
                    ctx.executor().execute {
                        if (error != null) ctx.fireExceptionCaught(error)
                        ctx.close()
                    }
                }
                awaitReconnect()
                return
            }
            val cipher = checkNotNull(ciphers).decodeCipher
            val opcode = frame.prot.opcode
            val encrypted =
                if (opcode < 128) {
                    byteArrayOf((opcode + cipher.nextInt()).toByte())
                } else {
                    byteArrayOf(
                        ((opcode ushr 8) + 128 + cipher.nextInt()).toByte(),
                        (opcode + cipher.nextInt()).toByte(),
                    )
                }
            val payload = frame.payload.copyOf()
            val packet = Rs3WirePacket(opcode, recording.decoder.serverProtTable.getValue(opcode), encrypted, payload)
            writes =
                writes.thenCompose { worldLists.accept(packet) }.thenApplyAsync({ frames ->
                    if (ctx.channel().isActive) frames.forEach { ctx.write(Unpooled.wrappedBuffer(it)) }
                    ctx.flush()
                    Unit
                }, ctx.executor())
            writes.whenComplete { _, error -> if (error != null) ctx.fireExceptionCaught(error) }
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            keepalive?.cancel(false)
            stream?.close()
            if (ctx.channel() == gameChannel) {
                gameChannel = null
                gameSink = null
                controller.pause()
            }
            if (ctx.channel() == lobbyChannel) lobbyController.pause()
            super.channelInactive(ctx)
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        lobbyController.stop()
        reconnectTimeout?.cancel(false)
        pendingRebuild = null
        pendingRecordedReconnect = null
        controller.stop()
        channels.close()
        group.shutdownGracefully(0, 2, TimeUnit.SECONDS)
        cache.close()
    }
}
