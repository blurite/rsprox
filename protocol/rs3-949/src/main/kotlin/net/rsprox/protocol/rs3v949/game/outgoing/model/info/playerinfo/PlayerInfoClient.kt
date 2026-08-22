
package net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo

import io.netty.buffer.ByteBufUtil
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.util.Rs3PlayerUpdateMaskKey
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoDecoder

public class PlayerInfoClient(ownIndex: Int? = null) : PlayerInfoDecoder {
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

    private fun formatCoord(
        level: Int,
        x: Int,
        z: Int,
    ): String {
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

    private fun cohortSnapshot(): String {
        val highResMembers = (1..MAX_SLOT).filter { highRes[it] }.map { slotLabel(it) }
        val lowResPromotable = (1..MAX_SLOT).filter { !highRes[it] && !nsn[it] }
        return "cohortSnapshot: highRes members(${highResMembers.size})=$highResMembers " +
            "lowRes/parity0 members(${lowResPromotable.size})=${lowResPromotable.take(50)}" +
            if (lowResPromotable.size > 50) "...(+${lowResPromotable.size - 50} more)" else ""
    }

    public fun decodeText(buffer: JagByteBuf): String {
        packetCounter++
        val payloadSnapshot = ByteBufUtil.getBytes(buffer.buffer, buffer.buffer.readerIndex(), buffer.readableBytes())
        val header = "PLAYER_INFO #$packetCounter (gpiInitApplied=$gpiInitApplied)\n${cohortSnapshot()}"
        return try {
            "$header\n${decodeOrThrow(buffer, payloadSnapshot.size)}\n${hexDump(payloadSnapshot)}"
        } catch (e: PassDecodeException) {
            "$header\n${e.partial}\nPLAYER_INFO <decode FAILED at ${e.passDiag}: " +
                "${e.cause?.javaClass?.simpleName}: ${e.cause?.message}> len=${payloadSnapshot.size}\n${hexDump(payloadSnapshot)}"
        } catch (e: Exception) {
            "$header\nPLAYER_INFO <decode FAILED before any pass started: ${e.javaClass.simpleName}: " +
                "${e.message}> len=${payloadSnapshot.size}\n${hexDump(payloadSnapshot)}"
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
        sb.append(
            "\n  [pass sizes] highRes/parity0=${hr0.size}$hr0 highRes/parity1=${hr1.size}$hr1 " +
                "lowRes/parity1=${lr1.size} lowRes/parity0=${lr0.size}${if (lr0.size <= 50) lr0 else lr0.take(50).toString() + "...(+${lr0.size - 50} more)"}",
        )

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
                if (buffer.readableBytes() < 2) {
                    sb.append("\n  [truncated: expected ext-info block for slot=$slot, ${buffer.readableBytes()}b left]")
                    break
                }
                val blockSize = buffer.g2()
                val byteOffset = totalBytes - buffer.readableBytes()
                sb.append("\n  EXT ${slotLabel(slot)} byteOffset=$byteOffset blockSize=$blockSize ")
                currentDecodingSlot = slot
                sb.append(decodeExtendedInfoBlock(buffer, blockSize))
            } catch (e: ExtendedInfoBlockException) {
                sb.append("\n  EXT-PARTIAL ${slotLabel(slot)} ${e.partial}")
                sb.append(
                    "\n  [stopping ext-info for the rest of this packet - reader position now " +
                        "unreliable (cohort state above is still valid)]",
                )
                break
            } catch (e: Exception) {
                sb.append(
                    "\n  [ext-info decode error for slot=$slot: ${e.javaClass.simpleName}: " +
                        "${e.message} - stopping ext-info for this packet (cohort state above is still valid)]",
                )
                break
            }
        }
        if (buffer.readableBytes() > 0) {
            val trailerOffset = totalBytes - buffer.readableBytes()
            val trailerBytes = (0 until buffer.readableBytes()).map { buffer.g1() }
            sb.append(
                "\n  [UNEXAMINED TRAILING DATA byteOffset=$trailerOffset len=${trailerBytes.size}: " +
                    trailerBytes.joinToString(" ") { "%02x".format(it) } + "]",
            )
        }
        return sb.toString()
    }

    private fun highResSlots(parity: Boolean): List<Int> = (1..MAX_SLOT).filter { highRes[it] && nsn[it] == parity }

