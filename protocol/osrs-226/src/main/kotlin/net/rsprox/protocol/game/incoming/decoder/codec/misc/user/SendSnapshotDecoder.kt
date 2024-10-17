package net.rsprox.protocol.game.incoming.decoder.codec.misc.user
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.session.Session

@Consistent
public class SendSnapshotDecoder : ProxyMessageDecoder<SendSnapshot> {
    override val prot: ClientProt = GameClientProt.SEND_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SendSnapshot {
        val name = buffer.gjstr()
        val ruleId = buffer.g1()
        val mute = buffer.g1() == 1
        return SendSnapshot(
            name,
            ruleId,
            mute,
        )
    }
}
