package net.rsprox.gui.auth

import com.github.michaelbull.logging.InlineLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

public class AuthHttpServer {
    private var bootstrap: ServerBootstrap? = null
    public val isOnline: Boolean
        get() = bootstrap != null
    private var futures: MutableMap<Int, CompletableFuture<OAuth2Response>> = hashMapOf()

    public fun start() {
        check(bootstrap == null) { "Server is already running" }
        val bootstrap =
            ServerBootstrap().apply {
                group(NioEventLoopGroup(1), NioEventLoopGroup(1))
                channel(NioServerSocketChannel::class.java)
                childHandler(
                    object : ChannelInitializer<NioSocketChannel>() {
                        override fun initChannel(ch: NioSocketChannel) {
                            ch.pipeline().addLast(HttpServerCodec())
                            ch.pipeline().addLast(HttpObjectAggregator(2048))
                            ch.pipeline().addLast(LoggingHandler(LogLevel.INFO))
                            ch.pipeline().addLast(AuthHttpServerHandler())
                        }
                    },
                )
            }
        this.bootstrap = bootstrap
        bootstrap.bind(80).sync()

        bootstrap.config().childGroup().schedule({
            stop()
        }, 5, TimeUnit.MINUTES)
    }

    public fun stop() {
        val bootstrap = bootstrap ?: return

        bootstrap.config().group().shutdownGracefully()
        bootstrap.config().childGroup().shutdownGracefully()

        bootstrap
            .config()
            .group()
            .terminationFuture()
            .sync()
        bootstrap
            .config()
            .childGroup()
            .terminationFuture()
            .sync()

        this.bootstrap = null

        futures.values.forEach { it.completeExceptionally(IllegalStateException("Server stopped")) }
        futures.clear()
    }

    public fun waitForResponse(state: Int): CompletableFuture<OAuth2Response> {
        val future = CompletableFuture<OAuth2Response>()
        future.whenComplete { _, _ -> futures.remove(state) }
        futures[state] = future
        return future
    }

    private inner class AuthHttpServerHandler : SimpleChannelInboundHandler<FullHttpRequest>() {
        override fun channelRead0(
            ctx: ChannelHandlerContext,
            request: FullHttpRequest,
        ) {
            if (request.method() != HttpMethod.GET) {
                sendErrorResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED)
                return
            }
            val query = QueryStringDecoder(request.uri(), CharsetUtil.UTF_8)
            val params = query.parameters()
            val error = params["error"]?.firstOrNull()
            val description = params["error_description"]?.firstOrNull()
            val path = query.path()
            if (error != null) {
                logger.error { "Received error from OAuth2 server: $error: $description" }
                ctx.writeAndFlush(HttpResponseStatus.OK)
            } else if (path == "/") {
                val content =
                    createBasicHtmlPage(
                        """
                        const url = window.location.href;
                        if (url.includes("localhost/#")) {
                            window.location.href = url.replace("localhost/#", "localhost/capture?");
                        } else {
                            alert("Something went wrong");
                        }
                        """.trimIndent(),
                        "Redirecting..",
                    )
                sendHtmlResponse(ctx, content)
            } else if (path == "/capture") {
                val code = params["code"]?.firstOrNull()
                val idToken = params["id_token"]?.firstOrNull()
                val state = params["state"]?.firstOrNull()
                if (code == null || idToken == null || state == null) {
                    sendErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST)
                    return
                }
                val response = OAuth2Response(code, idToken)
                val future = futures[state.toInt()]
                future?.complete(response)
                sendHtmlResponse(ctx, createBasicHtmlPage("", "Everything is complete. You can close the window now."))
            } else {
                sendErrorResponse(ctx, HttpResponseStatus.NOT_FOUND)
            }
        }

        private fun sendErrorResponse(
            ctx: ChannelHandlerContext,
            status: HttpResponseStatus,
        ) {
            ctx.writeAndFlush(status)
        }
    }

    public data class OAuth2Response(
        val code: String,
        val idToken: String,
    )

    private companion object {
        private val logger = InlineLogger()

        private fun sendHtmlResponse(
            ctx: ChannelHandlerContext,
            content: String,
        ) {
            val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
            response.content().writeBytes(content.toByteArray(CharsetUtil.UTF_8))
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
            ctx.writeAndFlush(response).addListener { ctx.close() }
        }

        private fun createBasicHtmlPage(
            js: String,
            body: String,
        ): String {
            return "<html><head><script>$js</script></head><body>$body</body></html>"
        }
    }
}
