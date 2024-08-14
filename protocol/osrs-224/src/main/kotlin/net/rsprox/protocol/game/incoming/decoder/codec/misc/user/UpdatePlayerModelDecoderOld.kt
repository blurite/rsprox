package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModelOld
import net.rsprox.protocol.session.Session

@Consistent
public class UpdatePlayerModelDecoderOld : ProxyMessageDecoder<UpdatePlayerModelOld> {
    override val prot: ClientProt = GameClientProt.UPDATE_PLAYER_MODEL_OLD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdatePlayerModelOld {
        val bodyType = buffer.g1()
        val identKit = ByteArray(7)
        for (i in identKit.indices) {
            identKit[i] = buffer.g1().toByte()
        }
        val colours = ByteArray(5)
        for (i in colours.indices) {
            colours[i] = buffer.g1().toByte()
        }
        return UpdatePlayerModelOld(
            bodyType,
            identKit,
            colours,
        )
    }
}
