package net.rsprox.protocol.v229.game.outgoing.decoder.codec.unknown

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownVarShort
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UnknownVarShortDecoder : ProxyMessageDecoder<UnknownVarShort> {
    override val prot: ClientProt = GameServerProt.UNKNOWN_VAR_SHORT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UnknownVarShort {
        val value = buffer.g1()
        val remaining = ByteArray(buffer.readableBytes())
        buffer.gdata(remaining)
        return UnknownVarShort(value, remaining)
    }
}
