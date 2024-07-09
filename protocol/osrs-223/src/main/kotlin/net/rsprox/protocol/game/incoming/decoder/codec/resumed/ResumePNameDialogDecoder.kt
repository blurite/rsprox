package net.rsprox.protocol.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.resumed.ResumePNameDialog

@Consistent
public class ResumePNameDialogDecoder : MessageDecoder<ResumePNameDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_NAMEDIALOG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePNameDialog {
        val name = buffer.gjstr()
        return ResumePNameDialog(name)
    }
}
