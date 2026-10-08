package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.AppearanceBody
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.appearance.AppearanceBodyDecoder

/** Native shared appearance grammar plus the player-profile prefix/footer. */
internal object PlayerAppearanceDecoder {
    fun decode(
        bytes: ByteArray,
        definitions: Rs3AppearanceDefinitions,
    ): PlayerExtendedInfo.Appearance {
        val storage = Unpooled.wrappedBuffer(bytes)
        try {
            val buffer = storage.toJagByteBuf()
            val flags = buffer.g1()
            val title = if (flags and 0x40 != 0) buffer.gSmart1or2() else null
            val icons =
                if (flags and 2 != 0) {
                    List(buffer.g1()) { PlayerExtendedInfo.NameIcon(buffer.g2(), buffer.g1()) }
                } else {
                    emptyList()
                }
            val visibility = buffer.g1().toByte().toInt()
            val appearance = decodeBody(buffer, definitions)
            val name = buffer.readNativeString()
            val combat = buffer.g1()
            val skillLevel = if (flags and 4 != 0) buffer.g2().let { if (it == 65535) -1 else it } else null
            val effectiveCombat = if (flags and 4 == 0) buffer.g1() else null
            val colourParameter = if (flags and 4 == 0) buffer.g1().let { if (it == 255) -1 else it } else null
            val soundRange = buffer.g1().toByte().toInt()
            val backgroundSound =
                if (soundRange != 0) {
                    // Native applies these four IDs as signed shorts; 65535 is the absent sound -1.
                    PlayerExtendedInfo.BackgroundSound(
                        range = soundRange,
                        stationary = buffer.g2().toShort().toInt(),
                        crawl = buffer.g2().toShort().toInt(),
                        walk = buffer.g2().toShort().toInt(),
                        run = buffer.g2().toShort().toInt(),
                        volume = buffer.g1(),
                    )
                } else {
                    null
                }
            require(!storage.isReadable) { "Player appearance has ${storage.readableBytes()} trailing bytes" }
            return PlayerExtendedInfo.Appearance(
                flags = flags,
                size = (flags ushr 3 and 7) + 1,
                title = title,
                icons = icons,
                visibility = visibility,
                npc = appearance.npc,
                npcTeam = appearance.npcTeam,
                equipment = appearance.equipment,
                customisations = appearance.customisations,
                primaryColours = appearance.primaryColours,
                secondaryColours = appearance.secondaryColours,
                renderAnimationSet = appearance.renderAnimationSet,
                name = name,
                combatLevel = combat,
                skillLevel = skillLevel,
                effectiveCombatLevel = effectiveCombat,
                combatColourParameter = colourParameter,
                backgroundSound = backgroundSound,
            )
        } finally {
            storage.release()
        }
    }

    fun decodeBody(
        buffer: JagByteBuf,
        definitions: Rs3AppearanceDefinitions,
    ): AppearanceBody = AppearanceBodyDecoder.decodeBody(buffer, definitions)
}
