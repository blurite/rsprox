package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.reflection.ReflectionCheck

/**
 * Reflection checker packet will attempt to use [java.lang.reflect] to
 * perform a lookup or invocation on a method or field in the client,
 * using information provided in this packet.
 * These invocations/lookups may fail completely, which is fully supported,
 * as various exceptions get caught and special return codes are provided
 * in such cases.
 * An important thing to note, however, is that the server is responsible
 * for not requesting too much, as the client's reply packet has a var-byte
 * size, meaning the entire reply for a reflection check must fit into 255
 * bytes or fewer. There is no protection against this.
 * Additionally worth noting that the [ReflectionCheck.InvokeMethod] variant, while very
 * powerful, is not utilized in OldSchool, and is rather dangerous to
 * invoke due to the aforementioned size limitation.
 *
 * @property id the id of the reflection check, sent back in the reply and
 * used to link together the request and reply, which is needed to fully
 * decode the respective replies.
 * @property checks the list of reflection checks to perform.
 */
public class ReflectionChecker(
    public val id: Int,
    public val checks: List<ReflectionCheck>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReflectionChecker

        if (id != other.id) return false
        if (checks != other.checks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + checks.hashCode()
        return result
    }

    override fun toString(): String {
        return "ReflectionChecker(" +
            "id=$id, " +
            "checks=$checks" +
            ")"
    }
}
