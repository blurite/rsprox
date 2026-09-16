package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.localPlayerCoordinate
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

private var Session.npcInfo: NpcInfoState? by attribute()
private var Session.npcCoordinateBits: Int? by attribute()

internal fun Session.initializeNpcInfo(
    coordinateBits: Int,
    initial: Boolean,
) {
    require(coordinateBits in 1..31) { "Unsupported NPC coordinate width $coordinateBits" }
    npcCoordinateBits = coordinateBits
    if (initial) npcInfo = NpcInfoState()
}

/** The repository holds this stateless adapter; actor history belongs to the connection. */
public class NpcInfoClient : ProxyMessageDecoder<NpcInfo> {
    override val prot: ClientProt = GameServerProt.NPC_INFO_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcInfo =
        checkNotNull(session.npcInfo) { "NPC_INFO_V2 requires the initial rebuild" }.decode(
            buffer,
            session.localPlayerCoordinate(),
            checkNotNull(session.npcCoordinateBits),
            session.rs3PacketDefinitions,
            session.npcMorphVariables(),
        )
}

/** Decode transactionally; a rejected relative frame requires a fresh initial rebuild. */
internal class NpcInfoState {
    private data class Npc(
        val id: Int,
        val coord: CoordGrid,
    )

    private var synchronized = true
    private var active = linkedMapOf<Int, Npc>()

    fun decode(
        buffer: JagByteBuf,
        base: CoordGrid,
        coordinateBits: Int,
        definitions: Rs3PacketDefinitions?,
        variables: NpcMorphVariables = NpcMorphVariables(),
    ): NpcInfo {
        check(synchronized) { "NPC_INFO_V2 state lost after a failed frame; a fresh login/rebuild is required" }
        try {
            val next = linkedMapOf<Int, Npc>()
            val updates = linkedMapOf<Int, NpcUpdateType>()
            val masks = mutableListOf<Int>()
            val previous = active.entries.toList()
            buffer.buffer.toBitBuf().use { bits ->
                val count = bits.read(8)
                require(count <= previous.size) { "NPC count $count exceeds previous count ${previous.size}" }
                for (position in count until previous.size) updates[previous[position].key] = NpcUpdateType.Remove
                for (position in 0 until count) {
                    val (index, npc) = previous[position]
                    if (bits.read(1) == 0) {
                        next[index] = npc
                        updates[index] = NpcUpdateType.Idle
                        continue
                    }
                    val kind = bits.read(2)
                    if (kind == 3) {
                        updates[index] = NpcUpdateType.Remove
                        continue
                    }
                    var first: Int? = null
                    var second: Int? = null
                    val movement =
                        when (kind) {
                            0 -> NpcUpdateType.MovementType.EXT_ONLY
                            1 -> {
                                first = bits.read(3)
                                NpcUpdateType.MovementType.WALK
                            }
                            else -> {
                                val run = bits.read(1) != 0
                                first = bits.read(3)
                                if (run) second = bits.read(3)
                                if (run) NpcUpdateType.MovementType.RUN else NpcUpdateType.MovementType.CRAWL
                            }
                        }
                    if (kind == 0 || bits.read(1) != 0) masks += index
                    var coord = npc.coord
                    first?.let { coord = step(coord, it) }
                    second?.let { coord = step(coord, it) }
                    next[index] = npc.copy(coord = coord)
                    updates[index] =
                        NpcUpdateType.Active(
                            movement,
                            first,
                            second,
                            coord.level,
                            coord.x,
                            coord.z,
                            emptyList(),
                        )
                }
                while (bits.readableBits() >= 16) {
                    val index = bits.read(16)
                    if (index == 65535) break
                    require(index !in next) { "Duplicate NPC index $index" }
                    val z = bits.signed(coordinateBits)
                    val teleport = bits.read(1) != 0
                    val direction = bits.read(3)
                    val id = bits.read(16)
                    val level = bits.read(2)
                    val hasMask = bits.read(1) != 0
                    val x = bits.signed(coordinateBits)
                    val coord = CoordGrid(level, base.x + x, base.z + z)
                    next[index] = Npc(id, coord)
                    updates[index] =
                        NpcUpdateType.Add(
                            id,
                            x,
                            z,
                            level,
                            coord.x,
                            coord.z,
                            direction,
                            emptyList(),
                            teleport,
                        )
                    if (hasMask) masks += index
                }
            }
            for (index in masks) {
                // Native advances by two bytes; this is NOT a payload-length field.
                buffer.g2()
                val npc = next.getValue(index)
                val extendedInfo = NpcExtendedInfoDecoder.decode(buffer, npc.id, definitions, variables)
                val morph = extendedInfo.filterIsInstance<NpcMask.Transformation>().lastOrNull()
                if (morph != null) next[index] = npc.copy(id = morph.id)
                updates[index] =
                    when (val update = updates.getValue(index)) {
                        is NpcUpdateType.Active -> update.copy(extendedInfo = extendedInfo)
                        is NpcUpdateType.Add -> update.copy(extendedInfo = extendedInfo)
                        else -> error("NPC mask without an update for $index")
                    }
            }
            require(buffer.readableBytes() == 0) { "NPC_INFO_V2 has ${buffer.readableBytes()} trailing bytes" }
            active = next
            return NpcInfo(updates)
        } catch (exception: Exception) {
            synchronized = false
            throw exception
        }
    }

    private fun step(
        coord: CoordGrid,
        direction: Int,
    ): CoordGrid = CoordGrid(coord.level, coord.x + DX[direction], coord.z + DZ[direction])

    private fun BitBuf.read(count: Int): Int {
        require(isReadable(count)) { "Truncated NPC bit block" }
        return gBits(count)
    }

    private fun BitBuf.signed(count: Int): Int = read(count) shl (32 - count) shr (32 - count)

    private companion object {
        // Native 0x24a300: N, NE, E, SE, S, SW, W, NW (not the OSRS table).
        val DX = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
        val DZ = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
    }
}
