package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.ByteBufUtil
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.Rs3PlayerUpdateMaskKey
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

public class PlayerInfoClient(ownIndex: Int? = null) : PlayerInfoDecoder, ProxyMessageDecoder<PlayerInfo> {
    override val prot: ClientProt = GameServerProt.PLAYER_INFO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerInfo = decode(buffer)

    private val highRes = BooleanArray(MAX_SLOT + 1)
    private val nsn = BooleanArray(MAX_SLOT + 1) { true }
    private val playerNameByIndex = mutableMapOf<Int, String>()

    private var currentDecodingSlot: Int? = null

    private var ownIndex: Int? = null
    override public fun getOwnIndex(): Int? = ownIndex
    private val highResPlane = IntArray(MAX_SLOT + 1)

    public fun lookupPlayerName(index: Int): String? = playerNameByIndex[index]

    private fun slotLabel(slot: Int): String {
        val name = playerNameByIndex[slot]
        return if (name != null) "slot=$slot($name)" else "slot=$slot"
    }

    private fun formatCoord(level: Int, x: Int, z: Int): String {
        val mapSquareX = x ushr 6
        val mapSquareZ = z ushr 6
        val localX = x and 0x3F
        val localZ = z and 0x3F
        return "($level,$x,$z) jagex=${level}_${mapSquareX}_${mapSquareZ}_${localX}_${localZ}"
    }

    private val lowResCoords = IntArray(MAX_SLOT + 1)
    private val lowResSeeded = BooleanArray(MAX_SLOT + 1)

    private val highResX = IntArray(MAX_SLOT + 1)
    private val highResZ = IntArray(MAX_SLOT + 1)
    private val highResSeeded = BooleanArray(MAX_SLOT + 1)

    init {
        if (ownIndex != null) applyOwnIndex(ownIndex)
    }

    override public fun applyOwnIndex(index: Int) {
        if (index in 1..MAX_SLOT) {
            highRes[index] = true
            nsn[index] = false
            ownIndex = index
        }
    }

    override public fun gpiInit(initBlock: PlayerInfoInitBlock) {
        val idx = initBlock.localPlayerIndex
        applyOwnIndex(idx)
        if (idx in 1..MAX_SLOT) {
            highResX[idx] = initBlock.localPlayerX
            highResZ[idx] = initBlock.localPlayerZ
            highResPlane[idx] = initBlock.localPlayerLevel
            highResSeeded[idx] = true
        }
        for (slot in 1..MAX_SLOT) {
            if (slot == idx) continue
            lowResCoords[slot] = initBlock.getLowResolutionPosition(slot)
            lowResSeeded[slot] = true
        }
        gpiInitApplied = true
    }

    override public fun reset() {
        highRes.fill(false)
        nsn.fill(true)
        lowResCoords.fill(0)
        lowResSeeded.fill(false)
        highResX.fill(0)
        highResZ.fill(0)
        highResPlane.fill(0)
        highResSeeded.fill(false)
        playerNameByIndex.clear()
        gpiInitApplied = false
    }

    private var packetCounter = 0
    private var gpiInitApplied = false

    private fun hexDump(payload: ByteArray): String {
        val sb = StringBuilder("hexDump (${payload.size} bytes):")
        var i = 0
        while (i < payload.size) {
            val row = payload.copyOfRange(i, minOf(i + 16, payload.size))
            sb.append("\n  [%4d] ".format(i))
            sb.append(row.joinToString(" ") { "%02x".format(it) })
            i += 16
        }
        return sb.toString()
    }

    public fun decodeText(buffer: JagByteBuf): String {
        packetCounter++
        val payloadSnapshot = ByteBufUtil.getBytes(buffer.buffer, buffer.buffer.readerIndex(), buffer.readableBytes())
        return try {
            "PLAYER_INFO #$packetCounter\n${decodeOrThrow(buffer, payloadSnapshot.size)}\n${hexDump(payloadSnapshot)}"
        } catch (e: Exception) {
            "PLAYER_INFO <decode FAILED: ${e.javaClass.simpleName}: ${e.message}>\n${hexDump(payloadSnapshot)}"
        }
    }

    private class PassDecodeException(val partial: String, val passDiag: String, cause: Exception) : Exception(cause)

    private fun decodeOrThrow(
        buffer: JagByteBuf,
        totalBytes: Int,
    ): String {
        val sb = StringBuilder("PLAYER_INFO")
        val promote = mutableListOf<Int>()
        val demote = mutableListOf<Int>()
        val extInfoQueue = mutableListOf<Int>()
        val processedThisTick = BooleanArray(MAX_SLOT + 1)

        val hr0 = highResSlots(false)
        val hr1 = highResSlots(true)
        val lr1 = lowResSlots(true)
        val lr0 = lowResSlots(false)

        buffer.buffer.toBitBuf().use { bits ->
            runPass(bits, sb, hr0, isHighRes = true, "highRes/parity0", promote, demote, extInfoQueue, processedThisTick)
            bits.byteAlign()
            runPass(bits, sb, hr1, isHighRes = true, "highRes/parity1", promote, demote, extInfoQueue, processedThisTick)
            bits.byteAlign()
            runPass(bits, sb, lr1, isHighRes = false, "lowRes/parity1", promote, demote, extInfoQueue, processedThisTick)
            bits.byteAlign()
            runPass(bits, sb, lr0, isHighRes = false, "lowRes/parity0", promote, demote, extInfoQueue, processedThisTick)
            bits.byteAlign()
        }

        for (slot in promote) highRes[slot] = true
        for (slot in demote) highRes[slot] = false
        for (slot in 1..MAX_SLOT) nsn[slot] = processedThisTick[slot]

        for (slot in extInfoQueue) {
            try {
                if (buffer.readableBytes() < 2) break
                val blockSize = buffer.g2()
                val byteOffset = totalBytes - buffer.readableBytes()
                sb.append("\n  EXT ${slotLabel(slot)} byteOffset=$byteOffset blockSize=$blockSize ")
                currentDecodingSlot = slot
                sb.append(decodeExtendedInfoBlock(buffer, blockSize))
            } catch (e: Exception) {
                sb.append("\n  [ext-info decode error: ${e.message}]")
                break
            }
        }
        return sb.toString()
    }

