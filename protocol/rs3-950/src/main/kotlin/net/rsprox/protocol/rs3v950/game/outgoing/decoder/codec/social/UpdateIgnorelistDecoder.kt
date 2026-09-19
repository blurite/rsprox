package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateIgnorelist
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateIgnorelistDecoder : ProxyMessageDecoder<UpdateIgnorelist> {
    override val prot: ClientProt = GameServerProt.UPDATE_IGNORELIST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateIgnorelist {
        val ignores = mutableListOf<UpdateIgnorelist.Ignore>()
        while (buffer.isReadable) {
            val flags = buffer.g1()
            val name = buffer.readNativeString()
            val previousName = buffer.readNativeString()
            val note = buffer.readNativeString()
            ignores += UpdateIgnorelist.Ignore(flags, name, previousName, note)
        }
        return UpdateIgnorelist(ignores)
    }
}
