package net.rsprox.proxy.client

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprox.protocol.ProtProvider

public class ClientGenericDecoder<out T : ClientProt>(
    private val cipher: StreamCipher,
    private val protProvider: ProtProvider<T>,
) : ByteToMessageDecoder() {
    private enum class State {
        READ_OPCODE,
        READ_LENGTH,
        READ_PAYLOAD,
    }

    private var state: State = State.READ_OPCODE
    private var cipherMod: Int = -1
    private var opcode: Int = -1
    private var length: Int = 0
    private lateinit var prot: T

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.cipherMod = cipher.nextInt()
            this.opcode = (input.g1() - cipherMod) and 0xFF
            this.prot = protProvider[opcode]
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
                ClientPacket(
                    this.prot,
                    this.cipherMod,
                    payload.retainedSlice(),
                )
            state = State.READ_OPCODE
        }
    }
}