    private fun highResSlots(parity: Boolean): List<Int> = (1..MAX_SLOT).filter { highRes[it] && nsn[it] == parity }
    private fun lowResSlots(parity: Boolean): List<Int> = (1..MAX_SLOT).filter { !highRes[it] && nsn[it] == parity }

    override public fun decode(buffer: JagByteBuf): PlayerInfo =
        try {
            decodeTypedOrThrow(buffer)
        } catch (e: Exception) {
            for (slot in 1..MAX_SLOT) nsn[slot] = true
            PlayerInfo(emptyMap())
        }

    private fun decodeTypedOrThrow(buffer: JagByteBuf): PlayerInfo {
        val updates = mutableMapOf<Int, PlayerUpdateType>()
        val promote = mutableListOf<Int>()
        val demote = mutableListOf<Int>()
        val extInfoQueue = mutableListOf<Int>()
        val processedThisTick = BooleanArray(MAX_SLOT + 1)

        val hr0 = highResSlots(false)
        val hr1 = highResSlots(true)
        val lr1 = lowResSlots(true)
        val lr0 = lowResSlots(false)

        buffer.buffer.toBitBuf().use { bits ->
            decodeCohortPassTyped(bits, hr0, isHighRes = true, promote, demote, extInfoQueue, processedThisTick, updates)
            bits.byteAlign()
            decodeCohortPassTyped(bits, hr1, isHighRes = true, promote, demote, extInfoQueue, processedThisTick, updates)
            bits.byteAlign()
            decodeCohortPassTyped(bits, lr1, isHighRes = false, promote, demote, extInfoQueue, processedThisTick, updates)
            bits.byteAlign()
            decodeCohortPassTyped(bits, lr0, isHighRes = false, promote, demote, extInfoQueue, processedThisTick, updates)
            bits.byteAlign()
        }

        for (slot in promote) highRes[slot] = true
        for (slot in demote) highRes[slot] = false
        for (slot in 1..MAX_SLOT) nsn[slot] = processedThisTick[slot]

        extInfoLoop@ for (slot in extInfoQueue) {
            if (buffer.readableBytes() < 2) break@extInfoLoop
            val rendered =
                try {
                    val blockSize = buffer.g2()
                    currentDecodingSlot = slot
                    decodeExtendedInfoBlock(buffer, blockSize)
                } catch (e: Exception) {
                    break@extInfoLoop
                }
            val name = playerNameByIndex[slot]
            when (val existing = updates[slot]) {
                is PlayerUpdateType.HighResolutionIdle -> updates[slot] = existing.copy(name = name, extendedInfo = listOf(rendered))
                is PlayerUpdateType.LowResolutionToHighResolution -> updates[slot] = existing.copy(name = name, extendedInfo = listOf(rendered))
                is PlayerUpdateType.HighResolutionMovement -> updates[slot] = existing.copy(name = name, extendedInfo = listOf(rendered))
                else -> Unit
            }
        }

        return PlayerInfo(updates)
    }

    private fun decodeCohortPassTyped(
        bits: BitBuf,
        slots: List<Int>,
        isHighRes: Boolean,
        promote: MutableList<Int>,
        demote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
        updates: MutableMap<Int, PlayerUpdateType>,
    ) {
        var i = 0
        while (i < slots.size) {
            val needsUpdate = bits.readBoolean()
            if (!needsUpdate) {
                val width = bits.readBits(2)
                val count =
                    when (width) {
                        0 -> 0
                        1 -> bits.readBits(5)
                        2 -> bits.readBits(8)
                        else -> bits.readBits(11)
                    }
                val end = (i + count).coerceAtMost(slots.size - 1)
                for (j in i..end) {
                    processedThisTick[slots[j]] = true
                    updates[slots[j]] = PlayerUpdateType.LowResolutionIdle
                }
                i += 1 + count
                continue
            }
            val slot = slots[i]
            if (isHighRes) {
                decodeHighResRecordTyped(bits, slot, promote, demote, extInfoQueue, processedThisTick, updates)
            } else {
                decodeLowResRecordTyped(bits, slot, promote, extInfoQueue, processedThisTick, updates)
            }
            i++
        }
    }

