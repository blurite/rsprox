package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.readSpotanimRemovals

/** Selectors/order verified against the native revision-950 mask dispatcher. */
internal object NpcExtendedInfoDecoder {
    private val keys = Rs3NpcUpdateMaskKey.byOrder
    private val continuations = intArrayOf(4, 13, 23, 27)
    private val known = (keys.map { it.bit } + continuations.toList()).fold(0L) { value, bit -> value or (1L shl bit) }

    fun decode(
        buffer: JagByteBuf,
        initialType: Int,
        definitions: Rs3PacketDefinitions?,
        morphVariables: NpcMorphVariables = NpcMorphVariables(),
    ): List<NpcMask> {
        var mask = buffer.g1().toLong()
        for ((index, bit) in continuations.withIndex()) {
            if (mask and (1L shl bit) == 0L) break
            mask = mask or (buffer.g1().toLong() shl ((index + 1) * 8))
        }
        require(mask and known.inv() == 0L) { "Unknown NPC mask bits: ${mask.toString(16)}" }
        var type = initialType
        return buildList {
            for (key in keys) {
                if (mask and (1L shl key.bit) == 0L) continue

                fun scalars(vararg values: Int) = NpcMask.Scalars(key, values.toList())
                val value =
                    when (key.bit) {
                        6, 18 -> NpcMask.Text(key, buffer.readNativeString())
                        // Native 950 consumes these fields without using them; retain them for logging.
                        0 -> scalars(buffer.g2Alt3(), buffer.g4Alt2(), buffer.g1Alt3())
                        34 -> {
                            // Native -128 clears the override; zero is an explicit priority offset.
                            val offset = buffer.g1Alt2().toByte().toInt()
                            NpcMask.PriorityOffset(offset.takeUnless { it == -128 })
                        }
                        12 -> NpcMask.BasOverride(buffer.g2Alt1().takeUnless { it == 65535 })
                        20 -> customisation(buffer, key, type, definitions, morphVariables, false)
                        26 -> NpcMask.OverlapCulling(buffer.g1Alt1() == 1)
                        // Native 950 consumes these fields without using them; retain them for logging.
                        29 -> scalars(buffer.g2Alt2(), buffer.g4(), buffer.g1Alt1())
                        28 ->
                            scalars(
                                buffer.g1Alt1(),
                                buffer.g1Alt1(),
                                buffer.g1(),
                                buffer.g1(),
                                buffer.g2(),
                                buffer.g2(),
                            )
                        2 -> {
                            type = buffer.model()
                            NpcMask.Transformation(type)
                        }
                        14 ->
                            NpcMask.ExactMove(
                                deltaX1 = buffer.g1Alt2().toByte().toInt(),
                                deltaZ1 = buffer.g1Alt2().toByte().toInt(),
                                deltaX2 = buffer.g1().toByte().toInt(),
                                deltaZ2 = buffer.g1().toByte().toInt(),
                                deltaLevel1 = buffer.g1Alt2().toByte().toInt(),
                                deltaLevel2 = buffer.g1().toByte().toInt(),
                                delay1 = buffer.g2Alt1(),
                                delay2 = buffer.g2Alt1(),
                                angle = buffer.g2Alt1(),
                            )
                        21, 16 -> variables(buffer, key)
                        // Native 950 consumes these fields without using them; retain them for logging.
                        8 -> scalars(buffer.g2Alt1(), buffer.g4(), buffer.g1Alt2())
                        19 ->
                            NpcMask.Stats(
                                List(buffer.g1()) {
                                    NpcMask.Stat(buffer.g1Alt1(), buffer.g4Alt1(), buffer.g3Alt2())
                                },
                            )
                        // Native 950 consumes these fields without using them; retain them for logging.
                        15 -> scalars(buffer.g1Alt3(), buffer.g1(), buffer.g2())
                        1 -> NpcMask.FaceEntity(buffer.g3Alt3())
                        7 -> scalars(buffer.g2(), buffer.g2Alt1())
                        // Native 950 consumes these fields without using them; retain them for logging.
                        31 -> scalars(buffer.g2Alt3(), buffer.g4Alt3(), buffer.g1())
                        // Native 950 consumes these fields without using them; retain them for logging.
                        11 -> scalars(buffer.g1Alt1(), buffer.g2Alt1(), buffer.g2(), buffer.g2Alt2())
                        32 -> attachments(buffer)
                        10 -> customisation(buffer, key, type, definitions, morphVariables, true)
                        24 -> {
                            val removals = buffer.readSpotanimRemovals()
                            val additions =
                                List(buffer.g1Alt1()) {
                                    NpcMask.Spotanim(
                                        buffer.g1(),
                                        buffer.g2Alt2(),
                                        buffer.g4Alt3(),
                                        buffer.g1Alt3(),
                                        buffer.g3Alt3(),
                                    )
                                }
                            NpcMask.Spotanims(removals, additions)
                        }
                        33, 5 -> hits(buffer, key.bit == 33)
                        17 -> scalars(buffer.g2Alt1())
                        3 -> NpcMask.Sequence(List(4) { buffer.model() }, buffer.g1())
                        22 -> {
                            val presence = buffer.g1Alt2()
                            // Native expands omitted head-icon slots to -1/-1, not an unchanged-slot delta.
                            NpcMask.HeadIconCustomisation(
                                List(8) { slot ->
                                    if (presence and (1 shl slot) != 0) {
                                        NpcMask.HeadIcon(slot, buffer.gSmart2or4null(), buffer.gSmart1or2() - 1)
                                    } else {
                                        NpcMask.HeadIcon(slot, -1, -1)
                                    }
                                },
                            )
                        }
                        // One replacement bitmask: set bits suppress NPC interaction options.
                        25 -> scalars(buffer.g1())
                        // Native 950 consumes these fields without using them; retain them for logging.
                        30 -> scalars(buffer.g2Alt2(), buffer.g4Alt3(), buffer.g1Alt2())
                        else -> error("Unimplemented NPC mask $key")
                    }
                add(value)
            }
        }
    }

