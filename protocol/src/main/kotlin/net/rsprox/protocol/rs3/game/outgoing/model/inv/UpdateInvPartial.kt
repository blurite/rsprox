package net.rsprox.protocol.rs3.game.outgoing.model.inv

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.inv.util.InvVar

public class UpdateInvPartial(
    public val inventoryId: Int,
    public val flags: Int,
    public val objs: List<IndexedObj>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvPartial

        if (inventoryId != other.inventoryId) return false
        if (flags != other.flags) return false
        if (objs != other.objs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inventoryId
        result = 31 * result + flags
        result = 31 * result + objs.hashCode()
        return result
    }

    override fun toString(): String {
        return "UpdateInvPartial(inventoryId=$inventoryId, flags=$flags, objs=$objs)"
    }

    public class IndexedObj(
        public val slot: Int,
        public val id: Int,
        public val count: Int,
        public val vars: List<InvVar>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IndexedObj

            if (slot != other.slot) return false
            if (id != other.id) return false
            if (count != other.count) return false
            if (vars != other.vars) return false

            return true
        }

        override fun hashCode(): Int {
            var result = slot
            result = 31 * result + id
            result = 31 * result + count
            result = 31 * result + vars.hashCode()
            return result
        }

        override fun toString(): String {
            return "IndexedObj(slot=$slot, id=$id, count=$count, vars=$vars)"
        }
    }
}
