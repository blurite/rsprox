package net.rsprox.proxy.rs3

import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprox.protocol.rs3v950.ClientPacketDecoderServiceRs3V950
import net.rsprox.protocol.rs3v950.GameClientProtProviderRs3V950
import net.rsprox.protocol.rs3v950.GameServerProtProviderRs3V950
import net.rsprox.protocol.rs3v950.ServerPacketDecoderServiceRs3V950
import net.rsprox.protocol.rs3v950beta.ClientPacketDecoderServiceRs3V950Beta
import net.rsprox.protocol.rs3v950beta.GameClientProtProviderRs3V950Beta
import net.rsprox.protocol.rs3v950beta.GameServerProtProviderRs3V950Beta
import net.rsprox.protocol.rs3v950beta.ServerPacketDecoderServiceRs3V950Beta
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder
import net.rsprox.protocol.rs3v950.game.incoming.decoder.prot.GameClientProt as GameClientProt950
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt as GameServerProt950
import net.rsprox.protocol.rs3v950beta.game.incoming.decoder.prot.GameClientProt as GameClientProt950Beta
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt as GameServerProt950Beta

public object Rs3DecoderLoader {
    public fun load(
        revision: Int,
        huffmanCodec: HuffmanCodec,
        serverCipher: () -> StreamCipher? = { null },
    ): Rs3RevisionDecoder {
        return when (revision) {
            Rs3ProtocolRevision.LIVE_950 ->
                Rs3RevisionDecoder(
                    revision = 950,
                    clientPacketDecoder = ClientPacketDecoderServiceRs3V950(huffmanCodec),
                    serverPacketDecoder = ServerPacketDecoderServiceRs3V950(huffmanCodec, serverCipher),
                    gameClientProtProvider = GameClientProtProviderRs3V950,
                    gameServerProtProvider = GameServerProtProviderRs3V950,
                    clientProtTable =
                        GameClientProt950.entries.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                    serverProtTable =
                        GameServerProt950.entries.filter { it != GameServerProt950.RECONNECT }.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                )
            Rs3ProtocolRevision.BETA_950_1 ->
                Rs3RevisionDecoder(
                    revision = revision,
                    clientPacketDecoder = ClientPacketDecoderServiceRs3V950Beta(huffmanCodec),
                    serverPacketDecoder = ServerPacketDecoderServiceRs3V950Beta(huffmanCodec, serverCipher),
                    gameClientProtProvider = GameClientProtProviderRs3V950Beta,
                    gameServerProtProvider = GameServerProtProviderRs3V950Beta,
                    clientProtTable =
                        GameClientProt950Beta.entries.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                    serverProtTable =
                        GameServerProt950Beta.entries.filter { it != GameServerProt950Beta.RECONNECT }.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                )
            else -> error("Unsupported RS3 protocol revision key: $revision")
        }
    }
}
