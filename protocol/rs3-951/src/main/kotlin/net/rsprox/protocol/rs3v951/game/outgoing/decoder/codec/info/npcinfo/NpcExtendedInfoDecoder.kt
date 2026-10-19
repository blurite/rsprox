package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.UnusedExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.ATTACHMENTS
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.BAS_OVERRIDE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.BODY_CUSTOMISATION
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.DISABLED_OPS
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.EXACT_MOVE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.FACE_ENTITY
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.FACE_TILE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.HEADICON_CUSTOMISATION
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.HEAD_CUSTOMISATION
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V1
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V2
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.NAME_CHANGE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.NPC_STATS
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.OVERLAP_CULLING
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.PRIORITY_OFFSET
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.SAY
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.SEQUENCE
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.SPOTANIM
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.TINTING
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.TRANSFORMATION
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.VARNPC_DELTA
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey.VARNPC_FULL
import net.rsprox.protocol.rs3v951.buffer.readNativeString
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.readSpotanimRemovals

/** Revision-951 wire bits/order, mapped to revision-independent semantic keys. */
internal object NpcExtendedInfoDecoder {
    private val routes =
        listOf(
            34 to HITMARKS_AND_HEADBARS_V2,
            4 to TRANSFORMATION,
            14 to EXACT_MOVE,
            10 to BODY_CUSTOMISATION,
            30 to DISABLED_OPS,
            27 to TINTING,
            0 to HITMARKS_AND_HEADBARS_V1,
            28 to null,
            11 to null,
            12 to null,
            23 to HEADICON_CUSTOMISATION,
            31 to OVERLAP_CULLING,
            19 to COMBAT_LEVEL_CHANGE,
            1 to FACE_TILE,
            13 to null,
            20 to VARNPC_DELTA,
            2 to FACE_ENTITY,
            16 to VARNPC_FULL,
            25 to null,
            29 to SPOTANIM,
            18 to HEAD_CUSTOMISATION,
            7 to SAY,
            17 to NPC_STATS,
            32 to ATTACHMENTS,
            24 to null,
            22 to NAME_CHANGE,
            5 to null,
            3 to SEQUENCE,
            15 to BAS_OVERRIDE,
            33 to PRIORITY_OFFSET,
        )
    private val continuations = intArrayOf(6, 8, 21, 26)
    private val known =
        (routes.map { it.first } + continuations.toList()).fold(0L) { value, bit ->
            value or
                (1L shl bit)
        }