    private fun variables(
        buffer: JagByteBuf,
        key: Rs3NpcUpdateMaskKey,
    ): NpcMask.Variables {
        val prefix = buffer.g2()
        val variables =
            List(buffer.g1()) {
                val tag = if (key.bit == 21) buffer.g1Alt3() else buffer.g1()
                val id = buffer.g2()
                val value =
                    when (tag) {
                        0 -> TypedVariable.IntValue(buffer.g4())
                        1 -> TypedVariable.LongValue(buffer.g8())
                        2 -> TypedVariable.StringValue(buffer.readNativeString())
                        3 -> TypedVariable.Coordinate(buffer.g1(), buffer.g4(), buffer.g4(), buffer.g4())
                        else -> error("Unknown NPC variable tag $tag")
                    }
                TypedVariable(id, listOf(0, 110, 36, 50)[tag], value)
            }
        return NpcMask.Variables(key, prefix, variables)
    }

    private fun customisation(
        buffer: JagByteBuf,
        key: Rs3NpcUpdateMaskKey,
        type: Int,
        definitions: Rs3PacketDefinitions?,
        morphVariables: NpcMorphVariables,
        body: Boolean,
    ): NpcMask.Customisation {
        val flags = if (body) buffer.g1Alt1() else buffer.g1()
        if (flags and 1 != 0) {
            return NpcMask.Customisation(key, flags, emptyList(), emptyList(), emptyList(), emptyList())
        }
        val models =
            if (flags and 2 == 0) {
                emptyList()
            } else {
                List(if (body) buffer.g1Alt3() else buffer.g1Alt2()) {
                    val id = buffer.model()
                    val transformed = body && id != -1 && flags and 16 != 0
                    val scale = if (transformed) Float.fromBits(buffer.g4()) else null
                    val rotation = if (transformed) List(3) { buffer.g2Alt1().toShort().toInt() } else emptyList()
                    val translation = if (transformed) List(3) { buffer.g2().toShort().toInt() } else emptyList()
                    val recolours =
                        if (body && id != -1 && flags and 32 != 0) {
                            List(buffer.g1Alt1()) { buffer.g2Alt2().toShort().toInt() }
                        } else {
                            emptyList()
                        }
                    val retextures =
                        if (body && id != -1 && flags and 64 != 0) {
                            List(buffer.g1Alt3()) { buffer.g2Alt1().toShort().toInt() }
                        } else {
                            emptyList()
                        }
                    NpcMask.Model(id, scale, rotation, translation, recolours, retextures)
                }
            }
        val definition =
            if (flags and 12 != 0) {
                morphVariables.resolve(
                    type,
                    checkNotNull(definitions) { "NPC palette customisation requires the live cache" },
                )
            } else {
                null
            }
        val recolours =
            if (flags and 4 == 0) {
                emptyList()
            } else {
                List(checkNotNull(definition).recolourCount) {
                    if (body) buffer.g2() else buffer.g2Alt3()
                }
            }
        val retextures =
            if (flags and 8 == 0) {
                emptyList()
            } else {
                List(checkNotNull(definition).retextureCount) { buffer.g2Alt2() }
            }
        val paletteIndices = if (body && flags and 128 != 0) List(10) { buffer.g1Alt2() } else emptyList()
        return NpcMask.Customisation(
            key,
            flags,
            models,
            recolours,
            retextures,
            paletteIndices,
            if (flags and 4 != 0) checkNotNull(definition).recolourSlots else emptyList(),
            if (flags and 8 != 0) checkNotNull(definition).retextureSlots else emptyList(),
        )
    }

