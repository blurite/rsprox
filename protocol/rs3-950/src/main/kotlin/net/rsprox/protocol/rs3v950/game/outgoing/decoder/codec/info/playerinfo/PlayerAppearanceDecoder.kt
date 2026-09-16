package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.AppearanceBody
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.Equipment
import net.rsprox.protocol.rs3v950.buffer.readNativeString

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
                    List(buffer.g1()) { PlayerExtendedInfo.AppearanceIcon(buffer.g2(), buffer.g1()) }
                } else {
                    emptyList()
                }
            val bodyType = buffer.g1().toByte().toInt()
            val appearance = decodeBody(buffer, definitions)
            val name = buffer.readNativeString()
            val combat = buffer.g1()
            val total = if (flags and 4 != 0) buffer.g2().let { if (it == 65535) -1 else it } else null
            val visibleCombat = if (flags and 4 == 0) buffer.g1() else null
            val difference = if (flags and 4 == 0) buffer.g1().let { if (it == 255) -1 else it } else null
            val extraFlag = buffer.g1()
            val extra = if (extraFlag != 0) List(4) { buffer.g2() } + buffer.g1() else emptyList()
            require(!storage.isReadable) { "Player appearance has ${storage.readableBytes()} trailing bytes" }
            return PlayerExtendedInfo.Appearance(
                flags,
                (flags ushr 3 and 7) + 1,
                title,
                icons,
                bodyType,
                appearance.npc,
                appearance.npcTeam,
                appearance.equipment,
                appearance.customisations,
                appearance.primaryColours,
                appearance.secondaryColours,
                appearance.renderAnimationSet,
                name,
                combat,
                total,
                visibleCombat,
                difference,
                extraFlag,
                extra,
            )
        } finally {
            storage.release()
        }
    }

    fun decodeBody(
        buffer: JagByteBuf,
        definitions: Rs3AppearanceDefinitions,
    ): AppearanceBody {
        var npc: Int? = null
        var npcTeam: Int? = null
        val equipment =
            buildList {
                for ((slot, kind) in definitions.equipmentSlotKinds.withIndex()) {
                    if (kind == 1) continue
                    val value = buffer.readVarIntLE()
                    if (slot == 0 && value == 1) {
                        npc = buffer.readModel()
                        npcTeam = buffer.g1()
                        break
                    }
                    add(
                        when {
                            value == 0 -> Equipment(slot, Equipment.Kind.EMPTY, -1)
                            value in 2..2047 -> Equipment(slot, Equipment.Kind.KIT, value - 2)
                            value >= 2048 -> Equipment(slot, Equipment.Kind.ITEM, value - 2048)
                            else -> error("Invalid equipment value $value in slot $slot")
                        },
                    )
                }
            }
        val customisations =
            buildList {
                if (npc == null || npc == -1) {
                    val mask = buffer.g2()
                    var bit = 0
                    for ((slot, kind) in definitions.equipmentSlotKinds.withIndex()) {
                        if (kind != 0) continue
                        require(bit < 16) { "Too many customisable equipment slots" }
                        if (mask and (1 shl bit++) == 0) continue
                        val item = equipment.first { it.slot == slot }
                        require(item.kind == Equipment.Kind.ITEM) { "Customisation without an item in slot $slot" }
                        val customFlags = buffer.g1()
                        val definition = if (customFlags and 3 != 0) definitions.getItem(item.id) else null

                        fun models(
                            male: List<Int>,
                            female: List<Int>,
                        ): List<PlayerExtendedInfo.ModelPair> =
                            buildList {
                                for (index in male.indices) {
                                    if (index == 0 || male[index] != -1 || female[index] != -1) {
                                        add(PlayerExtendedInfo.ModelPair(index, buffer.readModel(), buffer.readModel()))
                                    }
                                }
                            }
                        val body =
                            if (customFlags and 1 != 0) {
                                models(checkNotNull(definition).maleBodyModels, definition.femaleBodyModels)
                            } else {
                                emptyList()
                            }
                        val head =
                            if (customFlags and 2 != 0) {
                                models(checkNotNull(definition).maleHeadModels, definition.femaleHeadModels)
                            } else {
                                emptyList()
                            }
                        val recolours = if (customFlags and 4 != 0) buffer.palette(buffer.g2(), 4) else emptyList()
                        val retextures = if (customFlags and 8 != 0) buffer.palette(buffer.g1(), 2) else emptyList()
                        add(PlayerExtendedInfo.Customisation(slot, customFlags, body, head, recolours, retextures))
                    }
                    require(mask ushr bit == 0) { "Customisation mask exceeds equipment slots" }
                }
            }
        val primary = List(10) { buffer.g1() }
        val secondary = List(10) { buffer.g1() }
        val renderAnimationSet = buffer.g2().toShort().toInt()
        return AppearanceBody(npc, npcTeam, equipment, customisations, primary, secondary, renderAnimationSet)
    }

    private fun JagByteBuf.readModel(): Int =
        if (buffer.getByte(buffer.readerIndex()) < 0) {
            g4() and Int.MAX_VALUE
        } else {
            g2().let { if (it == 32767) -1 else it }
        }

    private fun JagByteBuf.readVarIntLE(): Int {
        var value = 0
        for (index in 0..4) {
            val next = g1()
            require(index != 4 || next <= 15) { "Equipment varint exceeds 32 bits" }
            value = value or ((next and 127) shl (index * 7))
            if (next and 128 == 0) return value
        }
        error("Unterminated equipment varint")
    }

    private fun JagByteBuf.palette(
        packed: Int,
        count: Int,
    ): List<PlayerExtendedInfo.PaletteReplacement> =
        buildList {
            repeat(count) {
                val index = packed ushr (it * 4) and 15
                if (index != 15) add(PlayerExtendedInfo.PaletteReplacement(index, g2()))
            }
        }
}
