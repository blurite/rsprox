package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.account.ChangeEmailAddress
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ChangeEmailAddressDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ChangeEmailAddress> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChangeEmailAddress {
        val newEmail = buffer.readNativeString()
        val oldEmail = buffer.readNativeString()
        return ChangeEmailAddress(
            newEmail,
            oldEmail,
        )
    }
}
