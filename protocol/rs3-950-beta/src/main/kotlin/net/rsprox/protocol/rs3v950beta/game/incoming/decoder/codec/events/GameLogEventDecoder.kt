package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3BaseVarType
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.protocol.rs3.game.incoming.model.events.GameLogEvent
import net.rsprox.protocol.rs3v950beta.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class GameLogEventDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<GameLogEvent> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): GameLogEvent {
        val id = buffer.g2Alt1()
        val value16 = buffer.g2()
        val script = buffer.g4()
        val definition =
            checkNotNull(session.rs3PacketDefinitions) { "RS3 packet cache is not initialized" }
                .getGameLogEvent(id)
        val arguments =
            definition.parameters.map { parameter ->
                val value =
                    when (parameter.baseType) {
                        Rs3BaseVarType.INT -> TypedVariable.IntValue(buffer.g4())
                        Rs3BaseVarType.LONG -> TypedVariable.LongValue(buffer.g8())
                        Rs3BaseVarType.STRING -> TypedVariable.StringValue(buffer.readNativeString())
                        Rs3BaseVarType.COORDINATE -> error("Unsupported game-log coordinate parameter")
                    }
                GameLogEvent.Argument(parameter.scriptType, value)
            }
        require(buffer.readableBytes() == 0) { "Game-log payload does not match its cache definition" }
        return GameLogEvent(id, value16, script, arguments)
    }
}
