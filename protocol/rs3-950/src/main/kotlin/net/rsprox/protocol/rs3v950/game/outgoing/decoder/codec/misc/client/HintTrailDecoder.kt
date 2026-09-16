package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class HintTrailDecoder : ProxyMessageDecoder<HintTrail> {
    override val prot: ClientProt = GameServerProt.HINT_TRAIL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HintTrail {
        val slot = buffer.g1()
        val modelId = buffer.gSmart2or4null()
        if (modelId == -1) return HintTrail(slot, modelId, null)
        val count = buffer.gSmart1or2s()
        val baseX = buffer.g2()
        val baseZ = buffer.g2()
        val points = List(count.coerceAtLeast(0)) { HintTrail.Point(buffer.g1s(), buffer.g1s()) }
        return HintTrail(slot, modelId, HintTrail.Trail(count, baseX, baseZ, points))
    }
}
