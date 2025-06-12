package net.rsprox.proxy.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.Prot
import net.rsprox.protocol.ProtProvider

public class ServerGenericDecoder<out T : Prot>(
    private val cipher: StreamCipher,
    private val protProvider: ProtProvider<T>,
) : ByteToMessageDecoder() {
    private enum class State {
        READ_OPCODE_P1,
        READ_OPCODE_P2,
        READ_LENGTH,
        READ_PAYLOAD,
    }

    private var state: State = State.READ_OPCODE_P1
    private var cipherMod1: Int = -1
    private var cipherMod2: Int = -1
    private var opcode: Int = -1
    private var length: Int = 0
    private lateinit var prot: T

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.READ_OPCODE_P1) {
            if (!input.isReadable) {
                return
            }
            this.cipherMod1 = cipher.nextInt()
            this.cipherMod2 = 0
            this.opcode = (input.g1() - cipherMod1) and 0xFF
            if (this.opcode >= 128) {
                state = State.READ_OPCODE_P2
            } else {
                this.prot = protProvider[opcode]
                this.length = this.prot.size
                state =
                    if (this.length >= 0) {
                        State.READ_PAYLOAD
                    } else {
                        State.READ_LENGTH
                    }
            }
        }

        if (state == State.READ_OPCODE_P2) {
            if (!input.isReadable) {
                return
            }
            val opcodeP1 = this.opcode
            this.cipherMod2 = cipher.nextInt()
            this.opcode = ((opcodeP1 - 128) shl 8) + ((input.g1() - cipherMod2) and 0xFF)
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
            val payload = input.readBytes(length)
            out +=
                ServerPacket(
                    this.prot,
                    this.cipherMod1,
                    this.cipherMod2,
                    payload,
                )
            state = State.READ_OPCODE_P1
        }
    }
}
