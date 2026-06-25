package net.rsprox.proxy.replay

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.attributes.STREAM_CIPHER_PAIR
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.client.ClientGenericDecoder
import net.rsprox.proxy.client.ClientPacket
import net.rsprox.proxy.client.prot.LoginClientProt
import net.rsprox.proxy.rsa.rsa
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters

public class ReplayClientLoginHandler(
    private val rsa: RSAPrivateCrtKeyParameters,
    private val replaySession: ReplaySession,
) : SimpleChannelInboundHandler<ClientPacket<LoginClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        try {
            when (msg.prot) {
                LoginClientProt.INIT_GAME_CONNECTION -> sendGameConnectionResponse(ctx)
                LoginClientProt.INIT_JS5REMOTE_CONNECTION -> acceptJs5(ctx, msg)
                LoginClientProt.GAMELOGIN -> acceptLogin(ctx, msg)
                LoginClientProt.GAMERECONNECT -> acceptReconnect(ctx, msg)
                else -> {
                    // Web login side channels are not needed for packet replay.
                }
            }
        } finally {
            msg.payload.release()
        }
    }

    private fun sendGameConnectionResponse(ctx: ChannelHandlerContext) {
        val buffer = ctx.alloc().buffer().toJagByteBuf()
        buffer.p1(0)
        buffer.p8(System.nanoTime())
        ctx.writeAndFlush(buffer.buffer)
    }

    private fun acceptJs5(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        val input = msg.payload.toJagByteBuf()
        val revision = input.g4()
        check(revision == replaySession.timeline.header.revision) {
            "Replay capture revision ${replaySession.timeline.header.revision} does not match JS5 revision $revision"
        }
        val response = ctx.alloc().buffer(Byte.SIZE_BYTES)
        response.p1(0)
        ctx.writeAndFlush(response)
        val pipeline = ctx.pipeline()
        pipeline.remove<ClientGenericDecoder<*>>()
        pipeline.remove<ReplayClientLoginHandler>()
        pipeline.addLast(ReplayJs5RequestHandler(replaySession))
    }

    private fun acceptLogin(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        initializeGameCiphers(ctx, msg)
        sendLoginOk(ctx)
        switchToGameReplay(ctx)
        replaySession.attachClientChannel(ctx.channel())
    }

    private fun acceptReconnect(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        initializeGameCiphers(ctx, msg)
        if (replaySession.shouldTreatReconnectAsCleanLogin()) {
            sendLoginOk(ctx)
            switchToGameReplay(ctx)
            replaySession.attachClientChannel(ctx.channel())
            return
        }
        val reconnectBootstrap = replaySession.prepareReconnectBootstrap()
        if (reconnectBootstrap == null) {
            replaySession.prepareCleanLoginFromCurrentReconnect()
            sendLoginOk(ctx)
            switchToGameReplay(ctx)
            replaySession.attachClientChannel(ctx.channel())
            return
        }
        sendReconnectOk(ctx, reconnectBootstrap)
        switchToGameReplay(ctx)
        replaySession.attachReconnectClientChannel(ctx.channel())
    }

    private fun initializeGameCiphers(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        val input = msg.payload.toJagByteBuf()
        val revision = input.g4()
        check(revision == replaySession.timeline.header.revision) {
            "Replay capture revision ${replaySession.timeline.header.revision} does not match client revision $revision"
        }
        input.g4()
        if (revision >= 232) {
            input.g4()
        }
        input.g1()
        input.g1()
        input.g1()

        val originalRsaSize = input.g2()
        check(input.isReadable(originalRsaSize)) {
            "Replay login RSA block is truncated"
        }
        val rsaSlice = input.buffer.readSlice(originalRsaSize)
        val decryptedRsaBuffer = rsaSlice.rsa(rsa).toJagByteBuf()
        try {
            check(decryptedRsaBuffer.g1() == 1) {
                "Replay login RSA block marker is invalid"
            }
            val encodeSeed = IntArray(4) { decryptedRsaBuffer.g4() }
            val decodeSeed = IntArray(encodeSeed.size) { encodeSeed[it] + 50 }
            ctx.channel().attr(STREAM_CIPHER_PAIR).set(StreamCipherPair(IsaacRandom(encodeSeed), IsaacRandom(decodeSeed)))
        } finally {
            decryptedRsaBuffer.buffer.release()
        }
    }

    private fun sendLoginOk(ctx: ChannelHandlerContext) {
        val header = replaySession.timeline.header
        val response = Unpooled.buffer().toJagByteBuf()
        response.p1(2)
        response.p1(LOGIN_OK_LENGTH)
        response.p1(0)
        response.p4(0)
        response.p1(0)
        response.pboolean(false)
        response.p2(header.localPlayerIndex)
        response.pboolean(true)
        response.p8(0)
        response.p8(0)
        response.p8(0)
        ctx.writeAndFlush(response.buffer)
    }

    private fun sendReconnectOk(
        ctx: ChannelHandlerContext,
        payload: ByteArray,
    ) {
        val response = Unpooled.buffer().toJagByteBuf()
        response.p1(RECONNECT_OK)
        response.p2(payload.size)
        response.pdata(payload)
        ctx.writeAndFlush(response.buffer)
    }

    private fun switchToGameReplay(ctx: ChannelHandlerContext) {
        val pipeline = ctx.pipeline()
        pipeline.remove<ClientGenericDecoder<*>>()
        pipeline.remove<ReplayClientLoginHandler>()
        pipeline.addLast(
            ClientGenericDecoder(
                ctx.channel().attr(STREAM_CIPHER_PAIR).get().encoderCipher,
                replaySession.revisionDecoder.gameClientProtProvider,
            ),
        )
        pipeline.addLast(ReplayClientGameHandler(replaySession))
    }

    private companion object {
        /*
         * The client expects this length to include the 34-byte login-ok metadata
         * plus the first 3 bytes of the following game packet header.
         */
        private const val LOGIN_OK_LENGTH: Int = 37
        private const val RECONNECT_OK: Int = 15
    }
}
