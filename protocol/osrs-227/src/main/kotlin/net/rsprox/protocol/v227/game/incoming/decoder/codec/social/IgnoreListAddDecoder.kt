package net.rsprox.protocol.v227.game.incoming.decoder.codec.social
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class IgnoreListAddDecoder : ProxyMessageDecoder<IgnoreListAdd> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IgnoreListAdd {
        val name = buffer.gjstr()
        return IgnoreListAdd(name)
    }
}
