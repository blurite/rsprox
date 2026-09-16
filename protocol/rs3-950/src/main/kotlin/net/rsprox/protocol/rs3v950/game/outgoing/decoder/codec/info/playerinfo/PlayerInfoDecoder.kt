package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3PlayerInfoInitPending
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

private var Session.playerInfo: PlayerInfoClient? by attribute()

internal class PlayerInfoDecoder : ProxyMessageDecoder<PlayerInfo> {
    override val prot: ClientProt = GameServerProt.PLAYER_INFO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerInfo =
        checkNotNull(session.playerInfo) {
            "PLAYER_INFO requires the initial rebuild's player table"
        }.decode(buffer)
}

/** Native 0x00cd1a00, called before either initial rebuild body. Not a packet-size heuristic. */
internal fun Session.readPlayerInfoInit(buffer: JagByteBuf): PlayerInfoInitBlock? {
    if (rs3PlayerInfoInitPending != true) return null
    require(localPlayerIndex in 1..2047) { "Invalid login player index $localPlayerIndex" }
    val positions = IntArray(2048)
    val local =
        buffer.buffer.toBitBuf().use { bits ->
            require(bits.isReadable(30 + 2046 * 20)) { "Truncated initial player table" }
            val position = bits.gBits(30)
            for (index in 1..2047) {
                if (index != localPlayerIndex) positions[index] = bits.gBits(20)
            }
            position
        }
    return PlayerInfoInitBlock(localPlayerIndex, local ushr 28, local ushr 14 and 0x3fff, local and 0x3fff, positions)
}

/** Commit only after the entire rebuild has decoded successfully. */
internal fun Session.initializePlayerInfo(init: PlayerInfoInitBlock?) {
    if (init == null) return
    playerInfo = PlayerInfoClient(init, rs3AppearanceDefinitions)
    rs3PlayerInfoInitPending = false
}

internal fun Session.localPlayerCoordinate(): CoordGrid =
    checkNotNull(playerInfo) { "Local player position requires the initial rebuild" }.localCoordinate()
