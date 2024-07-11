package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.ReflectionChecker
import net.rsprox.protocol.reflection.ReflectionCheck
import net.rsprox.protocol.session.Session

@Suppress("DuplicatedCode")
@Consistent
public class ReflectionCheckerDecoder : ProxyMessageDecoder<ReflectionChecker> {
    override val prot: ClientProt = GameServerProt.REFLECTION_CHECKER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ReflectionChecker {
        val checksCount = buffer.g1()
        val id = buffer.g4()
        val checks =
            buildList {
                for (i in 0..<checksCount) {
                    when (val opcode = buffer.g1()) {
                        0 -> {
                            val className = buffer.gjstr()
                            val fieldName = buffer.gjstr()
                            add(
                                ReflectionCheck.GetFieldValue(
                                    className,
                                    fieldName,
                                ),
                            )
                        }
                        1 -> {
                            val className = buffer.gjstr()
                            val fieldName = buffer.gjstr()
                            val value = buffer.g4()
                            add(
                                ReflectionCheck.SetFieldValue(
                                    className,
                                    fieldName,
                                    value,
                                ),
                            )
                        }
                        2 -> {
                            val className = buffer.gjstr()
                            val fieldName = buffer.gjstr()
                            add(
                                ReflectionCheck.GetFieldModifiers(
                                    className,
                                    fieldName,
                                ),
                            )
                        }
                        3 -> {
                            val className = buffer.gjstr()
                            val methodName = buffer.gjstr()
                            val parameterClassCount = buffer.g1()
                            val parameterClasses =
                                buildList {
                                    for (j in 0..<parameterClassCount) {
                                        add(buffer.gjstr())
                                    }
                                }
                            val returnClass = buffer.gjstr()
                            val parameterValues =
                                buildList {
                                    for (j in 0..<parameterClassCount) {
                                        val size = buffer.g4()
                                        val array = ByteArray(size)
                                        buffer.gdata(array)
                                        add(array)
                                    }
                                }
                            add(
                                ReflectionCheck.InvokeMethod(
                                    className,
                                    methodName,
                                    parameterClasses,
                                    parameterValues,
                                    returnClass,
                                ),
                            )
                        }
                        4 -> {
                            val className = buffer.gjstr()
                            val methodName = buffer.gjstr()
                            val parameterClassCount = buffer.g1()
                            val parameterClasses =
                                buildList {
                                    for (j in 0..<parameterClassCount) {
                                        add(buffer.gjstr())
                                    }
                                }
                            val returnClass = buffer.gjstr()
                            add(
                                ReflectionCheck.GetMethodModifiers(
                                    className,
                                    methodName,
                                    parameterClasses,
                                    returnClass,
                                ),
                            )
                        }
                        else -> error("Unknown reflection check type: $opcode")
                    }
                }
            }
        val old = session.reflectionChecks.put(id, checks)
        check(old == null) {
            "Overlapping reflection check: $old/$checks"
        }
        return ReflectionChecker(
            id,
            checks,
        )
    }
}
