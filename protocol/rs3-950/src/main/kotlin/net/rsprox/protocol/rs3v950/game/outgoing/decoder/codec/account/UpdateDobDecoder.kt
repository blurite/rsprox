package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.account.UpdateDob
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateDobDecoder : ProxyMessageDecoder<UpdateDob> {
    override val prot: ClientProt = GameServerProt.UPDATE_DOB

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateDob {
        val dateOfBirth = buffer.g3() shl 8 shr 8
        val verified = buffer.g1() == 1
        return UpdateDob(
            dateOfBirth,
            verified,
        )
    }
}
