package net.rsprox.protocol.v240.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.zone.payload.ScriptedProjAdd
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v240.game.outgoing.decoder.prot.GameServerProt

internal class ScriptedProjAddDecoder : ProxyMessageDecoder<ScriptedProjAdd> {
    override val prot: ClientProt = GameServerProt.SCRIPTEDPROJ_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ScriptedProjAdd {
        val targetIndex = buffer.g3sAlt3()
        val targetHeight = buffer.g2s()
        val endTime = buffer.g2Alt3()
        val slot = buffer.g2Alt3()
        val sourceOffsetZ = buffer.g2s()
        val sourceIndex = buffer.g3sAlt1()
        val curveScriptH = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        val sourceOffsetX = buffer.g2sAlt3()
        val sourceHeight = buffer.g2s()
        val coordInZone = CoordInZone(buffer.g1Alt1())
        val targetCoord = CoordGrid(buffer.g4Alt1())
        val startTime = buffer.g2()
        val targetOffsetZ = buffer.g2sAlt2()
        val targetOffsetX = buffer.g2s()
        val curveScriptT = buffer.g2Alt1()
        val curveScriptA = buffer.g2Alt3()
        return ScriptedProjAdd(
            slot,
            id,
            coordInZone.xInZone,
            coordInZone.zInZone,
            sourceOffsetX,
            sourceOffsetZ,
            sourceHeight,
            sourceIndex,
            targetCoord,
            targetOffsetX,
            targetOffsetZ,
            targetHeight,
            targetIndex,
            startTime,
            endTime,
            curveScriptH,
            curveScriptA,
            curveScriptT,
        )
    }
}
