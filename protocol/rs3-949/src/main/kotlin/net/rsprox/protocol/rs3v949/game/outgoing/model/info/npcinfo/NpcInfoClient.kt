package net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo

import io.netty.buffer.ByteBufUtil
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.AnimationExtendedInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.OpaqueExtendedInfo
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.info.npcinfo.NpcInfoDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey

public class NpcInfoClient : NpcInfoDecoder {
    private val knownNpcIndices = mutableListOf<Int>()

    private val npcIdByIndex = mutableMapOf<Int, Int>()

    private val npcCoordByIndex = mutableMapOf<Int, CoordGrid>()

    override public fun lookupNpcId(index: Int): Int? = npcIdByIndex[index]

    private fun npcLabel(index: Int?): String {
        if (index == null) return "npc=null"
        val id = lookupNpcId(index)
        return if (id != null) "npc=$index(id=$id)" else "npc=$index"
    }

    public fun decodeText(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): String {
        val hexDump = ByteBufUtil.hexDump(buffer.buffer, buffer.buffer.readerIndex(), buffer.readableBytes())
        val originalReadable = buffer.readableBytes()
        return try {
            decodeOrThrow(buffer, baseCoord)
        } catch (e: Exception) {
            val diag = "NPC_INFO <decode FAILED: ${e.javaClass.simpleName}: ${e.message}> " +
                "len=$originalReadable trackedKnown=${knownNpcIndices.size} hex=[$hexDump]"
            knownNpcIndices.clear()
            npcIdByIndex.clear()
            npcCoordByIndex.clear()
            diag
        }
    }

    private fun step(
        coord: CoordGrid,
        direction: Int,
    ): CoordGrid {
        val dx = NPC_DIR_DELTA_X[direction]
        val dz = NPC_DIR_DELTA_Z[direction]
        return CoordGrid(coord.level, coord.x + dx, coord.z + dz)
    }

    private fun resolveAdd(
        npcIndex: Int,
        baseCoord: CoordGrid,
        level: Int,
        deltaX: Int,
        deltaZ: Int,
    ): CoordGrid {
        val coord = CoordGrid(level, baseCoord.x + deltaX, baseCoord.z + deltaZ)
        npcCoordByIndex[npcIndex] = coord
        return coord
    }

    private fun resolveActive(
        npcIndex: Int,
        baseCoord: CoordGrid,
        dir1: Int?,
        dir2: Int?,
    ): CoordGrid {
        var coord = npcCoordByIndex[npcIndex] ?: baseCoord
        if (dir1 != null) coord = step(coord, dir1)
        if (dir2 != null) coord = step(coord, dir2)
        npcCoordByIndex[npcIndex] = coord
        return coord
    }

