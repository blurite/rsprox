package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.session.Session

internal class HintArrowDecoder : ProxyMessageDecoder<HintArrow> {
    override val prot: ClientProt = GameServerProt.HINT_ARROW

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): HintArrow {
        val packed = buffer.g1()
        val slot = packed ushr 5
        val type = packed and 0x1F

        var targetIndex: Int? = null
        var x: Int? = null
        var y: Int? = null
        var z: Int? = null
        var distance: Int? = null
        if (type != 0) {
            targetIndex = buffer.g1()
            x = buffer.g2()
            y = buffer.g2()
            z = buffer.g1()
            distance = buffer.g2()
        }

        val trailingBytes = ByteArray(buffer.readableBytes()) { buffer.g1().toByte() }
        return HintArrow(
            slot,
            type,
            targetIndex,
            x,
            y,
            z,
            distance,
            trailingBytes,
        )
    }
}
