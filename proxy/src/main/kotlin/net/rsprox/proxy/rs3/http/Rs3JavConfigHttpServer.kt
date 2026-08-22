package net.rsprox.proxy.rs3.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.util.CharsetUtil
import net.rsprox.proxy.rs3.config.Rs3JavConfig

public class Rs3JavConfigHttpServer(
    private val javConfigSupplier: () -> Rs3JavConfig,
) {
    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()

    public fun bind(port: Int): Channel {
        val bootstrap =
            ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(
                    object : ChannelInitializer<Channel>() {
                        override fun initChannel(ch: Channel) {
                            ch.pipeline().addLast(HttpRequestDecoder())
                            ch.pipeline().addLast(HttpResponseEncoder())
                            ch.pipeline().addLast(
                                JavConfigRequestHandler(javConfigSupplier),
                            )
                        }
                    },
                )
        return bootstrap.bind(port).sync().channel()
    }

    public fun shutdown() {
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }
}

private class JavConfigRequestHandler(
    private val javConfigSupplier: () -> Rs3JavConfig,
) : SimpleChannelInboundHandler<HttpObject>() {
    private lateinit var request: HttpRequest

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: HttpObject,
    ) {
        if (msg is HttpRequest) {
            this.request = msg
        }
        if (msg is LastHttpContent) {
            handleRequest(ctx, request)
        }
    }

    private fun handleRequest(
        ctx: ChannelHandlerContext,
        request: HttpRequest,
    ) {
        val decoder = QueryStringDecoder(request.uri())
        val keepAlive = HttpUtil.isKeepAlive(request)

        if (request.method() == HttpMethod.GET && decoder.path() == "/jav_config.ws") {
            val fresh =
                try {
                    javConfigSupplier()
                } catch (_: Exception) {
                    writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "", keepAlive)
                    return
                }
            writeResponse(ctx, HttpResponseStatus.OK, fresh.text, keepAlive)
            return
        }
        writeResponse(ctx, HttpResponseStatus.OK, "", keepAlive)
    }

    private fun writeResponse(
        ctx: ChannelHandlerContext,
        status: HttpResponseStatus,
        body: String,
        keepAlive: Boolean,
    ) {
        val content = Unpooled.copiedBuffer(body, CharsetUtil.ISO_8859_1)
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=ISO-8859-1")
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE)
        }
        val future = ctx.writeAndFlush(response)
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE)
        }
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        ctx.close()
    }
}
