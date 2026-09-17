package net.rsprox.proxy.rs3.relay

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprox.proxy.rs3.login.Rs3LoginSuccessFramer
import net.rsprox.proxy.rs3.login.Rs3ServerLoginResponseFramer
import net.rsprox.proxy.rs3.login.Rs3WorldLoginResponseFramer
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Ordered, bounded asynchronous endpoint rewriting; never waits on a bind from a Netty thread. */
internal class Rs3MappedServerHandler(
    private val world: Boolean,
    private val rewriter: Rs3EndpointRewriter,
    private val stream: Rs3PacketStream,
    private val cipher: () -> StreamCipher,
    private val forward: (ByteBuf) -> Unit,
    private val finish: () -> Unit,
    onVariablesComplete: () -> Unit,
) : ChannelInboundHandlerAdapter() {
    private val login: Rs3LoginSuccessFramer =
        if (world) {
            Rs3WorldLoginResponseFramer(onVariablesComplete = onVariablesComplete)
        } else {
            Rs3ServerLoginResponseFramer()
        }
    private val loginBytes = ByteArrayOutputStream()
    private val worldLists = Rs3WorldListRewrite(rewriter)
    private var started = false
    private var disposed = false
    private var draining = false
    private var terminalError: Throwable? = null
    private var tail = CompletableFuture.completedFuture(Unit)
    private var queuedBytes = 0
    private var worldListTimeout: ScheduledFuture<*>? = null

    fun beginLogin() {
        check(!started) { "Duplicate login on mapped connection" }
        started = true
    }

    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any,
    ) {
        if (msg !is ByteBuf) {
            ctx.fireChannelRead(msg)
            return
        }
        if (disposed || draining) {
            msg.release()
            return
        }
        if (!started) {
            forward(msg)
            return
        }
        val bytes = ByteArray(msg.readableBytes())
        try {
            msg.readBytes(bytes)
        } finally {
            msg.release()
        }
        queuedBytes += bytes.size
        require(queuedBytes <= 2 * 1024 * 1024) { "Mapped relay queue limit exceeded" }
        tail =
            tail
                .thenComposeAsync({
                    check(!disposed) { "Mapped destination closed" }
                    process(ctx, bytes)
                }, ctx.executor())
                .thenApplyAsync({ frames ->
                    queuedBytes -= bytes.size
                    if (!disposed) frames.forEach { forward(Unpooled.wrappedBuffer(it)) }
                    Unit
                }, ctx.executor())
                .orTimeout(20, TimeUnit.SECONDS)
        tail.whenComplete { _, error ->
            if (error != null) {
                ctx.executor().execute {
                    if (!disposed && !draining) exceptionCaught(ctx, error)
                }
            }
        }
    }

    private fun process(
        ctx: ChannelHandlerContext,
        bytes: ByteArray,
    ): CompletableFuture<List<ByteArray>> {
        var packetBytes = bytes
        var output = CompletableFuture.completedFuture(emptyList<ByteArray>())
        if (!login.isDone) {
            val leftover = login.consume(bytes)
            val prefix = bytes.copyOfRange(0, bytes.size - (leftover?.size ?: 0))
            if (world) {
                output = CompletableFuture.completedFuture(listOf(prefix))
            } else {
                loginBytes.write(prefix)
                require(loginBytes.size() <= 257) { "Lobby login exceeds its length bound" }
                if (login.isDone) {
                    output =
                        if (login.isSuccessful) {
                            rewriter.lobbySuccess(checkNotNull(login.loginData)).thenApply { body ->
                                listOf(byteArrayOf(2, body.size.toByte()) + body)
                            }
                        } else {
                            CompletableFuture.completedFuture(listOf(loginBytes.toByteArray()))
                        }
                    loginBytes.reset()
                }
            }
            if (login.isSuccessful) repeat(login.initialCipherDraws) { cipher().nextInt() }
            packetBytes = leftover ?: ByteArray(0)
        } else if (!login.isSuccessful) {
            return CompletableFuture.completedFuture(listOf(bytes))
        }
        if (packetBytes.isNotEmpty()) {
            val packets = ArrayList<Rs3WirePacket>()
            stream.accept(packetBytes, packets::add)
            for (packet in packets) {
                output =
                    output.thenComposeAsync({ earlier ->
                        check(!disposed) { "Mapped destination closed" }
                        val result = worldLists.accept(packet)
                        if (worldLists.isPending && worldListTimeout == null) {
                            worldListTimeout =
                                ctx.executor().schedule({
                                    exceptionCaught(
                                        ctx,
                                        IllegalStateException("Incomplete world-list rewrite timed out"),
                                    )
                                }, 20, TimeUnit.SECONDS)
                        } else if (!worldLists.isPending) {
                            worldListTimeout?.cancel(false)
                            worldListTimeout = null
                        }
                        result.thenApply { earlier + it }
                    }, ctx.executor())
            }
        }
        return output
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        drain(ctx)
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        if (terminalError == null) terminalError = cause
        ctx.close()
        drain(ctx)
    }

    private fun drain(ctx: ChannelHandlerContext) {
        if (draining || disposed) return
        draining = true
        // A closed upstream can still have complete packets queued for rewriting.
        // Its pipeline may be destroyed before these callbacks run, so forwarding
        // uses an explicit sink rather than firing events through that pipeline.
        tail.whenCompleteAsync({ _, error ->
            if (!disposed) {
                val framingFailure =
                    runCatching {
                        check(!started || login.isDone) { "Upstream closed during login response" }
                        stream.finishInput()
                        check(!worldLists.isPending) { "Upstream closed during a world list" }
                    }.exceptionOrNull()
                val failure = error ?: terminalError ?: framingFailure
                if (failure != null && framingFailure != null && failure !== framingFailure) {
                    failure.addSuppressed(framingFailure)
                }
                if (failure != null) logger.warn(failure) { "RS3 upstream ended on ${ctx.channel().localAddress()}" }
                dispose()
                finish()
            }
        }, ctx.executor())
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        worldListTimeout?.cancel(false)
        stream.close()
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