    private fun decodeOrThrow(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): String {
        val sb = StringBuilder("NPC_INFO")

        val stillKnown: ArrayList<Int>
        val extInfoQueue = ArrayList<Int>(4)

        buffer.buffer.toBitBuf().use { bits ->
            val knownCount = bits.gBits(8)
            if (knownCount != knownNpcIndices.size) {
                sb.append(
                    " [warn: wire knownCount=$knownCount but this decoder is tracking " +
                        "${knownNpcIndices.size} - state likely desynced (missed a packet on this " +
                        "connection?), results below may misattribute npc indices]",
                )
            }

            stillKnown = ArrayList(knownCount)

            for (i in 0 until knownCount) {
                val npcIndex = knownNpcIndices.getOrNull(i)
                if (bits.gBits(1) == 0) {
                    if (npcIndex != null) stillKnown.add(npcIndex)
                    continue
                }
                when (val movementType = bits.gBits(2)) {
                    3 -> {
                        sb.append("\n  REMOVE ${npcLabel(npcIndex)}")
                        if (npcIndex != null) {
                            npcIdByIndex.remove(npcIndex)
                            npcCoordByIndex.remove(npcIndex)
                        }
                    }
                    2 -> {
                        val isTrueRun = bits.gBits(1) != 0
                        if (isTrueRun) {
                            val dir1 = bits.gBits(3)
                            val dir2 = bits.gBits(3)
                            val hasExt = bits.gBits(1) != 0
                            if (npcIndex != null) {
                                val coord = resolveActive(npcIndex, baseCoord, dir1, dir2)
                                sb.append(
                                    "\n  RUN ${npcLabel(npcIndex)} dir1=$dir1 dir2=$dir2 " +
                                        "level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt",
                                )
                                stillKnown.add(npcIndex)
                                if (hasExt) extInfoQueue.add(npcIndex)
                            } else {
                                sb.append("\n  RUN ${npcLabel(npcIndex)} dir1=$dir1 dir2=$dir2 ext=$hasExt")
                            }
                        } else {
                            val dir = bits.gBits(3)
                            val hasExt = bits.gBits(1) != 0
                            if (npcIndex != null) {
                                val coord = resolveActive(npcIndex, baseCoord, dir, null)
                                sb.append(
                                    "\n  STEP_ALT ${npcLabel(npcIndex)} dir=$dir " +
                                        "level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt",
                                )
                                stillKnown.add(npcIndex)
                                if (hasExt) extInfoQueue.add(npcIndex)
                            } else {
                                sb.append("\n  STEP_ALT ${npcLabel(npcIndex)} dir=$dir ext=$hasExt")
                            }
                        }
                    }
                    1 -> {
                        val walkDir = bits.gBits(3)
                        val hasExt = bits.gBits(1) != 0
                        if (npcIndex != null) {
                            val coord = resolveActive(npcIndex, baseCoord, walkDir, null)
                            sb.append(
                                "\n  WALK ${npcLabel(npcIndex)} dir=$walkDir " +
                                    "level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt",
                            )
                            stillKnown.add(npcIndex)
                            if (hasExt) extInfoQueue.add(npcIndex)
                        } else {
                            sb.append("\n  WALK ${npcLabel(npcIndex)} dir=$walkDir ext=$hasExt")
                        }
                    }
                    0 -> {
                        sb.append("\n  EXT_ONLY ${npcLabel(npcIndex)}")
                        if (npcIndex != null) {
                            stillKnown.add(npcIndex)
                            extInfoQueue.add(npcIndex)
                        }
                    }
                    else -> sb.append("\n  [unreachable movementType=$movementType]")
                }
            }
            while (bits.readableBits() > 15) {
                val npcIndex = bits.gBits(16)
                if (npcIndex == 0xFFFF) break
                val rawDx = bits.gBits(COORD_BIT_WIDTH)
                val deltaX = if (rawDx >= COORD_HALF) rawDx - (1 shl COORD_BIT_WIDTH) else rawDx
                val displayTypeId = bits.gBits(16)
                val hasExt = bits.gBits(1) != 0
                val level = bits.gBits(2)
                val direction = bits.gBits(3)
                val rawDz = bits.gBits(COORD_BIT_WIDTH)
                val deltaZ = if (rawDz >= COORD_HALF) rawDz - (1 shl COORD_BIT_WIDTH) else rawDz
                bits.gBits(1)

                val coord = resolveAdd(npcIndex, baseCoord, level, deltaX, deltaZ)
                sb.append(
                    "\n  ADD npc=$npcIndex id=$displayTypeId " +
                        "level=${coord.level} x=${coord.x} z=${coord.z} " +
                        "dx=$deltaX dz=$deltaZ dir=$direction ext=$hasExt",
                )
                npcIdByIndex[npcIndex] = displayTypeId
                stillKnown.add(npcIndex)
                if (hasExt) extInfoQueue.add(npcIndex)
            }
        }

        knownNpcIndices.clear()
        knownNpcIndices.addAll(stillKnown)

        for (npcIndex in extInfoQueue) {
            try {
                if (buffer.readableBytes() < 2) {
                    sb.append("\n  [truncated: expected ext-info block for ${npcLabel(npcIndex)}, ${buffer.readableBytes()}b left]")
                    break
                }
                val size = buffer.g2()
                sb.append("\n  EXT ${npcLabel(npcIndex)} blockSize=$size ")
                sb.append(decodeExtendedInfoBlock(buffer, size))
            } catch (e: ExtendedInfoBlockException) {
                sb.append("\n  EXT-PARTIAL ${npcLabel(npcIndex)} ${e.partial}")
                sb.append(
                    "\n  [stopping ext-info for the rest of this packet - reader position now " +
                        "unreliable (known-list state above is still valid)]",
                )
                break
            } catch (e: Exception) {
                sb.append(
                    "\n  [ext-info decode error for ${npcLabel(npcIndex)}: ${e.javaClass.simpleName}: " +
                        "${e.message} - reader position now unreliable, stopping ext-info for " +
                        "this packet (known-list state above is still valid)]",
                )
                break
            }
        }
        return sb.toString()
    }

