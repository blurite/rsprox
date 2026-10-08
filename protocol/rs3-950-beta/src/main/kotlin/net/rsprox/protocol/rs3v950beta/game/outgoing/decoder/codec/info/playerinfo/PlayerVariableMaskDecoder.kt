package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.VariableValue
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString

internal object PlayerVariableMaskDecoder {
    fun decode(
        buffer: JagByteBuf,
        full: Boolean,
    ): PlayerExtendedInfo.Variables {
        buffer.g2() // Native discarded prefix, not a trusted length.
        val entries =
            List(buffer.g1()) {
                val tag = if (full) buffer.g1() else buffer.g1Alt3()
                val id = buffer.g2()
                // Unlike VARCLAN, these masks send their strategy tag on the wire.
                // Rs3InfoVariableProof.Registry checks the client's four real vtable bindings.
                val value =
                    when (tag) {
                        0 -> VariableValue.IntegerValue(buffer.g4())
                        1 -> VariableValue.LongValue(buffer.g8())
                        2 -> VariableValue.StringValue(buffer.readNativeString())
                        3 -> VariableValue.Coordinate(buffer.g1(), buffer.g4(), buffer.g4(), buffer.g4())
                        else -> error("Unsupported player variable tag $tag for id $id")
                    }
                PlayerExtendedInfo.Variable(id, value)
            }
        return PlayerExtendedInfo.Variables(full, entries)
    }
}
