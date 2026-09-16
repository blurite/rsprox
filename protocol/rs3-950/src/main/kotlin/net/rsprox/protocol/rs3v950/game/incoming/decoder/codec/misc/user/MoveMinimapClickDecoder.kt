package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.session.Session

internal class MoveMinimapClickDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MoveMinimapClick> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveMinimapClick {
        val z = buffer.g2()
        val controlKey = buffer.g1Alt1()
        val x = buffer.g2Alt3()
        // The normal sender emits ff ff 00 00 39 00 00 59. Preserve it instead of discarding it.
        val reservedMinimapMetadata = List(8) { buffer.g1() }
        val playerX = buffer.g2()
        val playerZ = buffer.g2()
        val sentinel = buffer.g1()
        return MoveMinimapClick(
            z,
            controlKey,
            x,
            reservedMinimapMetadata,
            playerX,
            playerZ,
            sentinel,
        )
    }
}
