package net.rsprox.protocol.v230.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResyncV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class IfResyncV1Decoder : ProxyMessageDecoder<IfResyncV1> {
    override val prot: ClientProt = GameServerProt.IF_RESYNC_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfResyncV1 {
        val topLevelInterface = buffer.g2()
        val subInterfaceCount = buffer.g2()
        val subInterfaces =
            buildList {
                for (i in 0..<subInterfaceCount) {
                    val destinationCombinedId = buffer.gCombinedId()
                    val interfaceId = buffer.g2()
                    val type = buffer.g1()
                    add(
                        IfResyncV1.SubInterfaceMessage(
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
                    val events = buffer.g4()
                    add(
                        IfResyncV1.InterfaceEventsMessage(
                            combinedId.interfaceId,
                            combinedId.componentId,
                            start,
                            end,
                            events,
                        ),
                    )
                }
            }
        return IfResyncV1(
            topLevelInterface,
            subInterfaces,
            events,
        )
    }
}
