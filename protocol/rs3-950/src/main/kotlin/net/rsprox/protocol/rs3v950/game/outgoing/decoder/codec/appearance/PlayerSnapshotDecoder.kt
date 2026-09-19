package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.appearance

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.PlayerSnapshot
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.PlayerAppearanceDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PlayerSnapshotDecoder : ProxyMessageDecoder<PlayerSnapshot> {
    override val prot: ClientProt = GameServerProt.PLAYER_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerSnapshot {
        val snapshotIndex = buffer.g1()
        val appearanceFlags = buffer.g1s()
        return PlayerSnapshot(
            snapshotIndex,
            appearanceFlags,
            PlayerAppearanceDecoder.decodeBody(
                buffer,
                checkNotNull(session.rs3AppearanceDefinitions) {
                    "RS3 appearance cache is not initialized"
                },
            ),
        )
    }
}
