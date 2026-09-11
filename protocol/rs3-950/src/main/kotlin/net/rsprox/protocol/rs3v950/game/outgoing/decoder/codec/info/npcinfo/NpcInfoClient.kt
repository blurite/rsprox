package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo

import io.netty.buffer.ByteBufUtil
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.AnimationExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.OpaqueExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

public class NpcInfoClient : NpcInfoDecoder, ProxyMessageDecoder<NpcInfo> {
    override val prot: ClientProt = GameServerProt.NPC_INFO_V2

    public var baseCoord: CoordGrid = CoordGrid.INVALID
    private val knownNpcIndices = mutableListOf<Int>()
    private val npcIdByIndex = mutableMapOf<Int, Int>()
    private val npcCoordByIndex = mutableMapOf<Int, CoordGrid>()

    override public fun lookupNpcId(index: Int): Int? = npcIdByIndex[index]

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcInfo = decode(buffer, baseCoord)

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
            val diag = "NPC_INFO_V2 <decode FAILED: ${e.javaClass.simpleName}: ${e.message}> " +
                "len=$originalReadable trackedKnown=${knownNpcIndices.size} hex=[$hexDump]"
            knownNpcIndices.clear()
            npcIdByIndex.clear()
            npcCoordByIndex.clear()
            diag
        }
    }

    private fun step(coord: CoordGrid, direction: Int): CoordGrid {
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
        val sb = StringBuilder("NPC_INFO_V2")
        val stillKnown: ArrayList<Int>
        val extInfoQueue = ArrayList<Int>(4)

        buffer.buffer.toBitBuf().use { bits ->
            val knownCount = bits.gBits(8)
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
                                sb.append("\n  RUN ${npcLabel(npcIndex)} dir1=$dir1 dir2=$dir2 level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt")
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
                                sb.append("\n  STEP_ALT ${npcLabel(npcIndex)} dir=$dir level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt")
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
                            sb.append("\n  WALK ${npcLabel(npcIndex)} dir=$walkDir level=${coord.level} x=${coord.x} z=${coord.z} ext=$hasExt")
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
                val rawDz = bits.gBits(COORD_BIT_WIDTH)
                val deltaZ = if (rawDz >= COORD_HALF) rawDz - (1 shl COORD_BIT_WIDTH) else rawDz
                bits.gBits(1)
                val direction = bits.gBits(3)
                val displayTypeId = bits.gBits(16)
                val level = bits.gBits(2)
                val hasExt = bits.gBits(1) != 0
                val rawDx = bits.gBits(COORD_BIT_WIDTH)
                val deltaX = if (rawDx >= COORD_HALF) rawDx - (1 shl COORD_BIT_WIDTH) else rawDx

                val coord = resolveAdd(npcIndex, baseCoord, level, deltaX, deltaZ)
                sb.append("\n  ADD npc=$npcIndex id=$displayTypeId level=${coord.level} x=${coord.x} z=${coord.z} dx=$deltaX dz=$deltaZ dir=$direction ext=$hasExt")
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
                sb.append("\n  [stopping ext-info for the rest of this packet]")
                break
            } catch (e: Exception) {
                sb.append("\n  [ext-info decode error for ${npcLabel(npcIndex)}: ${e.javaClass.simpleName}: ${e.message}]")
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
                val rawDz = bits.gBits(COORD_BIT_WIDTH)
                val deltaZ = if (rawDz >= COORD_HALF) rawDz - (1 shl COORD_BIT_WIDTH) else rawDz
                bits.gBits(1)
                val direction = bits.gBits(3)
                val displayTypeId = bits.gBits(16)
                val level = bits.gBits(2)
                val hasExt = bits.gBits(1) != 0
                val rawDx = bits.gBits(COORD_BIT_WIDTH)
                val deltaX = if (rawDx >= COORD_HALF) rawDx - (1 shl COORD_BIT_WIDTH) else rawDx

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
        val mask = r.gExpandableMask(EXPANSION_BITS)
        val parts = mutableListOf<NpcExtendedInfo>()
        try {
            for (key in Rs3NpcUpdateMaskKey.byOrder) {
                if ((mask and (1L shl key.bit)) == 0L) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
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
        val mask = r.gExpandableMask(EXPANSION_BITS)
        val parts = mutableListOf("mask=0x${mask.toString(16)}")
        try {
            for (key in Rs3NpcUpdateMaskKey.byOrder) {
                if ((mask and (1L shl key.bit)) == 0L) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
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
                    entries += "key=$key type=$type <unrecognized type>"
                    break
                }
            }
        }
        return "$label(header=$header entries=[${entries.joinToString(",")}])"
    }

    private fun decodeBoneTransforms(r: JagByteBuf): String {
        val count = r.g1Alt2()
        if (count <= 0) return "BONE_TRANSFORMS(count=$count CLEAR)"
        val entries = mutableListOf<String>()
        repeat(count) {
            val flags = r.g2Alt3()
            val boneId = r.g2()
            val extra = if ((flags and 0xC00) != 0) r.g4Alt3() else null
            val f1 = if ((flags and 0x1) != 0) r.g4Alt3() else null
            val f2 = if ((flags and 0x2) != 0) r.g4() else null
            val f4 = if ((flags and 0x4) != 0) r.g4Alt1() else null
            val f8 = if ((flags and 0x8) != 0) r.g4Alt3() else null
            val f10 = if ((flags and 0x10) != 0) r.g4Alt2() else null
            val f20 = if ((flags and 0x20) != 0) r.g4Alt2() else null
            val f80 = if ((flags and 0x80) != 0) r.g4Alt3() else null
            val f100 = if ((flags and 0x100) != 0) r.g4Alt1() else null
            val f200 = if ((flags and 0x200) != 0) r.g4Alt2() else null
            entries += "bone=$boneId flags=0x${flags.toString(16)} extra=$extra t=($f1,$f2,$f4) r=($f8,$f10,$f20) s=($f80,$f100,$f200)"
        }
        return "BONE_TRANSFORMS(count=$count entries=[${entries.joinToString(",")}])"
    }

    private fun decodeSpotAnimList(r: JagByteBuf): String {
        val removeCount = r.g1()
        val removes = (0 until removeCount).map { r.g2s() }
        val addCount = r.g1Alt1()
        val adds =
            (0 until addCount).map {
                val slot = r.g1()
                val id = r.g2Alt2()
                val delayHeight = r.g4Alt3()
                val rot = r.g1Alt3()
                val fine = r.g3Alt3()
                "slot=$slot id=$id dh=$delayHeight rot=$rot fine=$fine"
            }
        return "SPOT_ANIMS(removes=$removes adds=[${adds.joinToString(",")}])"
    }

    private fun decodeCustomChannels(r: JagByteBuf): String {
        val flags = r.g1()
        val channels =
            (0 until 8).mapNotNull { i ->
                if ((flags and (1 shl i)) != 0) {
                    val f = r.g4f()
                    val s = r.gSmartSigned()
                    "ch$i=($f,$s)"
                } else null
            }
        return "CUSTOM_CHANNELS(flags=0x${flags.toString(16)} [${channels.joinToString(",")}])"
    }

    private fun decodeBodyCustomisation(r: JagByteBuf): String {
        val flags = r.g1Alt1()
        if ((flags and 0x1) != 0) {
            return "BODY_CUSTOMISATION(RESET)"
        }

        val parts = mutableListOf("flags=0x${flags.toString(16)}")

        if ((flags and 0x2) != 0) {
            val count = r.g1Alt3()
            val modelEntries = mutableListOf<String>()
            for (i in 0 until count) {
                val modelId = r.gSmart2or4null()
                if (modelId != -1 && (flags and 0x10) != 0) {
                    val boneId = r.g4()
                    val transX = r.g2sAlt1()
                    val transY = r.g2sAlt1()
                    val transZ = r.g2sAlt1()
                    val rotX = r.g2s()
                    val rotY = r.g2s()
                    val rotZ = r.g2s()
                    modelEntries += "model=$modelId bone=$boneId t=($transX,$transY,$transZ) r=($rotX,$rotY,$rotZ)"
                } else {
                    modelEntries += "model=$modelId"
                }

                if ((flags and 0x20) != 0 && modelId != -1) {
                    val recolourCount = r.g1Alt1()
                    val recolours = (0 until recolourCount).map { r.g2Alt2() }
                    modelEntries += "recolours=$recolours"
                }

                if ((flags and 0x40) != 0 && modelId != -1) {
                    val retextureCount = r.g1Alt3()
                    val retextures = (0 until retextureCount).map { r.g2Alt1() }
                    modelEntries += "retextures=$retextures"
                }
            }
            parts += "models=[${modelEntries.joinToString(", ")}]"
        }

        if ((flags and 0x4) != 0) {
            val count = r.g1()
            val globalRecolours = (0 until count).map { r.g2() }
            parts += "globalRecolours=$globalRecolours"
        }

        if ((flags and 0x8) != 0) {
            val count = r.g1()
            val globalRetextures = (0 until count).map { r.g2Alt2() }
            parts += "globalRetextures=$globalRetextures"
        }

        if ((flags and 0x80) != 0) {
            val headBytes = (0 until 10).map { r.g1Alt2() }
            parts += "headBuffer=$headBytes"
        }

        return "BODY_CUSTOMISATION(${parts.joinToString(" ")})"
    }

    private fun decodeModelOverride(r: JagByteBuf): String {
        val flags = r.g1Alt2()
        if ((flags and 0x1) != 0) return "HEAD_CUSTOMISATION(RESET)"
        val parts = mutableListOf("flags=0x${flags.toString(16)}")
        if ((flags and 2) != 0) {
            val count = r.g1Alt2()
            val models = (0 until count).map { r.gSmart2or4null() }
            parts += "models=$models"
        }
        if ((flags and 4) != 0) {
            val recolourCount = r.g1()
            val recolours = (0 until recolourCount).map { r.g2Alt3() }
            parts += "recolours=$recolours"
        }
        if ((flags and 8) != 0) {
            val retextureCount = r.g1()
            val retextures = (0 until retextureCount).map { r.g2Alt2() }
            parts += "retextures=$retextures"
        }
        return "HEAD_CUSTOMISATION(${parts.joinToString(" ")})"
    }

    private class ExtendedInfoBlockException(val partial: String, cause: Exception) : Exception(cause)

    private fun decodeKnownBlock(
        key: Rs3NpcUpdateMaskKey,
        r: JagByteBuf,
    ): NpcExtendedInfo =
        when (key) {
            Rs3NpcUpdateMaskKey.SAY -> OpaqueExtendedInfo("SAY(\"${r.gjstr()}\")")
            Rs3NpcUpdateMaskKey.NPC_FREEZE -> {
                val delay = r.g2Alt1()
                val duration = r.g4()
                val cancelSequence = r.g1Alt2() == 1
                OpaqueExtendedInfo("FREEZE(delay=$delay duration=$duration cancelSequence=$cancelSequence)")
            }
            Rs3NpcUpdateMaskKey.TRANSPARENCY -> {
                val alpha = r.g1sAlt1()
                OpaqueExtendedInfo("TRANSPARENCY(alpha=$alpha${if (alpha == -128) " RESET" else ""})")
            }
            Rs3NpcUpdateMaskKey.TRACKED_FACE_LOCK -> OpaqueExtendedInfo("TRACKED_FACE_LOCK(index=${r.g2()})")
            Rs3NpcUpdateMaskKey.HEAD_CUSTOMISATION -> OpaqueExtendedInfo(decodeModelOverride(r))
            Rs3NpcUpdateMaskKey.TRANSIENT_BOOL -> OpaqueExtendedInfo("TRANSIENT_BOOL(${r.g1() == 1})")
            Rs3NpcUpdateMaskKey.TIMED_EFFECT_1 -> {
                val v1 = r.g2()
                val v2 = r.g4()
                val v3 = r.g1()
                OpaqueExtendedInfo("TIMED_EFFECT_1(v1=$v1 v2=$v2 v3=$v3)")
            }
            Rs3NpcUpdateMaskKey.TINTING -> {
                val hue = r.g1Alt1()
                val sat = r.g1Alt1()
                val lum = r.g1Alt1()
                val brightness = r.g1()
                val startCycle = r.g2Alt1()
                val endCycle = r.g2Alt1()
                OpaqueExtendedInfo("TINTING(hue=$hue sat=$sat lum=$lum bright=$brightness cycle=$startCycle..$endCycle)")
            }
            Rs3NpcUpdateMaskKey.TRANSFORMATION -> OpaqueExtendedInfo("TRANSFORMATION(id=${r.gSmart2or4null()})")
            Rs3NpcUpdateMaskKey.EXACT_MOVE -> {
                val d1 = r.g1s()
                val d2 = r.g1s()
                val d3 = r.g1s()
                val d4 = r.g1s()
                val d5 = r.g1sAlt2()
                val d6 = r.g1sAlt2()
                val v1 = r.g2()
                val v2 = r.g2()
                val v3 = r.g2Alt2()
                OpaqueExtendedInfo("EXACT_MOVE(d1=$d1 d2=$d2 d3=$d3 d4=$d4 d5=$d5 d6=$d6 v1=$v1 v2=$v2 v3=$v3)")
            }
            Rs3NpcUpdateMaskKey.CONFIG_PARAMS_2 -> OpaqueExtendedInfo(decodeParamsList(r, "CONFIG_PARAMS_2"))
            Rs3NpcUpdateMaskKey.SECONDARY_FREEZE -> {
                val v1 = r.g2Alt1()
                val v2 = r.g4()
                val v3 = r.g1Alt2()
                OpaqueExtendedInfo("SECONDARY_FREEZE(v1=$v1 v2=$v2 v3=$v3)")
            }
            Rs3NpcUpdateMaskKey.CONFIG_PARAMS_1 -> OpaqueExtendedInfo(decodeParamsList(r, "CONFIG_PARAMS_1"))
            Rs3NpcUpdateMaskKey.NPC_STATS -> {
                val count = r.g1()
                val entries =
                    (0 until count).joinToString(",") {
                        val slot = r.g1Alt1()
                        val current = r.g4Alt1()
                        val max = r.g3Alt2()
                        "${statName(slot)} cur=$current max=$max"
                    }
                OpaqueExtendedInfo("NPC_STATS[$entries]")
            }
            Rs3NpcUpdateMaskKey.NAME_CHANGE -> OpaqueExtendedInfo("NAME_CHANGE(\"${r.gjstr()}\")")
            Rs3NpcUpdateMaskKey.ENABLED_OPS -> {
                val v1 = r.g1Alt3()
                val v2 = r.g1()
                val opMask = r.g2()
                OpaqueExtendedInfo("ENABLED_OPS(v1=$v1 v2=$v2 opMask=0x${opMask.toString(16)})")
            }
            Rs3NpcUpdateMaskKey.FACE_ENTITY -> {
                val packed = r.g3Alt3()
                val kind = (packed shr 16) and 0xFF
                val index = packed and 0xFFFF
                val kindLabel = when (kind) {
                    1 -> "NPC"
                    2 -> "PLAYER"
                    255 -> "NONE"
                    else -> "UNK_$kind"
                }
                OpaqueExtendedInfo("FACE_ENTITY(kind=$kindLabel index=$index)")
            }
            Rs3NpcUpdateMaskKey.FACE_TILE -> {
                val x = (r.g2() - 1) / 2
                val z = (r.g2Alt1() - 1) / 2
                OpaqueExtendedInfo("FACE_TILE(x=$x z=$z)")
            }
            Rs3NpcUpdateMaskKey.TIMED_EFFECT_3 -> {
                val v1 = r.g2Alt3()
                val v2 = r.g4Alt3()
                val v3 = r.g1()
                OpaqueExtendedInfo("TIMED_EFFECT_3(v1=$v1 v2=$v2 v3=$v3)")
            }
            Rs3NpcUpdateMaskKey.SCALE_CHANGE -> {
                val flag = r.g1Alt1()
                val scaleX = r.g2Alt1()
                val scaleY = r.g2()
                val scaleZ = r.g2Alt2()
                OpaqueExtendedInfo("SCALE_CHANGE(flag=$flag scale=($scaleX, $scaleY, $scaleZ))")
            }
            Rs3NpcUpdateMaskKey.BONE_TRANSFORMS -> OpaqueExtendedInfo(decodeBoneTransforms(r))
            Rs3NpcUpdateMaskKey.BODY_CUSTOMISATION -> OpaqueExtendedInfo(decodeBodyCustomisation(r))
            Rs3NpcUpdateMaskKey.SPOTANIM -> OpaqueExtendedInfo(decodeSpotAnimList(r))
            Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_WIDE -> OpaqueExtendedInfo(decodeWideHitmarksAndHeadbars(r, "HITMARKS_AND_HEADBARS_WIDE"))
            Rs3NpcUpdateMaskKey.COMBAT_LEVEL_CHANGE -> {
                val level = r.g2Alt1()
                OpaqueExtendedInfo("COMBAT_LEVEL_CHANGE(${if (level == 0xFFFF) "RESET" else "level=$level"})")
            }
            Rs3NpcUpdateMaskKey.SEQUENCE -> {
                val layer0 = r.gSmart2or4null()
                val layer1 = r.gSmart2or4null()
                val layer2 = r.gSmart2or4null()
                val layer3 = r.gSmart2or4null()
                val speed = r.g1Alt1()
                AnimationExtendedInfo(layer0, speed)
            }
            Rs3NpcUpdateMaskKey.TINTING_CHANNELS -> OpaqueExtendedInfo(decodeCustomChannels(r))
            Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS -> OpaqueExtendedInfo(decodeHitmarksAndHeadbars(r, "HITMARKS_AND_HEADBARS_NARROW"))
            Rs3NpcUpdateMaskKey.VISIBILITY_FLAG -> OpaqueExtendedInfo("VISIBILITY_FLAG(value=${r.g1()})")
            Rs3NpcUpdateMaskKey.TIMED_EFFECT_2 -> {
                val v1 = r.g2Alt2()
                val v2 = r.g4Alt3()
                val v3 = r.g1Alt2()
                OpaqueExtendedInfo("TIMED_EFFECT_2(v1=$v1 v2=$v2 v3=$v3)")
            }
        }

    private fun decodeHitmarksAndHeadbars(
        r: JagByteBuf,
        label: String,
    ): String {
        val hitCount = r.g1Alt2()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.g4Alt2()
                            val c = r.gSmart1or2()
                            val d = r.g4Alt2()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1Alt2()})"
                        else -> "normal(v1=$v1 a=${r.gSmart1or2()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1Alt2()
        val bars = (0 until barCount).joinToString(",") { decodeHeadbarNarrow(r) }
        return "$label(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarNarrow(r: JagByteBuf): String {
        val id = r.gSmart1or2()
        val cycles = r.gSmart1or2()
        if (cycles == 0x7fff) return "id=$id REMOVE"
        val unknownB = r.gSmart1or2()
        val primaryFill = r.g1Alt2()
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
                extra1 = r.g1Alt1()
                extra2 = extra1
            }
        } else {
            secondaryFill = r.g1Alt2()
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1Alt1()
                extra2 = r.g1Alt3()
            }
        }
        return "id=$id cycles=$cycles unk=$unknownB fill=$primaryFill,$secondaryFill delay=$delay extra=$extra1,$extra2"
    }

    private fun decodeWideHitmarksAndHeadbars(
        r: JagByteBuf,
        label: String,
    ): String {
        val hitCount = r.g1Alt1()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.g4Alt1()
                            val c = r.gSmart1or2()
                            val d = r.g4Alt1()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1Alt1()})"
                        else -> "normal(v1=$v1 a=${r.g4Alt1()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1Alt1()
        val bars = (0 until barCount).joinToString(",") { decodeHeadbarWide(r) }
        return "$label(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarWide(r: JagByteBuf): String {
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
                extra1 = r.g1Alt1()
                extra2 = extra1
            }
        } else {
            secondaryFill = r.g1Alt1()
            delay = r.gSmartSigned()
            if (delay < 0) {
                extra1 = 0
                extra2 = 0
            } else {
                extra1 = r.g1Alt1()
                extra2 = r.g1Alt3()
            }
        }
        return "id=$id cycles=$cycles unk=$unknownB fill=$primaryFill,$secondaryFill delay=$delay extra=$extra1,$extra2"
    }

    private companion object {
        val EXPANSION_BITS = intArrayOf(4, 13, 23, 27)

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
