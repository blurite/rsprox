package net.rsprox.protocol.rs3v950.cache

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.rs3.Rs3BaseVarType
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.rs3.common.QuickChat
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.buffer.readNativeString2
import net.rsprox.protocol.session.Session

internal fun JagByteBuf.readQuickChat(
    session: Session,
    phraseId: Int,
    fromClient: Boolean = false,
): QuickChat {
    val phrase =
        checkNotNull(session.rs3PacketDefinitions) { "RS3 packet cache is not initialized" }
            .getQuickChatPhrase(phraseId)
    return QuickChat(
        phrase.template,
        phrase.commands.map { command ->
            val width = if (fromClient) command.transmitBytes else command.receiveBytes
            require(width in 0..8 && width <= readableBytes()) { "Truncated quickchat parameter" }
            var value = 0L
            repeat(width) { value = (value shl 8) or g1().toLong() }
            QuickChat.Parameter(command.id, command.arguments, if (width == 0) null else value)
        },
    )
}

internal fun JagByteBuf.readTypedVariable(
    session: Session,
    domain: Rs3VariableDomain,
    id: Int = g2(),
    fromClient: Boolean = false,
): TypedVariable {
    val definition =
        checkNotNull(session.rs3PacketDefinitions) { "RS3 packet cache is not initialized" }
            .getVariable(domain, id)
    val value =
        when (definition.baseType) {
            Rs3BaseVarType.INT -> TypedVariable.IntValue(g4())
            Rs3BaseVarType.LONG -> TypedVariable.LongValue(g8())
            Rs3BaseVarType.STRING ->
                TypedVariable.StringValue(
                    if (fromClient) readNativeString2() else readNativeString(),
                )
            Rs3BaseVarType.COORDINATE -> {
                require(!fromClient) { "Coordinate varc uploads are not proven for revision 950" }
                TypedVariable.Coordinate(g1(), g4(), g4(), g4())
            }
        }
    return TypedVariable(id, definition.scriptType, value)
}
