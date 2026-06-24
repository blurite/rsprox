package net.rsprox.protocol.v239.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.zone.payload.ScriptedProjAdd
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class ScriptedProjAddDecoder : ProxyMessageDecoder<ScriptedProjAdd> {
    override val prot: ClientProt = GameServerProt.SCRIPTEDPROJ_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ScriptedProjAdd {
        val sourceOffsetX = buffer.g2sAlt1()
        val id = buffer.g2()
        val targetOffsetZ = buffer.g2s()
        val targetCoord = CoordGrid(buffer.g4Alt3())
        val startTime = buffer.g2Alt2()
        val sourceHeight = buffer.g2s()
        val curveScriptA = buffer.g2Alt1()
        val targetIndex = buffer.g3sAlt3()
        val slot = buffer.g2()
        val endTime = buffer.g2Alt1()
        val coordInZone = CoordInZone(buffer.g1())
        val targetOffsetX = buffer.g2s()
        val curveScriptH = buffer.g2Alt1()
        val sourceOffsetZ = buffer.g2sAlt3()
        val targetHeight = buffer.g2sAlt1()
        val sourceIndex = buffer.g3sAlt2()
        val curveScriptT = buffer.g2Alt1()
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
