package net.rsprox.protocol.v226.game.incoming.decoder.codec.misc.user
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModelV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class UpdatePlayerModelDecoderV1 : ProxyMessageDecoder<UpdatePlayerModelV1> {
    override val prot: ClientProt = GameClientProt.UPDATE_PLAYER_MODEL_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdatePlayerModelV1 {
        val bodyType = buffer.g1()
        val identKit = ByteArray(7)
        for (i in identKit.indices) {
            identKit[i] = buffer.g1().toByte()
        }
        val colours = ByteArray(5)
        for (i in colours.indices) {
            colours[i] = buffer.g1().toByte()
        }
        return UpdatePlayerModelV1(
            bodyType,
            identKit,
            colours,
        )
    }
}
