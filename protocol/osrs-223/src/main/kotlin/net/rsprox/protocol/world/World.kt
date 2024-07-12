package net.rsprox.protocol.world

import net.rsprot.compression.HuffmanCodec
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoClient

public class World(
    localPlayerIndex: Int,
    huffmanCodec: HuffmanCodec,
) {
    public val playerInfo: PlayerInfoClient = PlayerInfoClient(localPlayerIndex, huffmanCodec)
    public var baseX: Int = -1
    public var baseZ: Int = -1
}
