package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.session.Session

internal class HintTrailDecoder : ProxyMessageDecoder<HintTrail> {
    override val prot: ClientProt = GameServerProt.HINT_TRAIL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HintTrail {
        val slot = buffer.g1()
        val modelId = buffer.g2()
        val trailingBytes = ByteArray(buffer.readableBytes()) { buffer.g1().toByte() }
        return HintTrail(
            slot,
            modelId,
            trailingBytes,
        )
    }
}
