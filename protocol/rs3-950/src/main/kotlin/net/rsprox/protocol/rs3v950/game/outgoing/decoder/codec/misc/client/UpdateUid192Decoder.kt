package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import java.util.zip.CRC32

internal class UpdateUid192Decoder : ProxyMessageDecoder<UpdateUid192> {
    override val prot: ClientProt = GameServerProt.UPDATE_UID192

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateUid192 {
        val uid = List(24) { buffer.g1() }
        val crc = buffer.g4()
        val valid = CRC32().apply { uid.forEach { update(it) } }.value.toInt() == crc
        return UpdateUid192(
            uid,
            crc,
            valid,
        )
    }
}
