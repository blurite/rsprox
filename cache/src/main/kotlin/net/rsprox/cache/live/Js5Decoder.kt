package net.rsprox.cache.live

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.g4
import kotlin.math.min

internal class Js5Decoder(
    private val downloader: Js5GroupDownloader,
) : ByteToMessageDecoder() {
    private enum class State {
        CONNECTING,
        RESPONSE_HEADER,
        RESPONSE_PAYLOAD,
    }

    private var state: State = State.CONNECTING
    private var archive: Int = -1
    private var group: Int = -1
    private var compression: Int = -1
    private var size: Int = -1
    private lateinit var response: ByteBuf

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.CONNECTING) {
            val response = input.g1()
            downloader.resumeConnection(response == 0)
            if (response != 0) {
                throw DecoderException("JS5 connection failed; received $response, expected 0.")
            }
            this.state = State.RESPONSE_HEADER
        }
        if (this.state == State.RESPONSE_HEADER) {
            if (!input.isReadable(8)) {
                return
            }
            this.archive = input.g1()
            this.group = input.g2()
            this.compression = input.g1()
            this.size = input.g4()

            val totalLen =
                if (this.compression == 0) {
                    this.size + 5
                } else {
                    this.size + 9
                }

            if (totalLen < 0) {
                throw DecoderException("Total length exceeds maximum ByteBuf size")
            }

            this.response = ctx.alloc().buffer(totalLen, totalLen)
            this.response.writeByte(this.compression)
            this.response.writeInt(this.size)
            this.state = State.RESPONSE_PAYLOAD
        }
        if (this.state == State.RESPONSE_PAYLOAD) {
            if (!input.isReadable) {
                return
            }
            while (this.response.isWritable) {
                val blockLen =
                    min(
                        511 - ((this.response.readableBytes() + 2) % 511),
                        this.response.writableBytes(),
                    )
                val last = this.response.writableBytes() <= blockLen

                val blockLenIncludingTrailer =
                    if (last) {
                        blockLen
                    } else {
                        blockLen + 1
                    }

                if (input.readableBytes() < blockLenIncludingTrailer) {
                    return
                }

                this.response.writeBytes(input, blockLen)

                if (!last && input.readUnsignedByte().toInt() != 0xFF) {
                    throw DecoderException("Invalid block trailer")
                }
            }
            this.downloader.groupResponse(this.archive, this.group, this.response)
            this.state = State.RESPONSE_HEADER
        }
    }
}