    override public fun decode(buffer: JagByteBuf): PlayerInfo =
        try {
            decodeTypedOrThrow(buffer)
        } catch (e: Exception) {
            for (slot in 1..MAX_SLOT) {
                nsn[slot] = true
            }
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
                    decodeExtendedInfoBlockTyped(buffer, blockSize)
                } catch (e: Exception) {
                    break@extInfoLoop
                }
            val name = playerNameByIndex[slot]
            when (val existing = updates[slot]) {
                is PlayerUpdateType.HighResolutionIdle -> updates[slot] = existing.copy(name = name, extendedInfo = rendered)
                is PlayerUpdateType.LowResolutionToHighResolution -> updates[slot] = existing.copy(name = name, extendedInfo = rendered)
                is PlayerUpdateType.HighResolutionMovement -> updates[slot] = existing.copy(name = name, extendedInfo = rendered)
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
                bits.readBits(3)
                val plane = bits.readBits(2)
                val dx: Int
                val dz: Int
                if (big) {
                    dx = signExtend(bits.readBits(14), 14)
                    dz = signExtend(bits.readBits(14), 14)
                } else {
                    dx = signExtend(bits.readBits(5), 5)
                    dz = signExtend(bits.readBits(5), 5)
                }
                if (!big) {
                    if (highResSeeded[slot]) {
                        highResX[slot] += dx
                        highResZ[slot] += dz
                        highResPlane[slot] = plane
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
                } else {
                    highResPlane[slot] = plane
                    highResSeeded[slot] = false
                    updates[slot] =
                        PlayerUpdateType.HighResolutionMovement(
                            teleport = true,
                            ambiguous = true,
                            level = null,
                            x = null,
                            z = null,
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

    private fun decodeExtendedInfoBlockTyped(
        r: JagByteBuf,
        blockSize: Int,
    ): List<String> {
        val startRemaining = r.readableBytes()
        val mask = r.readExpandableMask(Rs3PlayerUpdateMaskKey.EXPANSION_BITS)
        val parts = mutableListOf<String>()
        for (key in Rs3PlayerUpdateMaskKey.byOrder) {
            if ((mask and (1L shl key.bit)) == 0L) continue
            if (key in Rs3PlayerUpdateMaskKey.MECHANICAL) continue
            val consumedSoFar = startRemaining - r.readableBytes()
            if (consumedSoFar >= blockSize) break
            if (key !in Rs3PlayerUpdateMaskKey.DECODABLE) {
                val remainingInBlock = blockSize - consumedSoFar
                val hex = ByteBufUtil.hexDump(r.buffer, r.buffer.readerIndex(), remainingInBlock)
                parts += "${key.name}=<unconfirmed, ${remainingInBlock}b, hex=$hex>"
                r.skipRead(remainingInBlock)
                break
            }
            parts += decodeKnownBlock(key, r)
        }
        val consumed = startRemaining - r.readableBytes()
        if (consumed < blockSize) r.skipRead(blockSize - consumed)
        return parts
    }

    private fun lowResSlots(parity: Boolean): List<Int> = (1..MAX_SLOT).filter { !highRes[it] && nsn[it] == parity }

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
                "byteOffset=${bits.readerIndex() / 8} (matches hexDump's [offset] labels directly)"
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
                val speed = bits.readBits(3)
                val plane = bits.readBits(2)
                val dx: Int
                val dz: Int
                if (big) {
                    dx = signExtend(bits.readBits(14), 14)
                    dz = signExtend(bits.readBits(14), 14)
                } else {
                    dx = signExtend(bits.readBits(5), 5)
                    dz = signExtend(bits.readBits(5), 5)
                }
                if (!big) {
                    if (highResSeeded[slot]) {
                        highResX[slot] += dx
                        highResZ[slot] += dz
                        highResPlane[slot] = plane
                    }
                    val posStr2 = if (highResSeeded[slot]) {
                        " pos=${formatCoord(highResPlane[slot], highResX[slot], highResZ[slot])}"
                    } else {
                        ""
                    }
                    if (dx != 0 || dz != 0 || plane != 0 || hasExt) {
                        sb.append(
                            "\n  TELEPORT ${slotLabel(slot)} big=false speed=$speed plane=$plane " +
                                "dx=$dx dz=$dz ext=$hasExt$posStr2",
                        )
                    }
                } else {
                    val asDelta =
                        if (highResSeeded[slot]) {
                            formatCoord(plane, highResX[slot] + dx, highResZ[slot] + dz)
                        } else {
                            "unknown (no prior seeded position)"
                        }
                    val asAbsolute = formatCoord(plane, dx, dz)
                    highResPlane[slot] = plane
                    highResSeeded[slot] = false
                    sb.append(
                        "\n  TELEPORT ${slotLabel(slot)} big=true speed=$speed plane=$plane raw=($dx,$dz) " +
                            "ext=$hasExt asDelta=$asDelta asAbsolute=$asAbsolute " +
                            "[genuinely ambiguous for big=true - see kdoc]",
                    )
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
        val bitPosBeforeLeadCode = bits.readerIndex()
        val leadCode = bits.readBits(2)
        val diag = "pass=$passLabel cohortSize=$cohortSize index=$indexInCohort bitPosBeforeRecord=$bitPosBeforeLeadCode byteOffset=${bitPosBeforeLeadCode / 8}"
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
            sb.append(
                "\n  LOWRES_PROMOTE ${slotLabel(slot)}${if (depth > 0) " depth=$depth" else ""} " +
                    "stale=$stale move=$moveStr fineX=$fineX fineZ=$fineZ promote=$promoteFlag",
            )
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
                else -> "unexpected code $code slot=$slot [$diag] - writeLowResMove's real source " +
                    "never emits this leading code, so a bit-miscount happened somewhere before " +
                    "this point in this same cohort pass; this context is what's needed to trace it"
            }
        lowResSeeded[slot] = true
        val text = if (!hadBaseline) "$result (first sighting, prior position was unknown)" else result
        return text to (isNoOp && hadBaseline)
    }

    private fun signExtend(
        value: Int,
        bitWidth: Int,
    ): Int {
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
                if (key in Rs3PlayerUpdateMaskKey.MECHANICAL) continue
                val consumedSoFar = startRemaining - r.readableBytes()
                if (consumedSoFar >= blockSize) break
                if (key !in Rs3PlayerUpdateMaskKey.DECODABLE) {
                    val remainingInBlock = blockSize - consumedSoFar
                    val hex = ByteBufUtil.hexDump(r.buffer, r.buffer.readerIndex(), remainingInBlock)
                    parts += "${key.name}=<unconfirmed, ${remainingInBlock}b, hex=$hex>"
                    r.skipRead(remainingInBlock)
                    break
                }
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

    private class ExtendedInfoBlockException(val partial: String, cause: Exception) : Exception(cause)

    private fun decodeSlotBitmaskBlock(
        r: JagByteBuf,
        label: String,
        len: Int,
    ): String {
        val startRemaining = r.readableBytes()
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
        val consumed = startRemaining - r.readableBytes()
        if (consumed != len) {
            r.buffer.readerIndex(r.buffer.writerIndex() - (startRemaining - len))
        }
        return "$label(len=$len slotMask=0x${slotMask.toString(16)} slots=[${slots.joinToString(",")}])"
    }

    private fun decodeKnownBlock(
        key: Rs3PlayerUpdateMaskKey,
        r: JagByteBuf,
    ): String =
        when (key) {
            Rs3PlayerUpdateMaskKey.UNK_BIT13 -> {
                r.g2Alt3()
                r.g4Alt2()
                r.g1Alt3()
                "UNK_BIT13(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT9 -> {
                r.g2Alt1()
                r.g4Alt2()
                r.g1Alt1()
                "UNK_BIT9(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT3 -> {
                r.g2Alt1()
                r.g4()
                r.g1()
                "UNK_BIT3(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT23 -> {
                r.g1Alt1()
                r.g2Alt3()
                r.g2Alt3()
                r.g2Alt2()
                "UNK_BIT23(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT27 -> {
                r.g2Alt3()
                r.g4Alt2()
                r.g1()
                "UNK_BIT27(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.FACE_ENTITY -> {
                r.g1Alt2()
                r.g1Alt3()
                r.g2Alt3()
                "FACE_ENTITY(consumed, confirmed discarded by client - see kdoc)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT16 -> {
                r.g2Alt2()
                r.g4Alt1()
                r.g1()
                "UNK_BIT16(consumed, discarded by client)"
            }
            Rs3PlayerUpdateMaskKey.UNK_BIT7 -> "UNK_BIT7(v=${r.g2Alt3()})"
            Rs3PlayerUpdateMaskKey.UNK_BIT11 -> "UNK_BIT11(v=${r.g1()})"
            Rs3PlayerUpdateMaskKey.OVERHEAD_TEXT -> "OVERHEAD_TEXT(v=${r.g3Alt1()})"
            Rs3PlayerUpdateMaskKey.TRANSIENT_BOOL -> "TRANSIENT_BOOL(${r.g1Alt1() == 1})"

            Rs3PlayerUpdateMaskKey.ANIMATION -> {
                val layer0 = r.gSmart2or4null()
                val layer1 = r.gSmart2or4null()
                val layer2 = r.gSmart2or4null()
                val layer3 = r.gSmart2or4null()
                val delay = r.g1()
                "ANIMATION(layer0=$layer0 layer1=$layer1 layer2=$layer2 layer3=$layer3 delay=$delay)"
            }

            Rs3PlayerUpdateMaskKey.FORCED_MOVEMENT -> {
                val d1 = r.g1s()
                val d2 = r.g1Alt1()
                val d3 = r.g1s()
                val d4 = r.g1Alt3()
                val d5 = r.g1Alt3()
                val d6 = r.g1Alt1()
                val v1 = r.g2Alt2()
                val v2 = r.g2Alt2()
                val v3 = r.g2()
                "FORCED_MOVEMENT(d1=$d1 d2=$d2 d3=$d3 d4=$d4 d5=$d5 d6=$d6 v1=$v1 v2=$v2 v3=$v3)"
            }

            Rs3PlayerUpdateMaskKey.COMBAT_LEVEL_OVERRIDE_RGB -> {
                val hue = r.g1Alt3()
                val sat = r.g1Alt3()
                val lum = r.g1Alt1()
                val brightness = r.g1Alt1()
                val startCycle = r.g2()
                val endCycle = r.g2Alt3()
                "COMBAT_LEVEL_OVERRIDE_RGB(hue=$hue sat=$sat lum=$lum bright=$brightness cycle=$startCycle..$endCycle)"
            }

            Rs3PlayerUpdateMaskKey.TRANSFORM_BLEND -> decodeTransformBlend(r)
            Rs3PlayerUpdateMaskKey.HITMARKS_AND_HEADBARS_2 -> decodeWideHitmarksAndHeadbars(r)
            Rs3PlayerUpdateMaskKey.HITMARKS_AND_HEADBARS -> decodeNarrowHitmarksAndHeadbars(r)
            Rs3PlayerUpdateMaskKey.RAW_BLOB_A -> decodeSlotBitmaskBlock(r, "RAW_BLOB_A", r.g1Alt3())

            Rs3PlayerUpdateMaskKey.APPEARANCE -> {
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
                val ascii =
                    decoded.joinToString("") { d -> if (d in 32..126) d.toChar().toString() else "." }
                val longestRun = ascii.split(".").filter { it.length >= 3 }.maxByOrNull { it.length }
                val slot = currentDecodingSlot
                if (slot != null && longestRun != null) {
                    playerNameByIndex[slot] = longestRun
                }
                var tail = ""
                if (longestRun != null) {
                    val nameStart = ascii.indexOf(longestRun)
                    var nameEnd = nameStart + longestRun.length
                    while (nameEnd < decoded.size && decoded[nameEnd] != 0) nameEnd++
                    var q = nameEnd + 1
                    if (q < decoded.size) {
                        val pn1 = decoded[q]; q++
                        val morph = (flags and 0x04) != 0
                        val headIcon0: Int
                        val headIcon1: Int
                        val morphNpc: Int?
                        if (morph && q + 1 < decoded.size) {
                            morphNpc = (decoded[q] shl 8) or decoded[q + 1]
                            q += 2
                            headIcon0 = pn1
                            headIcon1 = -1
                        } else if (!morph && q < decoded.size) {
                            headIcon0 = pn1
                            headIcon1 = decoded[q].let { if (it == 0xFF) -1 else it }
                            q++
                            morphNpc = null
                        } else {
                            headIcon0 = pn1; headIcon1 = -1; morphNpc = null
                        }
                        val colourFlag = decoded.getOrNull(q)
                        var colorsStr = "none"
                        if (colourFlag != null) {
                            q++
                            if (colourFlag != 0 && q + 8 < decoded.size) {
                                val c0 = (decoded[q] shl 8) or decoded[q + 1]
                                val c1 = (decoded[q + 2] shl 8) or decoded[q + 3]
                                val c2 = (decoded[q + 4] shl 8) or decoded[q + 5]
                                val c3 = (decoded[q + 6] shl 8) or decoded[q + 7]
                                val skin = decoded.getOrNull(q + 8)
                                colorsStr = "c0=$c0 c1=$c1 c2=$c2 c3=$c3 skin=$skin"
                            }
                        }
                        tail = " headIcon0=$headIcon0 headIcon1=$headIcon1 morphNpc=$morphNpc colourFlag=$colourFlag colors=$colorsStr"
                    }
                }
                "APPEARANCE(len=$len flags=0x${flags.toString(16)} titleId=$titleId " +
                    "objOverrides=$objOverrideCount displayMode=$displayMode name=\"$longestRun\"$tail " +
                    "bytes=[${bytes.joinToString(",") { "%02x".format(it) }}] asciiByteAdd=\"$ascii\")"
            }

            Rs3PlayerUpdateMaskKey.NAME_BLOCK_A -> "NAME_BLOCK_A(\"${r.gjstr()}\")"

            Rs3PlayerUpdateMaskKey.NAME_BLOCK_B -> {
                val name = r.gjstr()
                val flags = r.g1()
                "NAME_BLOCK_B(\"$name\" flags=$flags)"
            }

            Rs3PlayerUpdateMaskKey.PARAMS_B -> decodeSimpleParamsList(r) { it.g1Alt1() }
            Rs3PlayerUpdateMaskKey.PARAMS_C -> decodeSimpleParamsList(r) { it.g1() }
            Rs3PlayerUpdateMaskKey.PARAMS_A -> decodeParamsAWithCleanup(r)

            else -> error("unreachable: $key is not in Rs3PlayerUpdateMaskKey.DECODABLE")
        }

    private fun decodeSimpleParamsList(
        r: JagByteBuf,
        readType: (JagByteBuf) -> Int,
    ): String {
        r.g2()
        val count = r.g1()
        val entries = mutableListOf<String>()
        for (i in 0 until count) {
            val type = readType(r)
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
                    entries += "key=$key type=$type <unrecognized type, bailing>"
                    break
                }
            }
        }
        return "PARAMS(entries=[${entries.joinToString(",")}])"
    }

    private fun decodeParamsAWithCleanup(r: JagByteBuf): String {
        val cleanupCount = r.g1()
        val cleanupKeys = mutableListOf<Int>()
        for (i in 0 until cleanupCount) {
            val key = r.g2()
            if (key == 0xFFFF) break
            cleanupKeys.add(key)
        }
        val applyCount = r.g1()
        val applyEntries = mutableListOf<String>()
        for (i in 0 until applyCount) {
            val a = r.g1Alt3()
            val b = r.g2Alt3()
            val c = r.g4Alt1()
            val d = r.g1Alt2()
            val e = r.g3Alt3()
            applyEntries += "a=$a b=$b c=$c d=$d e=$e"
        }
        return "PARAMS_A(cleanup=[${cleanupKeys.joinToString(",")}] apply=[${applyEntries.joinToString(",")}])"
    }

    private fun decodeNarrowHitmarksAndHeadbars(r: JagByteBuf): String {
        val hitCount = r.g1()
        val hits = mutableListOf<String>()
        repeat(hitCount) { hits += decodeHitEntryNarrow(r) }
        val barCount = r.g1()
        val bars = (0 until barCount).map { decodeHeadbarNarrow(r) }
        return "HITMARKS_AND_HEADBARS(hits=[${hits.joinToString(",")}] bars=[${bars.joinToString(",")}])"
    }

    private fun readSmart1or2(r: JagByteBuf): Int {
        val b0 = r.g1()
        if (b0 < 0x80) return b0
        val b1 = r.g1()
        return ((b0 and 0x7F) shl 8) or b1
    }

    private fun readSmart1or2Minus1(r: JagByteBuf): Int = readSmart1or2(r) - 1

    private fun decodeHitEntryNarrow(r: JagByteBuf): String {
        var type = readSmart1or2(r)
        val body: String
        if (type == 0x7fff) {
            type = readSmart1or2(r)
            val damage = readSmart1or2(r)
            val soak = readSmart1or2(r)
            val extra = readSmart1or2(r)
            body = "big(type=$type damage=$damage soak=$soak extra=$extra)"
        } else if (type == 0x7ffe) {
            val soak = r.g1Alt1()
            body = "soak(soak=$soak)"
        } else {
            val damage = readSmart1or2(r)
            body = "normal(type=$type damage=$damage)"
        }
        val delay = readSmart1or2(r)
        return "$body delay=$delay"
    }

    private fun decodeHeadbarNarrow(r: JagByteBuf): String {
        val sel = readSmart1or2(r)
        val durationCheck = readSmart1or2(r)
        if (durationCheck == 0x7fff) return "sel=$sel REMOVE"
        val duration = readSmart1or2(r)
        val front = r.g1Alt1()
        val back: Int
        val transId: Int
        val front2: Int?
        val back2: Int?
        if (durationCheck == 0) {
            transId = readSmart1or2Minus1(r)
            if (transId < 0) {
                back = front
                front2 = null
                back2 = null
            } else {
                back = r.g1Alt1()
                front2 = null
                back2 = null
            }
        } else {
            back = r.g1Alt1()
            transId = readSmart1or2Minus1(r)
            if (transId >= 0) {
                front2 = r.g1Alt2()
                back2 = r.g1Alt1()
            } else {
                front2 = null
                back2 = null
            }
        }
        return "sel=$sel duration=$duration front=$front back=$back transId=$transId" +
            (if (front2 != null) " front2=$front2 back2=$back2" else "")
    }

    private fun readFun0021e6b0(r: JagByteBuf): Int {
        val b0 = r.g1()
        if (b0 < 0x80) return b0 - 1
        val b1 = r.g1()
        var v = (b0 shl 8) or b1
        if (v >= 0x8000) v -= 0x10000
        return v + 0x7fff
    }

    private fun decodeTransformBlend(r: JagByteBuf): String {
        val countRaw = r.g1Alt3()
        val count = countRaw.toByte().toInt()
        if (count <= 0) return "TRANSFORM_BLEND(count=$count)"
        val entries =
            (0 until count).joinToString(",") {
                val flags = r.g2Alt1()
                val secondary = r.g2Alt2()
                val extra = if ((flags and 0xC00) != 0) r.g4() else null
                val f1 = if ((flags and 0x1) != 0) r.g4Alt3() else null
                val f2 = if ((flags and 0x2) != 0) r.g4() else null
                val f4 = if ((flags and 0x4) != 0) r.g4() else null
                val f8 = if ((flags and 0x8) != 0) r.g4Alt3() else null
                val f10 = if ((flags and 0x10) != 0) r.g4() else null
                val f20 = if ((flags and 0x20) != 0) r.g4Alt2() else null
                val f80 = if ((flags and 0x80) != 0) r.g4() else null
                val f100 = if ((flags and 0x100) != 0) r.g4Alt3() else null
                val f200 = if ((flags and 0x200) != 0) r.g4Alt2() else null
                "flags=0x${flags.toString(16)} sec=$secondary extra=$extra f1=$f1 f2=$f2 " +
                    "f4=$f4 f8=$f8 f10=$f10 f20=$f20 f80=$f80 f100=$f100 f200=$f200"
            }
        return "TRANSFORM_BLEND(count=$count entries=[$entries])"
    }

    private fun decodeWideHitmarksAndHeadbars(r: JagByteBuf): String {
        val hitCount = r.g1Alt1()
        val hits =
            (0 until hitCount).joinToString(",") {
                val v1 = r.gSmart1or2()
                val body =
                    when (v1) {
                        0x7fff -> {
                            val a = r.gSmart1or2()
                            val b = r.g4Alt3()
                            val c = r.gSmart1or2()
                            val d = r.g4Alt1()
                            "wide(a=$a b=$b c=$c d=$d)"
                        }
                        0x7ffe -> "compact(a=${r.g1Alt3()})"
                        else -> "normal(v1=$v1 a=${r.g4Alt3()})"
                    }
                "$body delay=${r.gSmart1or2()}"
            }
        val barCount = r.g1Alt1()
        val bars = (0 until barCount).joinToString(",") { decodeHeadbarWide(r) }
        return "HITMARKS_AND_HEADBARS_2(hits=[$hits] bars=[$bars])"
    }

    private fun decodeHeadbarWide(r: JagByteBuf): String {
        val sel = readSmart1or2(r)
        val durationCheck = readSmart1or2(r)
        if (durationCheck == 0x7fff) return "sel=$sel REMOVE"
        val duration = readSmart1or2(r)
        val front = r.g1Alt3()
        val back: Int
        val transId: Int
        val front2: Int?
        val back2: Int?
        if (durationCheck == 0) {
            transId = readSmart1or2Minus1(r)
            if (transId < 0) {
                back = front
                front2 = null
                back2 = null
            } else {
                back = r.g1Alt1()
                front2 = null
                back2 = null
            }
        } else {
            back = r.g1Alt1()
            transId = readSmart1or2Minus1(r)
            if (transId >= 0) {
                front2 = r.g1Alt1()
                back2 = r.g1Alt3()
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