    private fun decodeHighResRecordTyped(
        bits: BitBuf,
        slot: Int,
        promote: MutableList<Int>,
        demote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
        updates: MutableMap<Int, PlayerUpdateType>,
    ) {
        val hasExt = bits.readBoolean()
        val movementType = bits.readBits(2)
        var wasDemoted = false
        when (movementType) {
            0 -> {
                updates[slot] = PlayerUpdateType.HighResolutionIdle(name = null, extendedInfo = emptyList())
                if (!hasExt && slot != ownIndex) {
                    val hasDemoteRecord = bits.readBoolean()
                    if (hasDemoteRecord) {
                        demote.add(slot)
                        highResSeeded[slot] = false
                        wasDemoted = true
                        decodeLowResRecordTyped(bits, slot, promote, extInfoQueue, processedThisTick, updates)
                        val chunk = lowResCoords[slot]
                        updates[slot] =
                            PlayerUpdateType.HighResolutionToLowResolution(
                                (chunk ushr 16) and 0x3,
                                (chunk ushr 8) and 0xFF,
                                chunk and 0xFF,
                            )
                    }
                }
            }
            1 -> {
                val dir = bits.readBits(3)
                val (dx, dz) = WALK_DELTAS[dir]
                val cornerFlag = bits.readBoolean()
                if (cornerFlag) bits.readBits(2)
                if (highResSeeded[slot]) {
                    highResX[slot] += dx
                    highResZ[slot] += dz
                }
                updates[slot] =
                    PlayerUpdateType.HighResolutionMovement(
                        teleport = false,
                        ambiguous = !highResSeeded[slot],
                        level = if (highResSeeded[slot]) highResPlane[slot] else null,
                        x = if (highResSeeded[slot]) highResX[slot] else null,
                        z = if (highResSeeded[slot]) highResZ[slot] else null,
                        rawX = dx,
                        rawZ = dz,
                        name = null,
                        extendedInfo = emptyList(),
                    )
            }
            2 -> {
                val runCode = bits.readBits(4)
                val (dx, dz) = RUN_DELTAS[runCode]
                if (highResSeeded[slot]) {
                    highResX[slot] += dx
                    highResZ[slot] += dz
                }
                updates[slot] =
                    PlayerUpdateType.HighResolutionMovement(
                        teleport = false,
                        ambiguous = !highResSeeded[slot],
                        level = if (highResSeeded[slot]) highResPlane[slot] else null,
                        x = if (highResSeeded[slot]) highResX[slot] else null,
                        z = if (highResSeeded[slot]) highResZ[slot] else null,
                        rawX = dx,
                        rawZ = dz,
                        name = null,
                        extendedInfo = emptyList(),
                    )
            }
            3 -> {
                val big = bits.readBoolean()
                if (big) {
                    val speed = bits.readBits(3)
                    val packed = bits.readBits(30)
                    val plane = (packed ushr 28) and 0x3
                    val dx = signExtend((packed ushr 14) and 0x3FFF, 14)
                    val dz = signExtend(packed and 0x3FFF, 14)
                    highResPlane[slot] = plane
                    highResSeeded[slot] = false
                    updates[slot] =
                        PlayerUpdateType.HighResolutionMovement(
                            teleport = true,
                            ambiguous = true,
                            level = plane,
                            x = null,
                            z = null,
                            rawX = dx,
                            rawZ = dz,
                            name = null,
                            extendedInfo = emptyList(),
                        )
                } else {
                    val packed = bits.readBits(15)
                    val dx = signExtend(packed and 0x1F, 5)
                    val dz = signExtend((packed ushr 5) and 0x1F, 5)
                    val planeDelta = (packed ushr 10) and 0x3
                    if (highResSeeded[slot]) {
                        highResX[slot] += dx
                        highResZ[slot] += dz
                        highResPlane[slot] = (highResPlane[slot] + planeDelta) and 0x3
                    }
                    updates[slot] =
                        PlayerUpdateType.HighResolutionMovement(
                            teleport = true,
                            ambiguous = !highResSeeded[slot],
                            level = if (highResSeeded[slot]) highResPlane[slot] else null,
                            x = if (highResSeeded[slot]) highResX[slot] else null,
                            z = if (highResSeeded[slot]) highResZ[slot] else null,
                            rawX = dx,
                            rawZ = dz,
                            name = null,
                            extendedInfo = emptyList(),
                        )
                }
            }
        }
        if (!wasDemoted) processedThisTick[slot] = false
        if (hasExt) extInfoQueue.add(slot)
    }

    private fun decodeLowResRecordTyped(
        bits: BitBuf,
        slot: Int,
        promote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
        updates: MutableMap<Int, PlayerUpdateType>,
    ) {
        val leadCode = bits.readBits(2)
        if (leadCode == 0) {
            decodeLowResPromoteChainTyped(bits, slot, promote, extInfoQueue, updates)
        } else {
            decodeLowResMoveBodyTyped(bits, leadCode, slot)
            val chunk = lowResCoords[slot]
            updates[slot] =
                PlayerUpdateType.LowResolutionMovement(
                    (chunk ushr 16) and 0x3,
                    (chunk ushr 8) and 0xFF,
                    chunk and 0xFF,
                )
        }
        processedThisTick[slot] = leadCode == 0
    }

    private fun decodeLowResPromoteChainTyped(
        bits: BitBuf,
        slot: Int,
        promote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        updates: MutableMap<Int, PlayerUpdateType>,
    ) {
        val stale = bits.readBoolean()
        if (stale) {
            val nestedLeadCode = bits.readBits(2)
            if (nestedLeadCode == 0) {
                decodeLowResPromoteChainTyped(bits, slot, promote, extInfoQueue, updates)
            } else {
                decodeLowResMoveBodyTyped(bits, nestedLeadCode, slot)
            }
        }
        val fineX = bits.readBits(6)
        val fineZ = bits.readBits(6)
        val promoteFlag = bits.readBoolean()
        if (promoteFlag) {
            if (slot !in promote) promote.add(slot)
            if (slot !in extInfoQueue) extInfoQueue.add(slot)
            val chunk = lowResCoords[slot]
            val chunkX = (chunk ushr 8) and 0xFF
            val chunkZ = chunk and 0xFF
            val plane = (chunk ushr 16) and 0x3
            highResX[slot] = (chunkX shl 6) or fineX
            highResZ[slot] = (chunkZ shl 6) or fineZ
            highResPlane[slot] = plane
            highResSeeded[slot] = true
            updates[slot] =
                PlayerUpdateType.LowResolutionToHighResolution(
                    plane,
                    highResX[slot],
                    highResZ[slot],
                    null,
                    emptyList(),
                )
        } else if (stale) {
            val chunk = lowResCoords[slot]
            updates[slot] =
                PlayerUpdateType.LowResolutionMovement(
                    (chunk ushr 16) and 0x3,
                    (chunk ushr 8) and 0xFF,
                    chunk and 0xFF,
                )
        } else if (slot !in updates) {
            updates[slot] = PlayerUpdateType.LowResolutionIdle
        }
    }

