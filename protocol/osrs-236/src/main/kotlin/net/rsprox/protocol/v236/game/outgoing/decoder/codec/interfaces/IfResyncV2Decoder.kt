package net.rsprox.protocol.v236.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResyncV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class IfResyncV2Decoder : ProxyMessageDecoder<IfResyncV2> {
    override val prot: ClientProt = GameServerProt.IF_RESYNC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfResyncV2 {
        val topLevelInterface = buffer.g2()
        val subInterfaceCount = buffer.g2()
        val subInterfaces =
            buildList {
                for (i in 0..<subInterfaceCount) {
                    val destinationCombinedId = buffer.gCombinedId()
                    val interfaceId = buffer.g2()
                    val type = buffer.g1()
                    add(
                        IfResyncV2.SubInterfaceMessage(
                            destinationCombinedId.interfaceId,
                            destinationCombinedId.componentId,
                            interfaceId,
                            type,
                        ),
                    )
                }
            }
        val events =
            buildList {
                while (buffer.isReadable) {
                    val combinedId = buffer.gCombinedId()
                    val start = buffer.g2()
                    val end = buffer.g2()
                    val events1 = buffer.g4()
                    val events2 = buffer.g4()
                    add(
                        IfResyncV2.InterfaceEventsMessage(
                            combinedId.interfaceId,
                            combinedId.componentId,
                            start,
                            end,
                            events1,
                            events2,
                        ),
                    )
                }
            }
        return IfResyncV2(
            topLevelInterface,
            subInterfaces,
            events,
        )
    }
}
