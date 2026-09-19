package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

/** Per-connection state. Never shared by a decoder repository or committed from a failed frame. */
internal class PlayerInfoClient(
    init: PlayerInfoInitBlock,
    private val appearanceDefinitions: Rs3AppearanceDefinitions? = null,
) {
    private data class Player(
        var highResolution: Boolean = false,
        var oldSkip: Boolean = false,
        var level: Int = 0,
        var x: Int = 0,
        var z: Int = 0,
        var movementMode: Int = 0,
    )

    private val localIndex = init.localPlayerIndex
    private var synchronized = true
    private var players =
        Array(2048) { index ->
            if (index == localIndex) {
                Player(true, false, init.localPlayerLevel, init.localPlayerX, init.localPlayerZ)
            } else {
                val packed = init.getLowResolutionPosition(index)
                val mode = packed ushr 18 and 3
                Player(false, mode == 0, packed ushr 16 and 3, packed ushr 8 and 255, packed and 255, mode)
            }
        }

    fun localCoordinate(): CoordGrid {
        check(synchronized) { "Local player position is unavailable after a failed PLAYER_INFO frame" }
        val player = players[localIndex]
        return CoordGrid(player.level, player.x, player.z)
    }

    fun decode(buffer: JagByteBuf): PlayerInfo {
        check(synchronized) { "PLAYER_INFO state lost after a failed frame; a fresh login/rebuild is required" }
        val working = Array(players.size) { players[it].copy() }
        try {
            val frame = Frame(working)
            val result = frame.decode(buffer)
            players = working
            return result
        } catch (exception: Exception) {
            // The next frame is relative to the client's NEW state, not our last successful one.
            synchronized = false
            throw exception
        }
    }

    private inner class Frame(
        private val state: Array<Player>,
    ) {
        private val updates = linkedMapOf<Int, PlayerUpdateType>()
        private val masks = mutableListOf<Int>()
        private val nextSkip = BooleanArray(2048)

        fun decode(buffer: JagByteBuf): PlayerInfo {
            // Snapshot all four cohorts before additions/removals change resolution.
            val phases = listOf(true to false, true to true, false to true, false to false)
            val cohorts =
                phases.map { (high, skip) ->
                    (1..2047).filter { state[it].highResolution == high && state[it].oldSkip == skip }
                }
            buffer.buffer.toBitBuf().use { bits ->
                for ((phase, indices) in cohorts.withIndex()) {
                    var cursor = 0
                    while (cursor < indices.size) {
                        val index = indices[cursor]
                        if (bits.read(1) == 0) {
                            val width = intArrayOf(0, 5, 8, 11)[bits.read(2)]
                            val count = if (width == 0) 0 else bits.read(width)
                            require(count < indices.size - cursor) { "Player skip exceeds phase $phase" }
                            repeat(count + 1) { nextSkip[indices[cursor++]] = true }
                        } else {
                            if (phase < 2) existing(bits, index) else nextSkip[index] = external(bits, index)
                            cursor++
                        }
                    }
                    bits.readerIndex((bits.readerIndex() + 7) and -8)
                }
            }
            for (index in masks) {
                // Native outer handler skips these bytes; it never treats them as a block length.
                buffer.g2()
                val extendedInfo = PlayerExtendedInfoDecoder.decode(buffer, appearanceDefinitions)
                updates[index] =
                    when (val update = updates[index]) {
                        is PlayerUpdateType.HighResolutionIdle -> update.copy(extendedInfo = extendedInfo)
                        is PlayerUpdateType.LowResolutionToHighResolution -> update.copy(extendedInfo = extendedInfo)
                        is PlayerUpdateType.HighResolutionMovement -> update.copy(extendedInfo = extendedInfo)
                        else -> error("Mask without an active player update: $index")
                    }
            }
            require(buffer.readableBytes() == 0) { "PLAYER_INFO has ${buffer.readableBytes()} trailing bytes" }
            for (index in 1..2047) state[index].oldSkip = nextSkip[index]
            return PlayerInfo(updates)
        }

        private fun existing(
            bits: BitBuf,
            index: Int,
        ) {
            val player = state[index]
            val hasMasks = bits.read(1) != 0
            if (hasMasks) masks += index
            when (bits.read(2)) {
                0 -> {
                    if (hasMasks || index == localIndex) {
                        updates[index] = PlayerUpdateType.HighResolutionIdle(emptyList())
                        return
                    }
                    player.highResolution = false
                    player.x = player.x shr 6
                    player.z = player.z shr 6
                    updates[index] = PlayerUpdateType.HighResolutionToLowResolution(player.level, player.x, player.z)
                    if (bits.read(1) != 0) {
                        external(bits, index)
                        // Region-only follow-ups must not hide the removal in the transcript.
                        if (!player.highResolution) {
                            updates[index] =
                                PlayerUpdateType.HighResolutionToLowResolution(
                                    player.level,
                                    player.x,
                                    player.z,
                                )
                        }
                    }
                }
                1 -> {
                    val direction = bits.read(3)
                    val extra = bits.read(1) != 0
                    val steps = mutableListOf<PlayerUpdateType.Step>()
                    if (extra) {
                        val cardinal = bits.read(2)
                        steps += PlayerUpdateType.Step(player.x + CARDINAL_X[cardinal], player.z + CARDINAL_Z[cardinal])
                    }
                    // Both native waypoints are relative to the original position, not to each other.
                    steps += PlayerUpdateType.Step(player.x + WALK_X[direction], player.z + WALK_Z[direction])
                    move(index, false, player.level, steps, if (extra) 3 else player.movementMode)
                }
                2 -> {
                    val direction = bits.read(4)
                    val step = PlayerUpdateType.Step(player.x + RUN_X[direction], player.z + RUN_Z[direction])
                    move(index, false, player.level, listOf(step), player.movementMode)
                }
                3 -> {
                    val large = bits.read(1) != 0
                    val mode: Int
                    val level: Int
                    val x: Int
                    val z: Int
                    if (large) {
                        mode = bits.read(3)
                        val packed = bits.read(30)
                        level = (player.level + (packed ushr 28)) and 3
                        x = (player.x + (packed ushr 14 and 0x3fff)) and 0x3fff
                        z = (player.z + (packed and 0x3fff)) and 0x3fff
                    } else {
                        val packed = bits.read(15)
                        mode = packed ushr 12
                        level = (player.level + (packed ushr 10 and 3)) and 3
                        x = player.x + ((packed ushr 5 and 31) shl 27 shr 27)
                        z = player.z + ((packed and 31) shl 27 shr 27)
                    }
                    val previousMode = player.movementMode
                    move(index, mode == 4, level, listOf(PlayerUpdateType.Step(x, z)), mode)
                    // Large instant teleports retain the cached mode; small ones reset it.
                    if (mode == 4) player.movementMode = if (large) previousMode else 0
                }
            }
        }

        private fun move(
            index: Int,
            teleport: Boolean,
            level: Int,
            steps: List<PlayerUpdateType.Step>,
            mode: Int,
        ) {
            val player = state[index]
            player.level = level
            player.x = steps.last().x
            player.z = steps.last().z
            player.movementMode = mode
            updates[index] =
                PlayerUpdateType.HighResolutionMovement(
                    teleport,
                    level,
                    player.x,
                    player.z,
                    mode,
                    steps,
                    emptyList(),
                )
        }

        private fun external(
            bits: BitBuf,
            index: Int,
            depth: Int = 0,
        ): Boolean {
            require(depth < 32) { "Excessive recursive external-player updates" }
            val player = state[index]
            when (bits.read(2)) {
                0 -> {
                    if (bits.read(1) != 0) external(bits, index, depth + 1)
                    require(!player.highResolution) { "Duplicate player add: $index" }
                    val x = bits.read(6)
                    val z = bits.read(6)
                    if (bits.read(1) != 0) masks += index
                    player.highResolution = true
                    player.x = (player.x shl 6) + x
                    player.z = (player.z shl 6) + z
                    updates[index] =
                        PlayerUpdateType.LowResolutionToHighResolution(
                            player.level,
                            player.x,
                            player.z,
                            player.movementMode,
                            emptyList(),
                        )
                    return true
                }
                1 -> player.level = (player.level + bits.read(2)) and 3
                2 -> {
                    val packed = bits.read(5)
                    player.level = (player.level + (packed ushr 3)) and 3
                    // Native reads the previous regions as unsigned bytes, then stores full integer deltas.
                    player.x = (player.x and 255) + WALK_X[packed and 7]
                    player.z = (player.z and 255) + WALK_Z[packed and 7]
                }
                3 -> {
                    val packed = bits.read(20)
                    player.movementMode = packed ushr 18
                    player.level = (player.level + (packed ushr 16 and 3)) and 3
                    player.x = (player.x + (packed ushr 8 and 255)) and 255
                    player.z = (player.z + (packed and 255)) and 255
                }
            }
            updates[index] =
                PlayerUpdateType.LowResolutionMovement(
                    player.level,
                    player.x,
                    player.z,
                    player.movementMode,
                )
            return false
        }
    }

    private companion object {
        val WALK_X = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        val WALK_Z = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)
        val RUN_X = intArrayOf(-2, -1, 0, 1, 2, -2, 2, -2, 2, -2, 2, -2, -1, 0, 1, 2)
        val RUN_Z = intArrayOf(-2, -2, -2, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 2, 2, 2)
        val CARDINAL_X = intArrayOf(0, -1, 1, 0)
        val CARDINAL_Z = intArrayOf(1, 0, 0, -1)
    }
}

private fun BitBuf.read(count: Int): Int {
    require(isReadable(count)) { "Truncated player movement ($count bits required)" }
    return gBits(count)
}
