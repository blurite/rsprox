package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class SendSnapshotDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<SendSnapshot> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendSnapshot {
        val name = buffer.readNativeString()
        val categoryIndex = buffer.g1()
        val includeImage = buffer.g1()
        val description = buffer.readNativeString()
        return SendSnapshot(
            name,
            categoryIndex,
            includeImage,
            description,
        )
    }
}
