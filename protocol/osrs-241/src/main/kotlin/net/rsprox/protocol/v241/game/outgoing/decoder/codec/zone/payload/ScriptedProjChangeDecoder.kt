package net.rsprox.protocol.v241.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.zone.payload.ScriptedProjChange
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class ScriptedProjChangeDecoder : ProxyMessageDecoder<ScriptedProjChange> {
    override val prot: ClientProt = GameServerProt.SCRIPTEDPROJ_CHANGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ScriptedProjChange {
        val targetOffsetX = buffer.g2sAlt3()
        val targetIndex = buffer.g3sAlt3()
        val targetHeight = buffer.g2s()
        val deleteOnFreezeEnd = buffer.g1Alt2() == 1
        val slot = buffer.g2Alt1()
        val freezeDuration = buffer.g2Alt3()
        val targetOffsetZ = buffer.g2sAlt3()
        val targetCoord = CoordGrid(buffer.g4Alt2())
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
