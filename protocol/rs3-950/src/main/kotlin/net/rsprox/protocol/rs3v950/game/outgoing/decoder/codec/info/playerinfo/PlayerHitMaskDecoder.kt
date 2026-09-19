package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.Headbar

internal object PlayerHitMaskDecoder {
    fun decode(
        buffer: JagByteBuf,
        wide: Boolean,
    ): PlayerExtendedInfo.Hits {
        val hitCount = if (wide) buffer.g1Alt2() else buffer.g1Alt1()
        val hits =
            List(hitCount) {
                var type = buffer.gSmart1or2()
                val value: Int
                var secondaryType = -1
                var secondaryValue = -1
                when (type) {
                    32767 -> {
                        type = buffer.gSmart1or2()
                        value = if (wide) buffer.g4Alt3() else buffer.gSmart1or2()
                        secondaryType = buffer.gSmart1or2()
                        secondaryValue = if (wide) buffer.g4Alt1() else buffer.gSmart1or2()
                    }
                    32766 -> {
                        type = -1
                        value = if (wide) buffer.g1Alt3() else buffer.g1()
                    }
                    else -> value = if (wide) buffer.g4Alt3() else buffer.gSmart1or2()
                }
                PlayerExtendedInfo.Hit(type, value, secondaryType, secondaryValue, buffer.gSmart1or2())
            }
        val headbarCount = if (wide) buffer.g1Alt1() else buffer.g1Alt2()
        val headbars = List(headbarCount) { readHeadbar(buffer, wide) }
        return PlayerExtendedInfo.Hits(wide, hits, headbars)
    }

    private fun readHeadbar(
        buffer: JagByteBuf,
        wide: Boolean,
    ): Headbar {
        val type = buffer.gSmart1or2()
        val duration = buffer.gSmart1or2()
        if (duration == 32767) return Headbar.Remove(type)
        val delay = buffer.gSmart1or2()
        val startFill = if (wide) buffer.g1Alt1() else buffer.g1Alt2()
        val endFill =
            if (duration == 0) {
                startFill
            } else if (wide) {
                buffer.g1Alt1()
            } else {
                buffer.g1Alt3()
            }
        val secondaryId = buffer.gSmart1or2() - 1
        val secondary =
            if (secondaryId == -1) {
                null
            } else {
                val secondaryStartFill = if (wide) buffer.g1() else buffer.g1Alt1()
                val secondaryEndFill =
                    if (duration == 0) {
                        secondaryStartFill
                    } else if (wide) {
                        buffer.g1()
                    } else {
                        buffer.g1Alt1()
                    }
                PlayerExtendedInfo.SecondaryHeadbar(secondaryId, secondaryStartFill, secondaryEndFill)
            }
        return Headbar.Update(type, duration, delay, startFill, endFill, secondary)
    }
}
