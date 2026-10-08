package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.info.npcinfo.npcMorphVariables
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarpSmallDecoder : ProxyMessageDecoder<VarpSmall> {
    override val prot: ClientProt = GameServerProt.VARP_SMALL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpSmall {
        // Native sign-extends after the byte negation, including 0x80 -> -128.
        val value = buffer.g1Alt2().toByte().toInt()
        val id = buffer.g2Alt1()
        session.npcMorphVariables().set(id, value)
        return VarpSmall(
            id,
            value,
        )
    }
}
