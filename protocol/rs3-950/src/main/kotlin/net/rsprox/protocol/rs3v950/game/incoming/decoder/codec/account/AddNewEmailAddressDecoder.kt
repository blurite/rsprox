package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.account.AddNewEmailAddress
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class AddNewEmailAddressDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<AddNewEmailAddress> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AddNewEmailAddress {
        val email = buffer.readNativeString()
        val flags = buffer.g1()
        return AddNewEmailAddress(
            email,
            flags,
        )
    }
}
