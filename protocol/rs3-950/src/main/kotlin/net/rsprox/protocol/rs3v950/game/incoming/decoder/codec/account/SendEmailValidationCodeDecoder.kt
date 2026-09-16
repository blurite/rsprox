package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.account.SendEmailValidationCode
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class SendEmailValidationCodeDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<SendEmailValidationCode> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendEmailValidationCode {
        val code = buffer.readNativeString()
        return SendEmailValidationCode(
            code,
        )
    }
}