    override public fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): NpcInfo =
        try {
            decodeTypedOrThrow(buffer, baseCoord)
        } catch (e: Exception) {
            knownNpcIndices.clear()
            npcIdByIndex.clear()
            npcCoordByIndex.clear()
            NpcInfo(emptyMap())
        }

    private fun decodeTypedOrThrow(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): NpcInfo {
        val updates = mutableMapOf<Int, NpcUpdateType>()
        val stillKnown: ArrayList<Int>
        val extInfoQueue = ArrayList<Int>(4)

        buffer.buffer.toBitBuf().use { bits ->
            val knownCount = bits.gBits(8)
            stillKnown = ArrayList(knownCount)

            for (i in 0 until knownCount) {
                val npcIndex = knownNpcIndices.getOrNull(i)
                if (bits.gBits(1) == 0) {
                    if (npcIndex != null) {
                        stillKnown.add(npcIndex)
                        updates[npcIndex] = NpcUpdateType.Idle
                    }
                    continue
                }
                when (bits.gBits(2)) {
                    3 -> {
                        if (npcIndex != null) {
                            npcIdByIndex.remove(npcIndex)
                            npcCoordByIndex.remove(npcIndex)
                            updates[npcIndex] = NpcUpdateType.Remove
                        }
                    }
                    2 -> {
                        val isTrueRun = bits.gBits(1) != 0
                        if (isTrueRun) {
                            val dir1 = bits.gBits(3)
                            val dir2 = bits.gBits(3)
                            val hasExt = bits.gBits(1) != 0
                            if (npcIndex != null) {
                                val coord = resolveActive(npcIndex, baseCoord, dir1, dir2)
                                stillKnown.add(npcIndex)
                                if (hasExt) extInfoQueue.add(npcIndex)
                                updates[npcIndex] =
                                    NpcUpdateType.Active(
                                        NpcUpdateType.MovementType.RUN,
                                        dir1,
                                        dir2,
                                        coord.level,
                                        coord.x,
                                        coord.z,
                                        emptyList(),
                                    )
                            }
                        } else {
                            val dir = bits.gBits(3)
                            val hasExt = bits.gBits(1) != 0
                            if (npcIndex != null) {
                                val coord = resolveActive(npcIndex, baseCoord, dir, null)
                                stillKnown.add(npcIndex)
                                if (hasExt) extInfoQueue.add(npcIndex)
                                updates[npcIndex] =
                                    NpcUpdateType.Active(
                                        NpcUpdateType.MovementType.STEP_ALT,
                                        dir,
                                        null,
                                        coord.level,
                                        coord.x,
                                        coord.z,
                                        emptyList(),
                                    )
                            }
                        }
                    }
                    1 -> {
                        val walkDir = bits.gBits(3)
                        val hasExt = bits.gBits(1) != 0
                        if (npcIndex != null) {
                            val coord = resolveActive(npcIndex, baseCoord, walkDir, null)
                            stillKnown.add(npcIndex)
                            if (hasExt) extInfoQueue.add(npcIndex)
                            updates[npcIndex] =
                                NpcUpdateType.Active(
                                    NpcUpdateType.MovementType.WALK,
                                    walkDir,
                                    null,
                                    coord.level,
                                    coord.x,
                                    coord.z,
                                    emptyList(),
                                )
                        }
                    }
                    0 -> {
                        if (npcIndex != null) {
                            val coord = npcCoordByIndex[npcIndex] ?: baseCoord
                            stillKnown.add(npcIndex)
                            extInfoQueue.add(npcIndex)
                            updates[npcIndex] =
                                NpcUpdateType.Active(
                                    NpcUpdateType.MovementType.EXT_ONLY,
                                    null,
                                    null,
                                    coord.level,
                                    coord.x,
                                    coord.z,
                                    emptyList(),
                                )
                        }
                    }
                }
            }

            while (bits.readableBits() > 15) {
                val npcIndex = bits.gBits(16)
                if (npcIndex == 0xFFFF) break
                val rawDx = bits.gBits(COORD_BIT_WIDTH)
                val deltaX = if (rawDx >= COORD_HALF) rawDx - (1 shl COORD_BIT_WIDTH) else rawDx
                val displayTypeId = bits.gBits(16)
                val hasExt = bits.gBits(1) != 0
                val level = bits.gBits(2)
                val direction = bits.gBits(3)
                val rawDz = bits.gBits(COORD_BIT_WIDTH)
                val deltaZ = if (rawDz >= COORD_HALF) rawDz - (1 shl COORD_BIT_WIDTH) else rawDz
                bits.gBits(1)

                val coord = resolveAdd(npcIndex, baseCoord, level, deltaX, deltaZ)
                npcIdByIndex[npcIndex] = displayTypeId
                stillKnown.add(npcIndex)
                if (hasExt) extInfoQueue.add(npcIndex)
                updates[npcIndex] =
                    NpcUpdateType.Add(
                        displayTypeId,
                        deltaX,
                        deltaZ,
                        level,
                        coord.x,
                        coord.z,
                        direction,
                        emptyList(),
                    )
            }
        }

        knownNpcIndices.clear()
        knownNpcIndices.addAll(stillKnown)

        extInfoLoop@ for (npcIndex in extInfoQueue) {
            if (buffer.readableBytes() < 2) break@extInfoLoop
            val rendered =
                try {
                    val size = buffer.g2()
                    decodeExtendedInfoBlockTyped(buffer, size)
                } catch (e: Exception) {
                    break@extInfoLoop
                }
            when (val existing = updates[npcIndex]) {
                is NpcUpdateType.Active -> updates[npcIndex] = existing.copy(extendedInfo = rendered)
                is NpcUpdateType.Add -> updates[npcIndex] = existing.copy(extendedInfo = rendered)
                else -> Unit
            }
        }

        return NpcInfo(updates)
    }

    private fun decodeExtendedInfoBlockTyped(
        r: JagByteBuf,
        blockSize: Int,
    ): List<NpcExtendedInfo> {
        val startRemaining = r.readableBytes()
        val mask = r.gExpandableMask(Rs3NpcUpdateMaskKey.EXPANSION_BITS)
        val parts = mutableListOf<NpcExtendedInfo>()
        try {
            for (key in Rs3NpcUpdateMaskKey.byOrder) {
                if ((mask and (1L shl key.bit)) == 0L) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
                if (key !in Rs3NpcUpdateMaskKey.DECODABLE) {
                    val remainingInBlock = blockSize - consumedSoFar
                    val hex = ByteBufUtil.hexDump(r.buffer, r.buffer.readerIndex(), remainingInBlock)
                    parts += OpaqueExtendedInfo("${key.name}=<unconfirmed, ${remainingInBlock}b, hex=$hex>")
                    r.skipRead(remainingInBlock)
                    break
                }
                parts += decodeKnownBlock(key, r)
            }
        } catch (e: Exception) {
            val partial = parts.joinToString(" ") { it.toString() } + " <FAILED mid-block: ${e.javaClass.simpleName}: ${e.message}>"
            throw ExtendedInfoBlockException(partial, e)
        }
        val consumed = startRemaining - r.readableBytes()
        if (consumed < blockSize) r.skipRead(blockSize - consumed)
        return parts
    }

    private fun decodeExtendedInfoBlock(
        r: JagByteBuf,
        blockSize: Int,
    ): String {
        val startRemaining = r.readableBytes()
        val mask = r.gExpandableMask(Rs3NpcUpdateMaskKey.EXPANSION_BITS)
        val parts = mutableListOf("mask=0x${mask.toString(16)}")
        try {
            for (key in Rs3NpcUpdateMaskKey.byOrder) {
                if ((mask and (1L shl key.bit)) == 0L) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
                if (key !in Rs3NpcUpdateMaskKey.DECODABLE) {
                    val remainingInBlock = blockSize - consumedSoFar
                    val hex = ByteBufUtil.hexDump(r.buffer, r.buffer.readerIndex(), remainingInBlock)
                    parts += "${key.name}=<unconfirmed, ${remainingInBlock}b, hex=$hex>"
                    r.skipRead(remainingInBlock)
                    break
                }
                parts += decodeKnownBlock(key, r).toString()
            }
        } catch (e: Exception) {
            parts += "<FAILED mid-block: ${e.javaClass.simpleName}: ${e.message}>"
            throw ExtendedInfoBlockException(parts.joinToString(" "), e)
        }
        val consumed = startRemaining - r.readableBytes()
        if (consumed < blockSize) r.skipRead(blockSize - consumed)
        return parts.joinToString(" ")
    }

    private fun decodeParamsList(
        r: JagByteBuf,
        label: String,
    ): String {
        val header = r.g2()
        val count = r.g1()
        val entries = mutableListOf<String>()
        for (i in 0 until count) {
            val type = r.g1()
            val key = r.g2()
            when (type) {
                0 -> entries += "key=$key int=${r.g4()}"
                1 -> entries += "key=$key long=${r.g8()}"
                2 -> entries += "key=$key string=\"${r.gjstr()}\""
                3 -> {
                    val b = r.g1()
                    val x = r.g4()
                    val y = r.g4()
                    val z = r.g4()
                    entries += "key=$key b=$b vec=($x,$y,$z)"
                }
                else -> {
                    entries += "key=$key type=$type <unrecognized type, cannot determine length, bailing>"
                    break
                }
            }
        }
        return "$label(header=$header entries=[${entries.joinToString(",")}])"
    }

    private fun decodeUnk33(r: JagByteBuf): String {
        val count = r.g1Alt1().toByte().toInt()
        if (count <= 0) return "UNK_BIT33(count=$count)"
        val entries = mutableListOf<String>()
        repeat(count) {
            val flags = r.g2()
            val secondary = r.g2Alt3()
            val extra = if ((flags and 0xC00) != 0) r.g4() else null
            val f1 = if ((flags and 0x1) != 0) r.g4Alt2() else null
            val f2 = if ((flags and 0x2) != 0) r.g4Alt2() else null
            val f4 = if ((flags and 0x4) != 0) r.g4Alt1() else null
            val f8 = if ((flags and 0x8) != 0) r.g4Alt2() else null
            val f10 = if ((flags and 0x10) != 0) r.g4Alt3() else null
            val f20 = if ((flags and 0x20) != 0) r.g4Alt1() else null
            val f80 = if ((flags and 0x80) != 0) r.g4() else null
            val f100 = if ((flags and 0x100) != 0) r.g4() else null
            val f200 = if ((flags and 0x200) != 0) r.g4Alt1() else null
            entries +=
                "flags=0x${flags.toString(16)} sec=$secondary extra=$extra f1=$f1 f2=$f2 " +
                    "f4=$f4 f8=$f8 f10=$f10 f20=$f20 f80=$f80 f100=$f100 f200=$f200"
        }
        return "UNK_BIT33(count=$count entries=[${entries.joinToString(",")}])"
    }

    private class ExtendedInfoBlockException(val partial: String, cause: Exception) : Exception(cause)

    private fun decodeKnownBlock(
        key: Rs3NpcUpdateMaskKey,
        r: JagByteBuf,
    ): NpcExtendedInfo =
        when (key) {
            Rs3NpcUpdateMaskKey.FORCED_MOVEMENT -> {
                val d1 = r.g1s()
                val d2 = r.g1Alt3()
                val d3 = r.g1s()
                val d4 = r.g1s()
                val d5 = r.g1Alt1()
                val d6 = r.g1Alt2()
                val v1 = r.g2()
                val v2 = r.g2Alt2()
                val v3 = r.g2Alt2()
                OpaqueExtendedInfo("FORCED_MOVEMENT?(d1=$d1 d2=$d2 d3=$d3 d4=$d4 d5=$d5 d6=$d6 v1=$v1 v2=$v2 v3=$v3)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT8 -> {
                r.g2Alt1()
                r.g4()
                r.g1Alt2()
                OpaqueExtendedInfo("UNK_BIT8(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT27 -> {
                r.g2()
                r.g4Alt2()
                r.g1Alt2()
                OpaqueExtendedInfo("UNK_BIT27(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT13 -> {
                r.g1()
                r.g1Alt2()
                r.g2Alt3()
                OpaqueExtendedInfo("UNK_BIT13(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT34 -> {
                val v = r.g1Alt3()
                OpaqueExtendedInfo("UNK_BIT34(v=$v${if (v == -128) " CLEAR" else ""})")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT29 -> {
                r.g2()
                r.g4Alt1()
                r.g1Alt2()
                OpaqueExtendedInfo("UNK_BIT29(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT24 -> {
                r.g2Alt3()
                r.g4Alt3()
                r.g1()
                OpaqueExtendedInfo("UNK_BIT24(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT12 -> {
                r.g1()
                r.g2()
                r.g2Alt1()
                r.g2()
                OpaqueExtendedInfo("UNK_BIT12(consumed, discarded by client)")
            }
            Rs3NpcUpdateMaskKey.UNK_BIT21 -> {
                val flags = r.g1Alt2()
                val parts =
                    (0 until 8).joinToString(",") { i ->
                        if ((flags and (1 shl i)) != 0) {
                            "${r.gSmart2or4null()}/${r.gSmartSigned()}"
                        } else {
                            "-"
                        }
                    }
                OpaqueExtendedInfo("UNK_BIT21(flags=0x${flags.toString(16)} [$parts])")
            }
            Rs3NpcUpdateMaskKey.SPOT_ANIM_LIST_BIT19 -> OpaqueExtendedInfo(decodeParamsList(r, "PARAMS_BIT19"))
            Rs3NpcUpdateMaskKey.SPOT_ANIM_LIST_BIT23 -> OpaqueExtendedInfo(decodeParamsList(r, "PARAMS_BIT23"))
            Rs3NpcUpdateMaskKey.UNK_BIT33 -> OpaqueExtendedInfo(decodeUnk33(r))
            Rs3NpcUpdateMaskKey.VISIBILITY_FLAG -> OpaqueExtendedInfo("VISIBILITY_FLAG(value=${r.g1Alt3()})")
            Rs3NpcUpdateMaskKey.TRANSIENT_BOOL -> OpaqueExtendedInfo("TRANSIENT_BOOL(${r.g1Alt1() == 1})")
            Rs3NpcUpdateMaskKey.UNK_BIT1 -> OpaqueExtendedInfo("UNK_BIT1(v=${r.gSmart2or4null()})")
            Rs3NpcUpdateMaskKey.MODEL_OVERRIDE_ID -> OpaqueExtendedInfo("MODEL_OVERRIDE_ID(${r.g2()})")
            Rs3NpcUpdateMaskKey.FACE_ENTITY -> {
                val packed = r.g3()
                val kind = (packed shr 16) and 0xFF
                val index = packed and 0xFFFF
                val kindLabel =
                    when (kind) {
                        1 -> "NPC"
                        2 -> "PLAYER"
                        255 -> "NONE"
                        else -> "UNK_$kind"
                    }
                OpaqueExtendedInfo("FACE_ENTITY(kind=$kindLabel index=$index)")
            }
            Rs3NpcUpdateMaskKey.TRACKED_FACE_LOCK -> OpaqueExtendedInfo("TRACKED_FACE_LOCK(index=${r.g2Alt1()})")
            Rs3NpcUpdateMaskKey.COMBAT_LEVEL_OVERRIDE_RGB -> {
                val hue = r.g1Alt1()
                val sat = r.g1Alt1()
                val lum = r.g1Alt2()
                val brightness = r.g1Alt1()
                val startCycle = r.g2Alt3()
                val endCycle = r.g2()
                OpaqueExtendedInfo("COMBAT_LEVEL_OVERRIDE_RGB(hue=$hue sat=$sat lum=$lum bright=$brightness cycle=$startCycle..$endCycle)")
            }
            Rs3NpcUpdateMaskKey.NAME_OVERRIDE -> OpaqueExtendedInfo("NAME_OVERRIDE(\"${r.gjstr()}\")")
            Rs3NpcUpdateMaskKey.NPC_STATS -> {
                val count = r.g1()
                val entries =
                    (0 until count).joinToString(",") {
                        val slot = r.g1()
                        val current = r.g4Alt3()
                        val m0 = r.g1()
                        val m1 = r.g1()
                        val m2 = r.g1()
                        val max = (m1 shl 16) or (m0 shl 8) or m2
                        "${statName(slot)} cur=$current max=$max"
                    }
                OpaqueExtendedInfo("NPC_STATS[$entries]")
            }
            Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS -> OpaqueExtendedInfo(decodeHitmarksAndHeadbars(r, "HITMARKS_AND_HEADBARS"))
            Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_2 -> OpaqueExtendedInfo(decodeWideHitmarksAndHeadbars(r, "HITMARKS_AND_HEADBARS_2"))
            Rs3NpcUpdateMaskKey.ANIMATION -> {
                val layer0 = r.gSmart2or4null()
                r.gSmart2or4null()
                r.gSmart2or4null()
                r.gSmart2or4null()
                val speed = r.g1Alt1()
                AnimationExtendedInfo(layer0, speed)
            }
            Rs3NpcUpdateMaskKey.FACE_TILE -> {
                val x = (r.g2() - 1) / 2
                val z = (r.g2() - 1) / 2
                OpaqueExtendedInfo("FACE_TILE(x=$x z=$z)")
            }
            Rs3NpcUpdateMaskKey.STRING_OVERRIDE -> OpaqueExtendedInfo("NPC_SAY(\"${r.gjstr()}\")")
            else -> error("unreachable: $key is not in Rs3NpcUpdateMaskKey.DECODABLE")
        }

    private fun decodeHitmarksAndHeadbars(
        r: JagByteBuf,
        label: String,
    ): String {
        val hitCount = r.g1Alt3()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.gSmart1or2()
                            val c = r.gSmart1or2()
                            val d = r.gSmart1or2()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1Alt2()})"
                        else -> "normal(v1=$v1 a=${r.gSmart1or2()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1()
        val bars = (0 until barCount).joinToString(",") { decodeHeadbarNarrow(r) }
        return "$label(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarNarrow(r: JagByteBuf): String {
        val id = r.gSmart1or2()
        val cycles = r.gSmart1or2()
        if (cycles == 0x7fff) return "id=$id REMOVE"
        val unknownB = r.gSmart1or2()
        val primaryFill = r.g1Alt1()
        val secondaryFill: Int
        val delay: Int
        val extra1: Int
        val extra2: Int
        if (cycles == 0) {
            secondaryFill = primaryFill
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1Alt2()
                extra2 = extra1
            }
        } else {
            secondaryFill = r.g1Alt1()
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1Alt2()
                extra2 = r.g1()
            }
        }
        return "id=$id cycles=$cycles unk=$unknownB fill=$primaryFill,$secondaryFill delay=$delay extra=$extra1,$extra2"
    }

    private fun decodeWideHitmarksAndHeadbars(
        r: JagByteBuf,
        label: String,
    ): String {
        val hitCount = r.g1()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.g4Alt3()
                            val c = r.gSmart1or2()
                            val d = r.g4Alt2()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1Alt3()})"
                        else -> "normal(v1=$v1 a=${r.g4Alt3()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1Alt2()
        val bars = (0 until barCount).joinToString(",") { decodeHeadbarWide(r) }
        return "$label(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarWide(r: JagByteBuf): String {
        val id = r.gSmart1or2()
        val cycles = r.gSmart1or2()
        if (cycles == 0x7fff) return "id=$id REMOVE"
        val unknownB = r.gSmart1or2()
        val primaryFill = r.g1Alt3()
        val secondaryFill: Int
        val delay: Int
        val extra1: Int
        val extra2: Int
        if (cycles == 0) {
            secondaryFill = primaryFill
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1()
                extra2 = extra1
            }
        } else {
            secondaryFill = r.g1()
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1()
                extra2 = r.g1Alt2()
            }
        }
        return "id=$id cycles=$cycles unk=$unknownB fill=$primaryFill,$secondaryFill delay=$delay extra=$extra1,$extra2"
    }

    private companion object {
        const val COORD_BIT_WIDTH = 7
        const val COORD_HALF = 1 shl (COORD_BIT_WIDTH - 1)

        val NPC_DIR_DELTA_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        val NPC_DIR_DELTA_Z = intArrayOf(1, 1, 1, 0, 0, -1, -1, -1)

        val STAT_NAMES =
            arrayOf(
                "ATTACK", "DEFENCE", "STRENGTH", "HITPOINTS", "RANGED", "PRAYER", "MAGIC",
                "COOKING", "WOODCUTTING", "FLETCHING", "FISHING", "FIREMAKING", "CRAFTING",
                "SMITHING", "MINING", "HERBLORE", "AGILITY", "THIEVING", "SLAYER", "FARMING",
                "RUNECRAFTING", "HUNTER", "CONSTRUCTION", "SUMMONING", "DUNGEONEERING",
                "DIVINATION", "INVENTION", "ARCHAEOLOGY", "NECROMANCY",
            )

        fun statName(slot: Int): String = STAT_NAMES.getOrNull(slot) ?: "STAT_$slot"
    }
}

private fun JagByteBuf.gSmartSigned(): Int {
    val peek = buffer.getByte(buffer.readerIndex())
    return if (peek >= 0) {
        g1() - 1
    } else {
        g2() + 0x7fff
    }
}

private fun JagByteBuf.gExpandableMask(expansionBits: IntArray): Long {
    var mask = 0L
    var byteIndex = 0
    while (true) {
        val byteVal = g1().toLong()
        mask = mask or (byteVal shl (byteIndex * 8))
        val contBit = expansionBits.getOrNull(byteIndex)
        val continues = contBit != null && (mask and (1L shl contBit)) != 0L
        byteIndex++
        if (!continues) break
    }
    return mask
}
