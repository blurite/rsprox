package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.appearance

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.LobbyAppearance
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.PlayerAppearanceDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LobbyAppearanceDecoder : ProxyMessageDecoder<LobbyAppearance> {
    override val prot: ClientProt = GameServerProt.LOBBY_APPEARANCE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LobbyAppearance {
        val appearanceFlags = buffer.g1s()
        return LobbyAppearance(
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
