package net.rsprox.protocol.v240.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.zone.payload.ScriptedProjChange
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v240.game.outgoing.decoder.prot.GameServerProt

internal class ScriptedProjChangeDecoder : ProxyMessageDecoder<ScriptedProjChange> {
    override val prot: ClientProt = GameServerProt.SCRIPTEDPROJ_CHANGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ScriptedProjChange {
        val freezeDuration = buffer.g2Alt3()
        val deleteOnFreezeEnd = buffer.g1Alt1() == 1
        val targetOffsetX = buffer.g2sAlt2()
        val targetCoord = CoordGrid(buffer.g4())
        val targetOffsetZ = buffer.g2sAlt1()
        val targetHeight = buffer.g2sAlt3()
        val slot = buffer.g2Alt1()
        val targetIndex = buffer.g3s()
        return ScriptedProjChange(
            slot,
            targetCoord,
            targetOffsetX,
            targetOffsetZ,
            targetHeight,
            targetIndex,
            freezeDuration,
            deleteOnFreezeEnd,
        )
    }
}
