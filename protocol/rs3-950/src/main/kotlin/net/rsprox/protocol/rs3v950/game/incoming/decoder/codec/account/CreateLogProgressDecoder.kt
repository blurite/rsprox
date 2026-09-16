package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.account

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.account.CreateLogProgress
import net.rsprox.protocol.session.Session

internal class CreateLogProgressDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<CreateLogProgress> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CreateLogProgress {
        val progress = buffer.g1()
        return CreateLogProgress(
            progress,
        )
    }
}
