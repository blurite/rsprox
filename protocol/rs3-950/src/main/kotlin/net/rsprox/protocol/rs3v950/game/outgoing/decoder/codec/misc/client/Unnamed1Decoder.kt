package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed1
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class Unnamed1Decoder : ProxyMessageDecoder<Unnamed1> {
    override val prot: ClientProt = GameServerProt.UNNAMED_1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Unnamed1 {
        val mode = buffer.g1()
        val modeWord = if (mode == 1) buffer.g2() else null
        val modeBytes = if (mode == 2) Unnamed1.ModeBytes(buffer.g1(), buffer.g1(), buffer.g1()) else null
        val textFlag = buffer.g1()
        val discardedText = if (textFlag == 0) buffer.readNativeString() else null
        return Unnamed1(mode, modeWord, modeBytes, textFlag, discardedText)
    }
}
