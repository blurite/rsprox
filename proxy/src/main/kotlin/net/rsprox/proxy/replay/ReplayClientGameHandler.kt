package net.rsprox.proxy.replay

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprox.proxy.client.ClientPacket

public class ReplayClientGameHandler(
    private val replaySession: ReplaySession,
) : SimpleChannelInboundHandler<ClientPacket<*>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext?,
        msg: ClientPacket<*>,
    ) {
        if (msg.prot.isReplayMapBuildComplete()) {
            replaySession.mapBuildComplete()
        }
        msg.payload.release()
    }
}