    private fun decodeLowResMoveBodyTyped(
        bits: BitBuf,
        code: Int,
        slot: Int,
    ) {
        val prev = lowResCoords[slot]
        val prevPlane = (prev ushr 16) and 0x3
        val prevX = (prev ushr 8) and 0xFF
        val prevZ = prev and 0xFF
        when (code) {
            1 -> {
                val planeDelta = bits.readBits(2)
                val newPlane = (prevPlane + planeDelta) and 0x3
                lowResCoords[slot] = (newPlane shl 16) or (prevX shl 8) or prevZ
            }
            2 -> {
                val packed = bits.readBits(5)
                val planeDelta = (packed ushr 3) and 0x3
                val dirCode = packed and 0x7
                val (dx, dz) = WALK_DELTAS[dirCode]
                val newPlane = (prevPlane + planeDelta) and 0x3
                val newX = (prevX + dx) and 0xFF
                val newZ = (prevZ + dz) and 0xFF
                lowResCoords[slot] = (newPlane shl 16) or (newX shl 8) or newZ
            }
            3 -> {
                val packed = bits.readBits(20)
                val planeDelta = (packed ushr 16) and 0x3
                val dx = signExtend((packed ushr 8) and 0xFF, 8)
                val dz = signExtend(packed and 0xFF, 8)
                val newPlane = (prevPlane + planeDelta) and 0x3
                val newX = (prevX + dx) and 0xFF
                val newZ = (prevZ + dz) and 0xFF
                lowResCoords[slot] = (newPlane shl 16) or (newX shl 8) or newZ
            }
        }
        lowResSeeded[slot] = true
    }

    private fun runPass(
        bits: BitBuf,
        sb: StringBuilder,
        slots: List<Int>,
        isHighRes: Boolean,
        passLabel: String,
        promote: MutableList<Int>,
        demote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
    ) {
        try {
            decodeCohortPass(bits, sb, slots, isHighRes, passLabel, promote, demote, extInfoQueue, processedThisTick)
        } catch (e: Exception) {
            val diag = "pass=$passLabel cohortSize=${slots.size} slots=$slots bitPos=${bits.readerIndex()} " +
                "byteOffset=${bits.readerIndex() / 8}"
            throw PassDecodeException(sb.toString(), diag, e)
        }
    }

    private fun decodeCohortPass(
        bits: BitBuf,
        sb: StringBuilder,
        slots: List<Int>,
        isHighRes: Boolean,
        passLabel: String,
        promote: MutableList<Int>,
        demote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
    ) {
        var i = 0
        while (i < slots.size) {
            val needsUpdate = bits.readBoolean()
            if (!needsUpdate) {
                val width = bits.readBits(2)
                val count =
                    when (width) {
                        0 -> 0
                        1 -> bits.readBits(5)
                        2 -> bits.readBits(8)
                        else -> bits.readBits(11)
                    }
                val end = (i + count).coerceAtMost(slots.size - 1)
                for (j in i..end) processedThisTick[slots[j]] = true
                i += 1 + count
                continue
            }
            val slot = slots[i]
            try {
                if (isHighRes) {
                    decodeHighResRecord(bits, sb, slot, promote, demote, extInfoQueue, processedThisTick)
                } else {
                    decodeLowResRecord(bits, sb, slot, promote, extInfoQueue, processedThisTick, passLabel, slots.size, i)
                }
            } catch (e: Exception) {
                throw Exception("failed at index=$i (slot=$slot) of this pass: ${e.message}", e)
            }
            i++
        }
    }

    private fun decodeHighResRecord(
        bits: BitBuf,
        sb: StringBuilder,
        slot: Int,
        promote: MutableList<Int>,
        demote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
    ) {
        val hasExt = bits.readBoolean()
        val movementType = bits.readBits(2)
        val posStr = if (highResSeeded[slot]) {
            " pos=${formatCoord(highResPlane[slot], highResX[slot], highResZ[slot])}"
        } else {
            ""
        }
        when (movementType) {
            0 -> {
                if (hasExt) sb.append("\n  IDLE ${slotLabel(slot)} ext=$hasExt$posStr")
                if (!hasExt && movementType == 0 && slot != ownIndex) {
                    val hasDemoteRecord = bits.readBoolean()
                    if (hasDemoteRecord) {
                        demote.add(slot)
                        highResSeeded[slot] = false
                        decodeLowResRecord(bits, sb, slot, promote, extInfoQueue, processedThisTick, "demote-triggered", 1, 0)
                    }
                }
            }
            1 -> {
                val dir = bits.readBits(3)
                val (dx, dz) = WALK_DELTAS[dir]
                val cornerFlag = bits.readBoolean()
                var cornerStr = "null"
                if (cornerFlag) {
                    val corner = bits.readBits(2)
                    val (cdx, cdz) = CORNER_DELTAS[corner]
                    cornerStr = "$corner(dx=$cdx,dz=$cdz)"
                }
                if (highResSeeded[slot]) {
                    highResX[slot] += dx
                    highResZ[slot] += dz
                }
                sb.append("\n  WALK ${slotLabel(slot)} dir=$dir(dx=$dx,dz=$dz) corner=$cornerStr ext=$hasExt$posStr")
            }
            2 -> {
                val runCode = bits.readBits(4)
                val (dx, dz) = RUN_DELTAS[runCode]
                if (highResSeeded[slot]) {
                    highResX[slot] += dx
                    highResZ[slot] += dz
                }
                sb.append("\n  RUN ${slotLabel(slot)} runCode=$runCode(dx=$dx,dz=$dz) ext=$hasExt$posStr")
            }
            3 -> {
                val big = bits.readBoolean()
                if (big) {
                    val speed = bits.readBits(3)
                    val packed = bits.readBits(30)
                    val plane = (packed ushr 28) and 0x3
                    val dx = signExtend((packed ushr 14) and 0x3FFF, 14)
                    val dz = signExtend(packed and 0x3FFF, 14)
                    highResPlane[slot] = plane
                    highResSeeded[slot] = false
                    sb.append("\n  TELEPORT ${slotLabel(slot)} big=true speed=$speed plane=$plane dx=$dx dz=$dz ext=$hasExt")
                } else {
                    val packed = bits.readBits(15)
                    val dx = signExtend(packed and 0x1F, 5)
                    val dz = signExtend((packed ushr 5) and 0x1F, 5)
                    val planeDelta = (packed ushr 10) and 0x3
                    if (highResSeeded[slot]) {
                        highResX[slot] += dx
                        highResZ[slot] += dz
                        highResPlane[slot] = (highResPlane[slot] + planeDelta) and 0x3
                    }
                    sb.append("\n  TELEPORT ${slotLabel(slot)} big=false planeDelta=$planeDelta dx=$dx dz=$dz ext=$hasExt$posStr")
                }
            }
        }
        val wasDemoted = movementType == 0 && !hasExt && slot != ownIndex && demote.lastOrNull() == slot
        if (!wasDemoted) processedThisTick[slot] = false
        if (hasExt) extInfoQueue.add(slot)
    }

