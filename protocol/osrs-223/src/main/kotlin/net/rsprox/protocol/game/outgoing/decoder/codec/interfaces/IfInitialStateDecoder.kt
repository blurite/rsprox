package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfInitialState
import net.rsprox.protocol.session.Session

@Consistent
public class IfInitialStateDecoder : ProxyMessageDecoder<IfInitialState> {
    override val prot: ClientProt = GameServerProt.IF_INITIALSTATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfInitialState {
        val topLevelInterface = buffer.g2()
        val subInterfaceCount = buffer.g2()
        val subInterfaces =
            buildList {
                for (i in 0..<subInterfaceCount) {
                    val destinationCombinedId = buffer.gCombinedId()
                    val interfaceId = buffer.g2()
                    val type = buffer.g1()
                    add(
                        IfInitialState.SubInterfaceMessage(
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
                        IfInitialState.InterfaceEventsMessage(
                            combinedId.interfaceId,
                            combinedId.componentId,
                            start,
                            end,
                            events,
                        ),
                    )
                }
            }
        return IfInitialState(
            topLevelInterface,
            subInterfaces,
            events,
        )
    }
}
