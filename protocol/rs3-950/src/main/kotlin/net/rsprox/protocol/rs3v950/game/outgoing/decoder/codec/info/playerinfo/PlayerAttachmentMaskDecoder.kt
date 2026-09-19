package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.TransformAxes

internal object PlayerAttachmentMaskDecoder {
    fun decode(buffer: JagByteBuf): PlayerExtendedInfo.Attachments {
        val count = buffer.g1Alt3().toByte().toInt()
        val attachments = List(count.coerceAtLeast(0)) {
            val flags = buffer.g2Alt2().toShort().toInt()
            val slot = buffer.g2().toShort().toInt()
            val id = if (flags and 0xc00 != 0) buffer.g4Alt3() else null
            val translation = TransformAxes(
                if (flags and 0x1 != 0) buffer.g4Alt1() else null,
                if (flags and 0x2 != 0) buffer.g4Alt2() else null,
                if (flags and 0x4 != 0) buffer.g4() else null,
            )
            val rotation = TransformAxes(
                if (flags and 0x8 != 0) buffer.g4() else null,
                if (flags and 0x10 != 0) buffer.g4Alt1() else null,
                if (flags and 0x20 != 0) buffer.g4() else null,
            )
            // Bit 6 rotates the translation by the supplied rotation. Scale starts at bit 7.
            val scale = TransformAxes(
                if (flags and 0x80 != 0) buffer.g4() else null,
                if (flags and 0x100 != 0) buffer.g4Alt2() else null,
                if (flags and 0x200 != 0) buffer.g4Alt3() else null,
            )
            PlayerExtendedInfo.Attachment(flags, slot, id, translation, rotation, scale)
        }
        return PlayerExtendedInfo.Attachments(count, attachments)
    }
}