    private fun decodeLowResRecord(
        bits: BitBuf,
        sb: StringBuilder,
        slot: Int,
        promote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        processedThisTick: BooleanArray,
        passLabel: String,
        cohortSize: Int,
        indexInCohort: Int,
    ) {
        val leadCode = bits.readBits(2)
        val diag = "pass=$passLabel cohortSize=$cohortSize index=$indexInCohort"
        if (leadCode == 0) {
            decodeLowResPromoteChain(bits, sb, slot, promote, extInfoQueue, diag, depth = 0)
        } else {
            val (body, isNoOp) = decodeLowResMoveBody(bits, leadCode, slot, diag)
            if (!isNoOp) sb.append("\n  LOWRES_MOVE ${slotLabel(slot)} $body")
        }
        processedThisTick[slot] = leadCode == 0
    }

    private fun decodeLowResPromoteChain(
        bits: BitBuf,
        sb: StringBuilder,
        slot: Int,
        promote: MutableList<Int>,
        extInfoQueue: MutableList<Int>,
        diag: String,
        depth: Int,
    ) {
        val stale = bits.readBoolean()
        val moveStr: String? =
            if (stale) {
                val nestedLeadCode = bits.readBits(2)
                if (nestedLeadCode == 0) {
                    decodeLowResPromoteChain(bits, sb, slot, promote, extInfoQueue, diag, depth + 1)
                    "nested-promote(depth=${depth + 1})"
                } else {
                    decodeLowResMoveBody(bits, nestedLeadCode, slot, diag).first
                }
            } else {
                null
            }
        val fineX = bits.readBits(6)
        val fineZ = bits.readBits(6)
        val promoteFlag = bits.readBoolean()
        if (stale || promoteFlag) {
            sb.append("\n  LOWRES_PROMOTE ${slotLabel(slot)} stale=$stale move=$moveStr fineX=$fineX fineZ=$fineZ promote=$promoteFlag")
        }
        if (promoteFlag) {
            if (slot !in promote) promote.add(slot)
            if (slot !in extInfoQueue) extInfoQueue.add(slot)
            val chunk = lowResCoords[slot]
            val chunkX = (chunk ushr 8) and 0xFF
            val chunkZ = chunk and 0xFF
            highResX[slot] = (chunkX shl 6) or fineX
            highResZ[slot] = (chunkZ shl 6) or fineZ
            highResPlane[slot] = (chunk ushr 16) and 0x3
            highResSeeded[slot] = true
        }
    }

    private fun decodeLowResMoveBody(
        bits: BitBuf,
        code: Int,
        slot: Int,
        diag: String,
    ): Pair<String, Boolean> {
        val hadBaseline = lowResSeeded[slot]
        val prev = lowResCoords[slot]
        val prevPlane = (prev ushr 16) and 0x3
        val prevX = (prev ushr 8) and 0xFF
        val prevZ = prev and 0xFF

        var isNoOp = false
        val result =
            when (code) {
                1 -> {
                    val planeDelta = bits.readBits(2)
                    val newPlane = (prevPlane + planeDelta) and 0x3
                    lowResCoords[slot] = (newPlane shl 16) or (prevX shl 8) or prevZ
                    isNoOp = planeDelta == 0
                    "plane=$newPlane(+$planeDelta) x=$prevX z=$prevZ"
                }
                2 -> {
                    val packed = bits.readBits(5)
                    val planeDelta = (packed ushr 3) and 0x3
                    val dirCode = packed and 0x7
                    val (dx, dz) = WALK_DELTAS[dirCode]
                    val newPlane = (prevPlane + planeDelta) and 0x3
                    val newX = (prevX + dx) and 0xFF
                    val newZ = (prevZ + dz) and 0xFF
                    lowResCoords[slot] = (newPlane shl 16) or (newX shl 8) or newZ
                    "plane=$newPlane(+$planeDelta) dir=$dirCode(dx=$dx,dz=$dz) x=$newX z=$newZ"
                }
                3 -> {
                    val packed = bits.readBits(20)
                    val planeDelta = (packed ushr 16) and 0x3
                    val dx = signExtend((packed ushr 8) and 0xFF, 8)
                    val dz = signExtend(packed and 0xFF, 8)
                    val newPlane = (prevPlane + planeDelta) and 0x3
                    val newX = (prevX + dx) and 0xFF
                    val newZ = (prevZ + dz) and 0xFF
                    lowResCoords[slot] = (newPlane shl 16) or (newX shl 8) or newZ
                    isNoOp = planeDelta == 0 && dx == 0 && dz == 0
                    "plane=$newPlane(+$planeDelta) x=$newX(${if (dx >= 0) "+" else ""}$dx) z=$newZ(${if (dz >= 0) "+" else ""}$dz)"
                }
                else -> "unexpected code $code slot=$slot"
            }
        lowResSeeded[slot] = true
        val text = if (!hadBaseline) "$result (first sighting)" else result
        return text to (isNoOp && hadBaseline)
    }

    private fun signExtend(value: Int, bitWidth: Int): Int {
        val shift = 32 - bitWidth
        return (value shl shift) shr shift
    }

