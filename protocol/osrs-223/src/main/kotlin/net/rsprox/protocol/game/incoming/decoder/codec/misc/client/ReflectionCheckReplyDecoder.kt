package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.checkCRC32
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getReflectionChecks
import java.io.IOException
import java.io.InvalidClassException
import java.io.OptionalDataException
import java.io.StreamCorruptedException
import java.lang.reflect.InvocationTargetException

@Consistent
public class ReflectionCheckReplyDecoder : ProxyMessageDecoder<ReflectionCheckReply> {
    override val prot: ClientProt = GameClientProt.REFLECTION_CHECK_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ReflectionCheckReply {
        val id = buffer.g4()
        val checks = session.getReflectionChecks().remove(id)
        checkNotNull(checks) {
            "Unable to link reflection check reply to request: $id"
        }
        val results = ArrayList<ReflectionCheckReply.ReflectionCheckResult<*>>(checks.size)
        for (check in checks) {
            val opcode = buffer.g1s()
            if (opcode < 0) {
                val exception = getExceptionClass(opcode)
                results += ReflectionCheckReply.ErrorResult(check, exception)
                continue
            }
            when (check) {
                is ReflectionCheck.GetFieldValue -> {
                    val result = buffer.g4()
                    results += ReflectionCheckReply.GetFieldValueResult(check, result)
                }
                is ReflectionCheck.SetFieldValue -> {
                    results += ReflectionCheckReply.SetFieldValueResult(check)
                }
                is ReflectionCheck.GetFieldModifiers -> {
                    val modifiers = buffer.g4()
                    results += ReflectionCheckReply.GetFieldModifiersResult(check, modifiers)
                }
                is ReflectionCheck.InvokeMethod -> {
                    results +=
                        when (opcode) {
                            0 -> ReflectionCheckReply.InvokeMethodResult(check, ReflectionCheckReply.NullReturnValue)
                            1 ->
                                ReflectionCheckReply.InvokeMethodResult(
                                    check,
                                    ReflectionCheckReply.NumberReturnValue(buffer.g8()),
                                )
                            2 ->
                                ReflectionCheckReply.InvokeMethodResult(
                                    check,
                                    ReflectionCheckReply.StringReturnValue(buffer.gjstr()),
                                )
                            4 -> ReflectionCheckReply.InvokeMethodResult(check, ReflectionCheckReply.UnknownReturnValue)
                            else -> throw IllegalStateException("Unknown opcode for method invocation: $opcode")
                        }
                }
                is ReflectionCheck.GetMethodModifiers -> {
                    val modifiers = buffer.g4()
                    results += ReflectionCheckReply.GetMethodModifiersResult(check, modifiers)
                }
            }
        }
        buffer.readerIndex(buffer.writerIndex())
        if (!buffer.buffer.checkCRC32()) {
            throw IllegalStateException("CRC mismatch!")
        }
        return ReflectionCheckReply(
            id,
            results,
        )
    }

    /**
     * Gets the exception class corresponding to each opcode.
     * @param opcode the opcode value
     * @return the exception class corresponding to that opcode
     */
    private fun getExceptionClass(opcode: Int): Class<*> {
        return when (opcode) {
            -10 -> ClassNotFoundException::class.java
            -11 -> InvalidClassException::class.java
            -12 -> StreamCorruptedException::class.java
            -13 -> OptionalDataException::class.java
            -14 -> IllegalAccessException::class.java
            -15 -> IllegalArgumentException::class.java
            -16 -> InvocationTargetException::class.java
            -17 -> SecurityException::class.java
            -18 -> IOException::class.java
            -19 -> NullPointerException::class.java
            -20 -> Exception::class.java
            -21 -> Throwable::class.java
            else -> throw IllegalArgumentException("Unknown exception opcode: $opcode")
        }
    }
}
