package net.rsprox.proxy.client

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.protocol.Prot

public class ClientLoginDecoder : ByteToMessageDecoder() {
    private enum class State {
        READ_OPCODE,
        READ_LENGTH,
        READ_PAYLOAD,
    }

    private var state: State = State.READ_OPCODE
    private var opcode: Int = -1
    private var length: Int = 0
    private lateinit var prot: LoginClientProt

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.opcode = input.g1()
            this.prot = LoginClientProt.entries.first { it.opcode == this.opcode }
            this.length = this.prot.size
            state =
                if (this.length >= 0) {
                    State.READ_PAYLOAD
                } else {
                    State.READ_LENGTH
                }
        }

        if (state == State.READ_LENGTH) {
            when (length) {
                Prot.VAR_BYTE -> {
                    if (!input.isReadable(Byte.SIZE_BYTES)) {
                        return
                    }
                    this.length = input.g1()
                }

                Prot.VAR_SHORT -> {
                    if (!input.isReadable(Short.SIZE_BYTES)) {
                        return
                    }
                    this.length = input.g2()
                }

                else -> {
                    throw IllegalStateException("Invalid length: $length")
                }
            }
            state = State.READ_PAYLOAD
        }

        if (state == State.READ_PAYLOAD) {
            if (!input.isReadable(length)) {
                return
            }
            val payload = input.readSlice(length)
            out +=
                WrappedIncomingMessage(
                    this.prot,
                    payload.retainedSlice(),
                )
            state = State.READ_OPCODE
        }
    }
}