    private companion object {
        const val MAX_SLOT = 0x7FF
        val WALK_DELTAS = arrayOf(-1 to -1, 0 to -1, 1 to -1, -1 to 0, 1 to 0, -1 to 1, 0 to 1, 1 to 1)
        val RUN_DELTAS =
            arrayOf(
                -2 to -2, -1 to -2, 0 to -2, 1 to -2, 2 to -2,
                -2 to -1, 2 to -1, -2 to 0, 2 to 0, -2 to 1, 2 to 1,
                -2 to 2, -1 to 2, 0 to 2, 1 to 2, 2 to 2,
            )
        val CORNER_DELTAS = arrayOf(0 to 1, -1 to 0, 1 to 0, 0 to -1)
    }

    public fun decodeExtendedInfoBlock(
        r: JagByteBuf,
        blockSize: Int,
    ): String {
        val startRemaining = r.readableBytes()
        val mask = r.readExpandableMask(Rs3PlayerUpdateMaskKey.EXPANSION_BITS)
        val parts = mutableListOf("mask=0x${mask.toString(16)}")
        try {
            for (key in Rs3PlayerUpdateMaskKey.byOrder) {
                if ((mask and (1L shl key.bit)) == 0L) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
                parts += decodeKnownBlock(key, r)
            }
        } catch (e: Exception) {
            parts += "<FAILED mid-block: ${e.javaClass.simpleName}: ${e.message}>"
            throw ExtendedInfoBlockException(parts.joinToString(" "), e)
        }
        val consumed = startRemaining - r.readableBytes()
        if (consumed < blockSize) r.skipRead(blockSize - consumed)
        return parts.joinToString(" ")
    }

    private fun decodeSpotanimList(r: JagByteBuf): String {
        val removeCount = r.g1()
        val removes = (0 until removeCount).map { r.g2s() }
        val addCount = r.g1()
        val adds = (0 until addCount).map {
            val slot = r.g1Alt3()
            val id = r.g2Alt1()
            val heightDelay = r.g4Alt1()
            val rot = r.g1()
            val fine = r.g3()
            "slot=$slot id=$id dh=$heightDelay rot=$rot fine=$fine"
        }
        return "SPOTANIMS(removes=$removes adds=[${adds.joinToString(",")}])"
    }

    private fun decodeParamsProbe(r: JagByteBuf, label: String): String {
        val startPos = r.buffer.readerIndex()
        val results = mutableListOf<String>()
        for (variant in listOf("plain", "alt1", "alt2", "alt3")) {
            r.buffer.readerIndex(startPos)
            try {
                r.g2()
                val count = r.g1()
                val types = mutableListOf<Int>()
                var ok = true
                repeat(count) {
                    val type = when (variant) {
                        "plain" -> r.g1()
                        "alt1" -> r.g1Alt1()
                        "alt2" -> r.g1Alt2()
                        "alt3" -> r.g1Alt3()
                        else -> error("")
                    }
                    types += type
                    if (type !in 0..3) ok = false
                    r.g2()
                    when (type) {
                        0 -> r.g4()
                        1 -> r.g8()
                        2 -> r.gjstr()
                        3 -> { r.g1(); r.g4(); r.g4(); r.g4() }
                        else -> ok = false
                    }
                }
                if (ok) results += "$variant->types=$types"
            } catch (e: Exception) {
            }
        }
        r.buffer.readerIndex(startPos)
        return "PROBE[$label]: " + results.joinToString("; ").ifEmpty { "none parsed cleanly" }
    }

    private fun decodeAppearance(r: JagByteBuf): String {
        val len = r.g1()
        val bytes = (0 until len).map { r.g1() }
        val decoded = bytes.map { (it - 0x80) and 0xFF }
        var p = 0
        val flags = decoded.getOrNull(p) ?: 0
        p++
        val titleId =
            if ((flags and 0x40) != 0 && p + 1 < decoded.size) {
                val v = (decoded[p] shl 8) or decoded[p + 1]
                p += 2
                v
            } else {
                null
            }
        val objOverrideCount =
            if ((flags and 0x02) != 0 && p < decoded.size) {
                val c = decoded[p]
                p += 1 + c * 3
                c
            } else {
                null
            }
        val displayMode = decoded.getOrNull(p)
        if (displayMode != null) p++
        val ascii = decoded.joinToString("") { d -> if (d in 32..126) d.toChar().toString() else "." }
        val longestRun = ascii.split(".").filter { it.length >= 3 }.maxByOrNull { it.length }
        val slot = currentDecodingSlot
        if (slot != null && longestRun != null) {
            playerNameByIndex[slot] = longestRun
        }
        return "APPEARANCE(len=$len flags=0x${flags.toString(16)} titleId=$titleId name=\"$longestRun\")"
    }

    private fun decodeBoneTransforms(r: JagByteBuf): String {
        val count = r.g1sAlt3()
        if (count <= 0) return "TRANSFORM_BLEND(count=$count CLEAR)"
        val entries = mutableListOf<String>()
        repeat(count) {
            val flags = r.g2Alt2()
            val boneId = r.g2()
            val extra = if ((flags and 0xC00) != 0) r.g4Alt3() else null
            val f1 = if ((flags and 0x1) != 0) r.g4Alt1() else null
            val f2 = if ((flags and 0x2) != 0) r.g4Alt2() else null
            val f4 = if ((flags and 0x4) != 0) r.g4() else null
            val f8 = if ((flags and 0x8) != 0) r.g4() else null
            val f10 = if ((flags and 0x10) != 0) r.g4Alt1() else null
            val f20 = if ((flags and 0x20) != 0) r.g4() else null
            val f80 = if ((flags and 0x80) != 0) r.g4() else null
            val f100 = if ((flags and 0x100) != 0) r.g4Alt2() else null
            val f200 = if ((flags and 0x200) != 0) r.g4Alt3() else null
            entries += "bone=$boneId flags=0x${flags.toString(16)} extra=$extra t=($f1,$f2,$f4) r=($f8,$f10,$f20) s=($f80,$f100,$f200)"
        }
        return "TRANSFORM_BLEND(count=$count entries=[${entries.joinToString(",")}])"
    }

