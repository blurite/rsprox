package net.rsprox.proxy.replay

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprox.proxy.channel.setAutoRead
import net.rsprox.proxy.client.ClientGenericDecoder
import net.rsprox.proxy.client.prot.LoginClientProtProvider
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.io.IOException

public class ReplayClientLoginInitializer(
    private val rsa: RSAPrivateCrtKeyParameters,
    private val replaySession: ReplaySession,
) : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel) {
        ch.pipeline().addLast(
            ClientGenericDecoder(NopStreamCipher, LoginClientProtProvider),
            ReplayClientLoginHandler(rsa, replaySession),
        )
        ch.setAutoRead()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        if (cause is IOException) {
            return
        }
        ctx.close()
    }
}
