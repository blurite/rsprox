package net.rsprox.protocol.v224.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResync
import net.rsprox.protocol.session.Session

@Consistent
public class IfResyncDecoder : ProxyMessageDecoder<IfResync> {
    override val prot: ClientProt = GameServerProt.IF_RESYNC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfResync {
        val topLevelInterface = buffer.g2()
        val subInterfaceCount = buffer.g2()
        val subInterfaces =
            buildList {
                for (i in 0..<subInterfaceCount) {
                    val destinationCombinedId = buffer.gCombinedId()
                    val interfaceId = buffer.g2()
                    val type = buffer.g1()
                    add(
                        IfResync.SubInterfaceMessage(
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
                        IfResync.InterfaceEventsMessage(
                            combinedId.interfaceId,
                            combinedId.componentId,
                            start,
                            end,
                            events,
                        ),
                    )
                }
            }
        return IfResync(
            topLevelInterface,
            subInterfaces,
            events,
        )
    }
}