    private fun hits(
        buffer: JagByteBuf,
        wide: Boolean,
    ): NpcMask.Hits {
        val hits =
            List(if (wide) buffer.g1Alt1() else buffer.g1Alt2()) {
                var id = buffer.gSmart1or2()
                var secondary = -1
                var secondaryValue = -1
                val value =
                    when (id) {
                        32767 -> {
                            id = buffer.gSmart1or2()
                            val first = if (wide) buffer.g4Alt1() else buffer.gSmart1or2()
                            secondary = buffer.gSmart1or2()
                            secondaryValue = if (wide) buffer.g4Alt3() else buffer.gSmart1or2()
                            first
                        }
                        32766 -> {
                            id = -1
                            if (wide) buffer.g1Alt1() else buffer.g1Alt2()
                        }
                        else -> if (wide) buffer.g4Alt1() else buffer.gSmart1or2()
                    }
                NpcMask.Hit(id, value, secondary, secondaryValue, buffer.gSmart1or2())
            }
        val bars =
            List(if (wide) buffer.g1Alt3() else buffer.g1()) {
                val id = buffer.gSmart1or2()
                val duration = buffer.gSmart1or2()
                if (duration == 32767) {
                    NpcMask.Headbar(id, duration, null, null, null, null, null, null)
                } else {
                    val delay = buffer.gSmart1or2()
                    val startFill = if (wide) buffer.g1() else buffer.g1Alt1()
                    val endFill = if (duration == 0) startFill else buffer.g1Alt2()
                    val secondaryId = buffer.gSmart1or2() - 1
                    val secondaryStartFill = if (secondaryId == -1) null else buffer.g1()
                    val secondaryEndFill =
                        if (secondaryId == -1) {
                            null
                        } else if (duration == 0) {
                            secondaryStartFill
                        } else {
                            buffer.g1Alt3()
                        }
                    NpcMask.Headbar(
                        id,
                        duration,
                        delay,
                        startFill,
                        endFill,
                        secondaryId.takeIf { it != -1 },
                        secondaryStartFill,
                        secondaryEndFill,
                    )
                }
            }
        return NpcMask.Hits(wide, hits, bars)
    }

    private fun attachments(buffer: JagByteBuf): NpcMask.Attachments {
        val count = buffer.g1Alt2().toByte().toInt()
        val attachments =
            List(count.coerceAtLeast(0)) {
                val flags = buffer.g2Alt3().toShort().toInt()
                val slot = buffer.g2().toShort().toInt()
                val id = if (flags and 0xc00 != 0) buffer.g4Alt3() else null
                val translation =
                    listOf(
                        if (flags and 1 != 0) buffer.g4Alt3() else null,
                        if (flags and 2 != 0) buffer.g4() else null,
                        if (flags and 4 != 0) buffer.g4Alt1() else null,
                    )
                val rotation =
                    listOf(
                        if (flags and 8 != 0) buffer.g4Alt3() else null,
                        if (flags and 16 != 0) buffer.g4Alt2() else null,
                        if (flags and 32 != 0) buffer.g4Alt2() else null,
                    )
                val scale =
                    listOf(
                        if (flags and 128 != 0) buffer.g4Alt3() else null,
                        if (flags and 256 != 0) buffer.g4Alt1() else null,
                        if (flags and 512 != 0) buffer.g4Alt2() else null,
                    )
                NpcMask.Attachment(flags, slot, id, translation, rotation, scale)
            }
        return NpcMask.Attachments(count, attachments)
    }

    private fun JagByteBuf.model(): Int =
        if (buffer.getByte(buffer.readerIndex()) < 0) {
            g4() and Int.MAX_VALUE
        } else {
            g2().let { if (it == 32767) -1 else it }
        }
}