    private fun decodeParams(r: JagByteBuf, label: String): String {
        r.g2()
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
                    entries += "key=$key type=$type <unrecognized>"
                    break
                }
            }
        }
        return "$label(entries=[${entries.joinToString(",")}])"
    }

    private fun decodeHeadIcons(r: JagByteBuf): String {
        val len = r.g1Alt3()
        val slotMask = r.g1()
        val slots = mutableListOf<String>()
        for (bit in 0 until 8) {
            if ((slotMask and (1 shl bit)) == 0) continue
            val byte1 = r.g1()
            val short1raw = r.g2()
            val short1 = if (short1raw == 0xFFFF) -1 else short1raw
            val valA = r.gSmart2or4()
            val valB = r.gSmart2or4()
            val valC = r.gSmart2or4()
            val valD = r.gSmart2or4()
            slots += "slot=$bit byte1=$byte1 short1=$short1 a=$valA b=$valB c=$valC d=$valD"
        }
        return "HEAD_ICONS(len=$len slotMask=0x${slotMask.toString(16)} slots=[${slots.joinToString(",")}])"
    }

    private class ExtendedInfoBlockException(val partial: String, cause: Exception) : Exception(cause)

    private fun decodeKnownBlock(
        key: Rs3PlayerUpdateMaskKey,
        r: JagByteBuf,
    ): String =
        when (key) {
            Rs3PlayerUpdateMaskKey.SPOTANIM -> decodeSpotanimList(r)
            Rs3PlayerUpdateMaskKey.ENABLED_OPS -> {
                val v1 = r.g1Alt3()
                val v2 = r.g1Alt1()
                val opMask = r.g2()
                "ENABLED_OPS(v1=$v1 v2=$v2 opMask=0x${opMask.toString(16)})"
            }
            Rs3PlayerUpdateMaskKey.SECONDARY_FREEZE -> {
                val delay = r.g2Alt2()
                val duration = r.g4Alt2()
                val cancel = r.g1Alt1() == 1
                "SECONDARY_FREEZE(delay=$delay duration=$duration cancel=$cancel)"
            }
            Rs3PlayerUpdateMaskKey.SEQUENCE -> {
                val layer0 = r.gSmart2or4null()
                val layer1 = r.gSmart2or4null()
                val layer2 = r.gSmart2or4null()
                val layer3 = r.gSmart2or4null()
                val delay = r.g1()
                "SEQUENCE(layer0=$layer0 layer1=$layer1 layer2=$layer2 layer3=$layer3 delay=$delay)"
            }
            Rs3PlayerUpdateMaskKey.MOVE_SPEED -> "MOVE_SPEED(speed=${r.g1sAlt3()})"
            Rs3PlayerUpdateMaskKey.FACE_ENTITY -> {
                val packed = r.g3Alt1()
                val kind = (packed shr 16) and 0xFF
                val index = packed and 0xFFFF
                val kindLabel = when (kind) {
                    1 -> "NPC"
                    2 -> "PLAYER"
                    255, 127 -> "NONE"
                    else -> "UNK_$kind"
                }
                "FACE_ENTITY(kind=$kindLabel index=$index)"
            }
            Rs3PlayerUpdateMaskKey.PLAYER_FREEZE -> {
                val delay = r.g2Alt1()
                val duration = r.g4Alt2()
                val cancel = r.g1() == 1
                "PLAYER_FREEZE(delay=$delay duration=$duration cancel=$cancel)"
            }
            Rs3PlayerUpdateMaskKey.TIMED_EFFECT_1 -> {
                val v1 = r.g2Alt1()
                val v2 = r.g4Alt1()
                val v3 = r.g1Alt1()
                "TIMED_EFFECT_1(v1=$v1 v2=$v2 v3=$v3)"
            }
            Rs3PlayerUpdateMaskKey.CONFIG_PARAMS_1 -> decodeParamsProbe(r, "CONFIG_PARAMS_1") + " | " + decodeParams(r, "CONFIG_PARAMS_1")
            Rs3PlayerUpdateMaskKey.HITMARKS_AND_HEADBARS -> decodeNarrowHitmarksAndHeadbars(r)
            Rs3PlayerUpdateMaskKey.HEAD_ICONS -> decodeHeadIcons(r)
            Rs3PlayerUpdateMaskKey.NAME_EXTRAS -> {
                val name = r.gjstr()
                val flags = r.g1()
                "NAME_EXTRAS(\"$name\" flags=$flags)"
            }
            Rs3PlayerUpdateMaskKey.APPEARANCE -> decodeAppearance(r)
            Rs3PlayerUpdateMaskKey.EXACT_MOVE -> {
                val d1 = r.g1sAlt3()
                val d2 = r.g1s()
                val d3 = r.g1sAlt2()
                val d4 = r.g1sAlt1()
                val d5 = r.g1sAlt2()
                val d6 = r.g1sAlt3()
                val v1 = r.g2Alt1()
                val v2 = r.g2()
                val v3 = r.g2Alt1()
                "EXACT_MOVE(d1=$d1 d2=$d2 d3=$d3 d4=$d4 d5=$d5 d6=$d6 v1=$v1 v2=$v2 v3=$v3)"
            }
            Rs3PlayerUpdateMaskKey.CONFIG_PARAMS_2 -> decodeParams(r, "CONFIG_PARAMS_2") + " | " + decodeParamsProbe(r,
                "CONFIG_PARAMS_2")
            Rs3PlayerUpdateMaskKey.TINTING -> {
                val hue = r.g1s()
                val sat = r.g1sAlt3()
                val lum = r.g1s()
                val brightness = r.g1Alt3()
                val startCycle = r.g2Alt3()
                val endCycle = r.g2()
                "TINTING(hue=$hue sat=$sat lum=$lum bright=$brightness cycle=$startCycle..$endCycle)"
            }
            Rs3PlayerUpdateMaskKey.SCALE_CHANGE -> {
                val flag = r.g1()
                val scaleX = r.g2Alt3()
                val scaleY = r.g2Alt3()
                val scaleZ = r.g2Alt3()
                "SCALE_CHANGE(flag=$flag scale=($scaleX,$scaleY,$scaleZ))"
            }
            Rs3PlayerUpdateMaskKey.HITMARKS_AND_HEADBARS_WIDE -> decodeWideHitmarksAndHeadbars(r)
            Rs3PlayerUpdateMaskKey.BONE_TRANSFORMS -> decodeBoneTransforms(r)
            Rs3PlayerUpdateMaskKey.TRANSPARENCY -> {
                val alpha = r.g1Alt2()
                "TRANSPARENCY(alpha=$alpha)"
            }
            Rs3PlayerUpdateMaskKey.SAY -> "SAY(\"${r.gjstr()}\")"
            Rs3PlayerUpdateMaskKey.HEAD_TURN_ANGLE -> {
                val angle = r.g2()
                "HEAD_TURN_ANGLE(angle=$angle)"
            }
            Rs3PlayerUpdateMaskKey.TIMED_EFFECT_2 -> {
                val v1 = r.g2Alt1()
                val v2 = r.g4Alt3()
                val v3 = r.g1()
                "TIMED_EFFECT_2(v1=$v1 v2=$v2 v3=$v3)"
            }
            Rs3PlayerUpdateMaskKey.TIMED_EFFECT_3 -> {
                val v1 = r.g2Alt2()
                val v2 = r.g4Alt2()
                val v3 = r.g1Alt2()
                "TIMED_EFFECT_3(v1=$v1 v2=$v2 v3=$v3)"
            }
        }

    private fun decodeNarrowHitmarksAndHeadbars(r: JagByteBuf): String {
        val hitCount = r.g1Alt3()
        val hits = mutableListOf<String>()
        repeat(hitCount) { hits += decodeHitEntryNarrow(r) }
        val barCount = r.g1()
        val bars = (0 until barCount).map { decodeHeadbarNarrow(r) }
        return "HITMARKS_AND_HEADBARS(hits=[${hits.joinToString(",")}] bars=[${bars.joinToString(",")}])"
    }

    private fun decodeHitEntryNarrow(r: JagByteBuf): String {
        var type = r.gSmart1or2()
        val body: String
        if (type == 0x7fff) {
            type = r.gSmart1or2()
            val damage = r.gSmart1or2()
            val soak = r.gSmart1or2()
            val extra = r.gSmart1or2()
            body = "big(type=$type damage=$damage soak=$soak extra=$extra)"
        } else if (type == 0x7ffe) {
            val soak = r.g1()
            body = "soak(soak=$soak)"
        } else {
            val damage = r.gSmart1or2()
            body = "normal(type=$type damage=$damage)"
        }
        val delay = r.gSmart1or2()
        return "$body delay=$delay"
    }

    private fun decodeHeadbarNarrow(r: JagByteBuf): String {
        val sel = r.gSmart1or2()
        val durationCheck = r.gSmart1or2()
        if (durationCheck == 0x7fff) return "sel=$sel REMOVE"
        val duration = r.gSmart1or2()
        val front = r.g1()
        val back: Int
        val transId: Int
        val front2: Int?
        val back2: Int?
        if (durationCheck == 0) {
            transId = r.gSmartSigned()
            if (transId < 0) {
                back = front
                front2 = null
                back2 = null
            } else {
                back = r.g1()
                front2 = null
                back2 = null
            }
        } else {
            back = r.g1()
            transId = r.gSmartSigned()
            if (transId >= 0) {
                front2 = r.g1()
                back2 = r.g1()
            } else {
                front2 = null
                back2 = null
            }
        }
        return "sel=$sel duration=$duration front=$front back=$back transId=$transId" +
            (if (front2 != null) " front2=$front2 back2=$back2" else "")
    }

    private fun decodeWideHitmarksAndHeadbars(r: JagByteBuf): String {
        val hitCount = r.g1Alt2()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.g4Alt3()
                            val c = r.gSmart1or2()
                            val d = r.g4Alt3()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1()})"
                        else -> "normal(v1=$v1 a=${r.g4Alt3()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1Alt1()
        val bars = (0 until barCount).map { decodeHeadbarWide(r) }
        return "HITMARKS_AND_HEADBARS_2(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarWide(r: JagByteBuf): String {
        val sel = r.gSmart1or2()
        val durationCheck = r.gSmart1or2()
        if (durationCheck == 0x7fff) return "sel=$sel REMOVE"
        val duration = r.gSmart1or2()
        val front = r.g1()
        val back: Int
        val transId: Int
        val front2: Int?
        val back2: Int?
        if (durationCheck == 0) {
            transId = r.gSmartSigned()
            if (transId < 0) {
                back = front
                front2 = null
                back2 = null
            } else {
                back = r.g1()
                front2 = null
                back2 = null
            }
        } else {
            back = r.g1()
            transId = r.gSmartSigned()
            if (transId >= 0) {
                front2 = r.g1()
                back2 = r.g1()
            } else {
                front2 = null
                back2 = null
            }
        }
        return "sel=$sel duration=$duration front=$front back=$back transId=$transId" +
            (if (front2 != null) " front2=$front2 back2=$back2" else "")
    }
}

private fun BitBuf.readBits(count: Int): Int = gBits(count)
private fun BitBuf.readBoolean(): Boolean = gBits(1) != 0
private fun BitBuf.byteAlign() {
    readerIndex((readerIndex() + 7) and 7.inv())
}

private fun JagByteBuf.gSmartSigned(): Int {
    val peek = buffer.getByte(buffer.readerIndex())
    return if (peek >= 0) {
        g1() - 1
    } else {
        g2() + 0x7fff
    }
}

private fun JagByteBuf.readExpandableMask(expansionBits: IntArray): Long {
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
