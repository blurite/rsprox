package net.rsprox.proxy.rs3.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.rsa.rsa
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters

public class Rs3ClientLoginRsaSwapHandler(
    private val proxyPrivateKey: RSAPrivateCrtKeyParameters,
    private val realServerPublicKey: RSAKeyParameters,
    private val onRecordingCiphers: (StreamCipherPair, Int, Int) -> Unit = { _, _, _ -> },
    private val onLoginMode: (Boolean) -> Unit = {},
    private val onCiphersEstablished: (real: StreamCipherPair, diagnosticCopy: StreamCipherPair) -> Unit,
) : ByteToMessageDecoder() {
    private enum class State {
        AWAITING_HANDSHAKE_BYTE,
        AWAITING_LOGIN_HEADER,
        AWAITING_LOGIN_PAYLOAD,
    }

    private var state: State = State.AWAITING_HANDSHAKE_BYTE
    private var loginType: Int = -1
    private var loginPayloadLength: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        when (state) {
            State.AWAITING_HANDSHAKE_BYTE -> {
                if (!input.isReadable) return
                val handshakeByte = input.readByte()
                out += Rs3LoginFrame(Unpooled.buffer(1).writeByte(handshakeByte.toInt()))

                if (handshakeByte.toInt() != LOGIN_HANDSHAKE_TYPE) {
                    ctx.pipeline().remove(this)
                    return
                }
                state = State.AWAITING_LOGIN_HEADER
            }

            State.AWAITING_LOGIN_HEADER -> {
                if (!input.isReadable(3)) return
                loginType = input.readUnsignedByte().toInt()
                loginPayloadLength = input.readUnsignedShort()
                state = State.AWAITING_LOGIN_PAYLOAD
            }

            State.AWAITING_LOGIN_PAYLOAD -> {
                if (!input.isReadable(loginPayloadLength)) return
                val payload = input.readSlice(loginPayloadLength).retain()
                try {
                    out += Rs3LoginFrame(handleLoginPacket(loginType, payload))
                } finally {
                    payload.release()
                }
                ctx.pipeline().remove(this)
            }
        }
    }

    private fun handleLoginPacket(
        type: Int,
        payload: ByteBuf,
    ): ByteBuf {
        require(type == LOBBY_LOGIN_TYPE || type == GAME_LOGIN_TYPE) {
            "Unsupported RS3 login type: $type"
        }
        // A block encrypted to the proxy cannot safely be forwarded to the real server.
        return swapRsaBlock(type, payload)
    }

    private fun swapRsaBlock(
        type: Int,
        payload: ByteBuf,
    ): ByteBuf {
        val buildMajor = payload.readInt()
        val buildMinor = payload.readInt()

        val reconnectFlag = if (type == GAME_LOGIN_TYPE) payload.readUnsignedByte().toInt() else 0
        require(reconnectFlag in 0..1) { "Invalid reconnect flag: $reconnectFlag" }

        val rsaSize = payload.readUnsignedShort()
        val rsaBlock = payload.readSlice(rsaSize)

        val plaintext = rsaBlock.rsa(proxyPrivateKey)
        val block =
            try {
                Rs3LoginBlock.decode(plaintext)
            } finally {
                plaintext.release()
            }

        val reEncryptedBlock = Rs3LoginBlock.encode(block)
        val reEncrypted =
            try {
                reEncryptedBlock.rsa(realServerPublicKey)
            } finally {
                reEncryptedBlock.release()
            }

        val newPayload = Unpooled.buffer()
        try {
            newPayload.writeInt(buildMajor)
            newPayload.writeInt(buildMinor)
            if (type == GAME_LOGIN_TYPE) {
                newPayload.writeByte(reconnectFlag)
            }
            newPayload.writeShort(reEncrypted.readableBytes())
            newPayload.writeBytes(reEncrypted)
            newPayload.writeBytes(payload)
            require(newPayload.readableBytes() <= 65535) { "Re-encrypted login exceeds its length field" }
            onLoginMode(reconnectFlag == 1)
            onCiphersEstablished(block.buildStreamCipherPair(), block.buildStreamCipherPair())
            onRecordingCiphers(block.buildStreamCipherPair(), buildMajor, buildMinor)
            return rebuild(type, newPayload)
        } finally {
            reEncrypted.release()
            newPayload.release()
        }
    }

    private fun rebuild(
        type: Int,
        payload: ByteBuf,
    ): ByteBuf {
        val out = Unpooled.buffer(3 + payload.readableBytes())
        out.writeByte(type)
        out.writeShort(payload.readableBytes())
        out.writeBytes(payload)
        return out
    }

    private companion object {
        private const val LOGIN_HANDSHAKE_TYPE = 14
        private const val LOBBY_LOGIN_TYPE = 19
        private const val GAME_LOGIN_TYPE = 16
    }
}
