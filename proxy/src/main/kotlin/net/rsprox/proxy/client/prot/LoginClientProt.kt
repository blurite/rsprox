package net.rsprox.proxy.client.prot

import net.rsprot.protocol.ClientProt

public enum class LoginClientProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    INIT_GAME_CONNECTION(LoginClientProtId.INIT_GAME_CONNECTION, 0),
    INIT_JS5REMOTE_CONNECTION(LoginClientProtId.INIT_JS5REMOTE_CONNECTION, 20),
    GAMELOGIN(LoginClientProtId.GAMELOGIN, -2),
    GAMERECONNECT(LoginClientProtId.GAMERECONNECT, -2),
    POW_REPLY(LoginClientProtId.POW_REPLY, -2),
    UNKNOWN(LoginClientProtId.UNKNOWN, 37),
    SSL_WEB_CONNECTION(LoginClientProtId.SSL_WEB_CONNECTION, 0),
    REMAINING_BETA_ARCHIVE_CRCS_V1(LoginClientProtId.REMAINING_BETA_ARCHIVE_HASHES_V1, 58),
    REMAINING_BETA_ARCHIVE_CRCS_V2(LoginClientProtId.REMAINING_BETA_ARCHIVE_HASHES_V2, 66),
}
