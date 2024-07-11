package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModel
import net.rsprox.protocol.session.Session

@Consistent
public class UpdatePlayerModelDecoder : ProxyMessageDecoder<UpdatePlayerModel> {
    override val prot: ClientProt = GameClientProt.UPDATE_PLAYER_MODEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdatePlayerModel {
        val bodyType = buffer.g1()
        val identKit = ByteArray(7)
        for (i in identKit.indices) {
            identKit[i] = buffer.g1().toByte()
        }
        val colours = ByteArray(5)
        for (i in colours.indices) {
            colours[i] = buffer.g1().toByte()
        }
        return UpdatePlayerModel(
            bodyType,
            identKit,
            colours,
        )
    }
}