    fun decode(
        buffer: JagByteBuf,
        initialType: Int,
        definitions: Rs3PacketDefinitions?,
        morphVariables: NpcMorphVariables = NpcMorphVariables(),
    ): List<NpcExtendedInfo> {
        var mask = buffer.g1().toLong()
        for ((index, bit) in continuations.withIndex()) {
            if (mask and (1L shl bit) == 0L) break
            mask = mask or (buffer.g1().toLong() shl ((index + 1) * 8))
        }
        require(mask and known.inv() == 0L) { "Unknown NPC mask bits: ${mask.toString(16)}" }
        var type = initialType
        return buildList {
            for ((bit, key) in routes) {
                if (mask and (1L shl bit) == 0L) continue
                if (key == null) {
                    // Note(revision): native reads and discards these blocks; retain their wire fields.
                    val fields =
                        when (bit) {
                            28 -> listOf(buffer.g2Alt3(), buffer.g4Alt2(), buffer.g1())
                            11 -> listOf(buffer.g2Alt2(), buffer.g4Alt2(), buffer.g1Alt2())
                            12 -> listOf(buffer.g1(), buffer.g2Alt2(), buffer.g2(), buffer.g2Alt3())
                            13 -> listOf(buffer.g1Alt2(), buffer.g1Alt3(), buffer.g2())
                            25 -> listOf(buffer.g2Alt3(), buffer.g4(), buffer.g1Alt2())
                            24 -> listOf(buffer.g2Alt1(), buffer.g4Alt3(), buffer.g1Alt1())
                            5 -> listOf(buffer.g2(), buffer.g4Alt3(), buffer.g1())
                            else -> error("Unregistered discarded NPC mask $bit")
                        }
                    add(UnusedExtendedInfo(bit, fields))
                    continue
                }

                fun scalars(vararg values: Int) = NpcMask.Scalars(key, values.toList())
                val value =
                    when (key) {
                        SAY, NAME_CHANGE -> NpcMask.Text(key, buffer.readNativeString())
                        PRIORITY_OFFSET -> {
                            // Native -128 clears the override; zero is an explicit priority offset.
                            val offset = buffer.g1().toByte().toInt()
                            NpcMask.PriorityOffset(offset.takeUnless { it == -128 })
                        }
                        BAS_OVERRIDE -> NpcMask.BasOverride(buffer.g2Alt2().takeUnless { it == 65535 })
                        HEAD_CUSTOMISATION -> customisation(buffer, key, type, definitions, morphVariables, false)
                        OVERLAP_CULLING -> NpcMask.OverlapCulling(buffer.g1() == 1)
                        TINTING ->
                            scalars(
                                buffer.g1Alt1(),
                                buffer.g1Alt3(),
                                buffer.g1Alt1(),
                                buffer.g1(),
                                buffer.g2(),
                                buffer.g2(),
                            )
                        TRANSFORMATION -> {
                            type = buffer.model()
                            NpcMask.Transformation(type)
                        }
                        EXACT_MOVE ->
                            NpcMask.ExactMove(
                                deltaX1 = buffer.g1().toByte().toInt(),
                                deltaZ1 = buffer.g1Alt3().toByte().toInt(),
                                deltaX2 = buffer.g1Alt2().toByte().toInt(),
                                deltaZ2 = buffer.g1Alt2().toByte().toInt(),
                                deltaLevel1 = buffer.g1Alt3().toByte().toInt(),
                                deltaLevel2 = buffer.g1().toByte().toInt(),
                                delay1 = buffer.g2Alt2(),
                                delay2 = buffer.g2Alt2(),
                                angle = buffer.g2(),
                            )
                        VARNPC_FULL, VARNPC_DELTA -> variables(buffer, key)
                        NPC_STATS ->
                            NpcMask.Stats(
                                List(buffer.g1()) {
                                    NpcMask.Stat(buffer.g1Alt1(), buffer.g4(), buffer.g3Alt1())
                                },
                            )
                        FACE_ENTITY -> NpcMask.FaceEntity(buffer.g3Alt1())
                        FACE_TILE -> scalars(buffer.g2Alt1(), buffer.g2Alt2())
                        ATTACHMENTS -> attachments(buffer)
                        BODY_CUSTOMISATION -> customisation(buffer, key, type, definitions, morphVariables, true)
                        SPOTANIM -> {
                            val removals = buffer.readSpotanimRemovals()
                            val additions =
                                List(buffer.g1()) {
                                    NpcMask.Spotanim(
                                        buffer.g1Alt2(),
                                        buffer.g2(),
                                        buffer.g4(),
                                        buffer.g1(),
                                        buffer.g3Alt3(),
                                    )
                                }
                            NpcMask.Spotanims(removals, additions)
                        }
                        HITMARKS_AND_HEADBARS_V2, HITMARKS_AND_HEADBARS_V1 ->
                            hits(
                                buffer,
                                key == HITMARKS_AND_HEADBARS_V2,
                            )
                        COMBAT_LEVEL_CHANGE -> scalars(buffer.g2Alt2())
                        SEQUENCE -> NpcMask.Sequence(List(4) { buffer.model() }, buffer.g1())
                        HEADICON_CUSTOMISATION -> {
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
                        DISABLED_OPS -> scalars(buffer.g1Alt3())
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
                val tag = buffer.g1Alt2()
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
        val flags = if (body) buffer.g1() else buffer.g1Alt3()
        if (flags and 1 != 0) {
            return NpcMask.Customisation(key, flags, emptyList(), emptyList(), emptyList(), emptyList())
        }
        val models =
            if (flags and 2 == 0) {
                emptyList()
            } else {
                List(if (body) buffer.g1Alt1() else buffer.g1Alt3()) {
                    val id = buffer.model()
                    val transformed = body && id != -1 && flags and 16 != 0
                    val scale = if (transformed) Float.fromBits(buffer.g4()) else null
                    val translation = if (transformed) List(3) { buffer.g2Alt1().toShort().toInt() } else emptyList()
                    val rotation = if (transformed) List(3) { buffer.g2().toShort().toInt() } else emptyList()
                    val recolours =
                        if (body && id != -1 && flags and 32 != 0) {
                            List(buffer.g1Alt2()) { buffer.g2Alt2().toShort().toInt() }
                        } else {
                            emptyList()
                        }
                    val retextures =
                        if (body && id != -1 && flags and 64 != 0) {
                            List(buffer.g1Alt2()) { buffer.g2Alt1().toShort().toInt() }
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
                    buffer.g2()
                }
            }
        val retextures =
            if (flags and 8 == 0) {
                emptyList()
            } else {
                List(checkNotNull(definition).retextureCount) { if (body) buffer.g2Alt1() else buffer.g2Alt3() }
            }
        val paletteIndices = if (body && flags and 128 != 0) List(10) { buffer.g1() } else emptyList()
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
            List(if (wide) buffer.g1Alt3() else buffer.g1Alt1()) {
                var id = buffer.gSmart1or2()
                var secondary = -1
                var secondaryValue = -1
                val value =
                    when (id) {
                        32767 -> {
                            id = buffer.gSmart1or2()
                            val first = if (wide) buffer.g4Alt2() else buffer.gSmart1or2()
                            secondary = buffer.gSmart1or2()
                            secondaryValue = if (wide) buffer.g4Alt2() else buffer.gSmart1or2()
                            first
                        }
                        32766 -> {
                            id = -1
                            if (wide) buffer.g1Alt2() else buffer.g1()
                        }
                        else -> if (wide) buffer.g4Alt2() else buffer.gSmart1or2()
                    }
                NpcMask.Hit(id, value, secondary, secondaryValue, buffer.gSmart1or2())
            }
        val bars =
            List(if (wide) buffer.g1Alt2() else buffer.g1()) {
                val id = buffer.gSmart1or2()
                val duration = buffer.gSmart1or2()
                if (duration == 32767) {
                    NpcMask.Headbar(id, duration, null, null, null, null, null, null)
                } else {
                    val delay = buffer.gSmart1or2()
                    val startFill = if (wide) buffer.g1Alt2() else buffer.g1Alt3()
                    val endFill =
                        if (duration == 0) {
                            startFill
                        } else if (wide) {
                            buffer.g1()
                        } else {
                            buffer.g1Alt3()
                        }
                    val secondaryId = buffer.gSmart1or2() - 1
                    val secondaryStartFill =
                        if (secondaryId == -1) {
                            null
                        } else if (wide) {
                            buffer.g1Alt3()
                        } else {
                            buffer.g1()
                        }
                    val secondaryEndFill =
                        if (secondaryId == -1) {
                            null
                        } else if (duration == 0) {
                            secondaryStartFill
                        } else {
                            if (wide) buffer.g1Alt2() else buffer.g1Alt3()
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
        val count = buffer.g1().toByte().toInt()
        val attachments =
            List(count.coerceAtLeast(0)) {
                val flags = buffer.g2().toShort().toInt()
                val slot = buffer.g2Alt1().toShort().toInt()
                val id = if (flags and 0xc00 != 0) buffer.g4Alt2() else null
                val translation =
                    listOf(
                        if (flags and 1 != 0) buffer.g4Alt3() else null,
                        if (flags and 2 != 0) buffer.g4Alt3() else null,
                        if (flags and 4 != 0) buffer.g4Alt1() else null,
                    )
                val rotation =
                    listOf(
                        if (flags and 8 != 0) buffer.g4() else null,
                        if (flags and 16 != 0) buffer.g4() else null,
                        if (flags and 32 != 0) buffer.g4Alt1() else null,
                    )
                val scale =
                    listOf(
                        if (flags and 128 != 0) buffer.g4Alt2() else null,
                        if (flags and 256 != 0) buffer.g4Alt2() else null,
                        if (flags and 512 != 0) buffer.g4Alt3() else null,
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
