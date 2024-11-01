package net.rsprox.proxy.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.CONTINUE
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.CharsetUtil
import net.rsprox.proxy.config.JavConfig
import net.rsprox.proxy.config.ProxyProperties
import net.rsprox.proxy.config.ProxyProperty
import net.rsprox.proxy.worlds.WorldListProvider
import java.io.IOException

public class HttpServerHandler(
    private val worldListProvider: WorldListProvider,
    private val javConfig: JavConfig,
    private val properties: ProxyProperties,
    private val gamePackProvider: GamePackProvider,
) : SimpleChannelInboundHandler<HttpObject>() {
    private lateinit var request: HttpRequest
    private val responseData: StringBuilder = StringBuilder()

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: HttpObject,
    ) {
        if (msg is HttpRequest) {
            this.request = msg
            val request = this.request
            if (HttpUtil.is100ContinueExpected(request)) {
                writeResponse(ctx)
            }
        }

        if (msg is LastHttpContent) {
            responseData.append(buildResponse(request))
            writeResponse(ctx, msg, responseData)
        }
    }

    private fun writeResponse(ctx: ChannelHandlerContext) {
        ctx
            .write(DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER))
            .addListener(ChannelFutureListener.CLOSE)
    }

    private fun writeResponse(
        ctx: ChannelHandlerContext,
        trailer: LastHttpContent,
        responseData: StringBuilder,
    ) {
        val keepAlive = HttpUtil.isKeepAlive(request)
        val httpResponse =
            DefaultFullHttpResponse(
                HTTP_1_1,
                if (trailer.decoderResult().isSuccess) OK else BAD_REQUEST,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.ISO_8859_1),
            )
        httpResponse.headers()[HttpHeaderNames.CONTENT_TYPE] = "text/plain; charset=ISO-8859-1"
        if (keepAlive) {
            httpResponse.headers().setInt(
                HttpHeaderNames.CONTENT_LENGTH,
                httpResponse.content().readableBytes(),
            )
            httpResponse.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        }
        ctx.write(httpResponse)
        if (!keepAlive) {
            ctx
                .writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        // Ignore IO exceptions
        if (cause is IOException) {
            return
        }
        logger.error(cause) {
            "Exception in HTTP server"
        }
    }

    private fun buildResponse(request: HttpRequest): StringBuilder {
        return when (val uri = request.uri()) {
            "/${properties.getProperty(ProxyProperty.WORLDLIST_ENDPOINT)}" -> {
                val response = StringBuilder()
                val worldListBuffer = worldListProvider.get().encode(ByteBufAllocator.DEFAULT)
                val bytes = ByteArray(worldListBuffer.readableBytes())
                worldListBuffer.gdata(bytes)
                response.append(String(bytes, Charsets.ISO_8859_1))
            }

            "/${properties.getProperty(ProxyProperty.JAV_CONFIG_ENDPOINT)}" -> {
                StringBuilder(javConfig.toString().encodeToByteArray().toString(Charsets.ISO_8859_1))
            }

            "/worlds.js" -> {
                val provider = worldListProvider.get()
                val runeLiteWorldList = provider.toRuneLiteWorldResult()
                val mapper = jacksonObjectMapper()
                val asString = mapper.writeValueAsString(runeLiteWorldList)
                StringBuilder().append(asString)
            }

            else -> {
                if (uri.startsWith("/gamepack_")) {
                    val builder = StringBuilder()
                    builder.append(gamePackProvider.get().toString(Charsets.ISO_8859_1))
                    return builder
                }
                throw IllegalStateException("Unknown HTTP request: $uri")
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
