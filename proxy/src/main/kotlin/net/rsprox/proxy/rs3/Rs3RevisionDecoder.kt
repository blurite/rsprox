package net.rsprox.proxy.rs3

import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder

public data class Rs3RevisionDecoder(
    public val revision: Int,
    public val clientPacketDecoder: ClientPacketDecoder,
    public val serverPacketDecoder: ServerPacketDecoder,
    public val gameClientProtProvider: ProtProvider<ClientProt>,
    public val gameServerProtProvider: ProtProvider<ClientProt>,
    public val clientProtTable: Map<Int, Rs3ProtDecoder.ProtEntry>,
    public val serverProtTable: Map<Int, Rs3ProtDecoder.ProtEntry>,
)
