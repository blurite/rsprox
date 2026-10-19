package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcSaySpecific
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class NpcSaySpecificDecoder : ProxyMessageDecoder<NpcSaySpecific> {
    override val prot: ClientProt = GameServerProt.NPC_SAY_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcSaySpecific {
        val effect = buffer.g1Alt3()
        val npcIndex = buffer.g2Alt2()
        val text = buffer.gjstr()
        val colour = buffer.g1Alt2()
        return NpcSaySpecific(text, colour, npcIndex, effect)
    }
}
