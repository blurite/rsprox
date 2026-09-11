package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetModel
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetModelDecoder : ProxyMessageDecoder<IfSetModel> {
    override val prot: ClientProt = GameServerProt.IF_SETMODEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetModel {
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        val modelId = buffer.g4()
        return IfSetModel(
            componentHash,
            modelId,
        )
    }
}
