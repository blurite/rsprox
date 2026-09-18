package net.rsprox.proxy.rs3

import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprox.protocol.rs3v949.ClientPacketDecoderServiceRs3V949
import net.rsprox.protocol.rs3v949.GameClientProtProviderRs3V949
import net.rsprox.protocol.rs3v949.GameServerProtProviderRs3V949
import net.rsprox.protocol.rs3v949.ServerPacketDecoderServiceRs3V949
import net.rsprox.protocol.rs3v950.ClientPacketDecoderServiceRs3V950
import net.rsprox.protocol.rs3v950.GameClientProtProviderRs3V950
import net.rsprox.protocol.rs3v950.GameServerProtProviderRs3V950
import net.rsprox.protocol.rs3v950.ServerPacketDecoderServiceRs3V950
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt as GameClientProt949
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt as GameServerProt949
import net.rsprox.protocol.rs3v950.game.incoming.decoder.prot.GameClientProt as GameClientProt950
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt as GameServerProt950

public object Rs3DecoderLoader {
    public fun load(
        revision: Int,
        huffmanCodec: HuffmanCodec,
        serverCipher: () -> StreamCipher? = { null },
    ): Rs3RevisionDecoder {
        return when (revision) {
            949 ->
                Rs3RevisionDecoder(
                    revision = 949,
                    clientPacketDecoder = ClientPacketDecoderServiceRs3V949(huffmanCodec),
                    serverPacketDecoder = ServerPacketDecoderServiceRs3V949(),
                    gameClientProtProvider = GameClientProtProviderRs3V949,
                    gameServerProtProvider = GameServerProtProviderRs3V949,
                    clientProtTable =
                        GameClientProt949.entries.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                    serverProtTable =
                        GameServerProt949.entries.associate {
                            it.opcode to Rs3ProtDecoder.ProtEntry(it.size, it.name)
                        },
                )
            950 ->
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
            else -> error("Unsupported RS3 revision: $revision")
        }
    }
}
