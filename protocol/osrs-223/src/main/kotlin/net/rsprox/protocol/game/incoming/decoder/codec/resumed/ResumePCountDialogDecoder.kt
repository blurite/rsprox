package net.rsprox.protocol.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog

@Consistent
public class ResumePCountDialogDecoder : MessageDecoder<ResumePCountDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePCountDialog {
        val count = buffer.g4()
        return ResumePCountDialog(count)
    }
}
