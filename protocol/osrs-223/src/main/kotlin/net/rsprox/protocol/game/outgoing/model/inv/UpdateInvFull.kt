package net.rsprox.protocol.game.outgoing.model.inv

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update inv full is used to perform a full synchronization of an inventory's
 * contents to the client.
 * The client will wipe any existing cache of this inventory prior to performing
 * an update.
 * While not very well known, it is possible to send less objs than the inventory's
 * respective capacity in the cache. As an example, if the inventory's capacity
 * in the cache is 500, but the inv only has a single object at the first slot,
 * a simple compression method is to send the capacity as 1 to the client,
 * and only inform of the single object that does exist - all others would be
 * presumed non-existent. There is no need to transmit all 500 slots when
 * the remaining 499 are not filled, saving considerable amount of space in the
 * process.
 *
 * @property combinedId the combined id of the interface and the component id.
 * For IF3-type interfaces, only negative values are allowed.
 * If one wishes to make the inventory a "mirror", e.g. for trading,
 * how both the player's own and the partner's inventory share the id,
 * a value of < -70000 is expected, this tells the client that the respective
 * inventory is a "mirrored" one.
 * For normal IF3 interfaces, a value of -1 is perfectly acceptable.
 * @property interfaceId the IF1 interface on which the inventory lies.
 * For IF3 interfaces, no [interfaceId] should be provided.
 * @property componentId the component on which the inventory lies
 * @property inventoryId the id of the inventory to update
 * @property objs the list of items to render on the interface.
 */
public class UpdateInvFull private constructor(
    public val combinedId: CombinedId,
    private val _inventoryId: UShort,
    public val objs: List<Obj>,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        inventoryId: Int,
        items: List<Obj>,
    ) : this(
        CombinedId(interfaceId, componentId),
        inventoryId.toUShort(),
        items,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val inventoryId: Int
        get() = _inventoryId.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvFull

        if (combinedId != other.combinedId) return false
        if (_inventoryId != other._inventoryId) return false
        if (objs != other.objs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _inventoryId.hashCode()
        result = 31 * result + objs.hashCode()
        return result
    }

    override fun toString(): String {
        return "UpdateInvFull(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "inventoryId=$inventoryId, " +
            "objs=$objs" +
            ")"
    }

    public class Obj(
        private val id: Int,
        private val count: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Obj

            if (id != other.id) return false
            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + count
            return result
        }

        override fun toString(): String {
            return "Obj(" +
                "id=$id, " +
                "count=$count" +
                ")"
        }
    }
}
