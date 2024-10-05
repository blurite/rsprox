package net.rsprox.protocol.game.outgoing.model.inv

import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update inv partial is used to send an update of an inventory
 * that doesn't include the entire inventory.
 * This is typically used after the first [UpdateInvFull]
 * update has been performed, as subsequent updates tend to
 * be smaller, such as picking up an object - only
 * a single slot in the player's inventory would change, not warranting
 * the full 28-slot update as a result.
 *
 * A general rule of thumb for when to use partial updates is
 * if the percentage of modified objects is less than two thirds
 * of that of the highest modified index.
 * So, if our inventory has a capacity of 1,000, and we have sparsely
 * modified 500 indices throughout that, including the 999th index,
 * it is more beneficial to use the partial inventory update,
 * as the total bandwidth used by it is generally going to be less
 * than what the full update would require.
 * If, however, there is a continuous sequence of indices that
 * have been modified, such as everything from indices 0 to 100,
 * it is more beneficial to use the full update and set the
 * capacity to 100 in that.
 *
 * Below is a percentage-breakdown of how much more bandwidth the partial update
 * requires per object basis given different criteria, compared to the full update:
 * ```
 * 14.3% if slot < 128 && count >= 255
 * 28.6% if slot >= 128 && count >= 255
 * 33.3% if slot < 128 && count < 255
 * 66.6% if slot >= 128 && count < 255
 * ```
 *
 * While it is impossible to truly estimate what the exact threshold is,
 * this provides a good general idea of when to use either packet.
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
 * @property count the number of items added into this partial update.
 * @property objs the list of indexed objs to render.
 */
public class UpdateInvPartial private constructor(
    public val combinedId: CombinedId,
    private val _inventoryId: UShort,
    public val objs: List<IndexedObj>,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        inventoryId: Int,
        items: List<IndexedObj>,
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

        other as UpdateInvPartial

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
        return "UpdateInvPartial(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "inventoryId=$inventoryId, " +
            "objs=$objs" +
            ")"
    }

    public class IndexedObj private constructor(
        private val _slot: UShort,
        private val _id: UShort,
        public val count: Int,
    ) {
        public constructor(
            slot: Int,
            id: Int,
            count: Int,
        ) : this(
            slot.toUShort(),
            id.toUShort(),
            count,
        )

        public val slot: Int
            get() = _slot.toInt()
        public val id: Int
            get() = _id.toIntOrMinusOne()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IndexedObj

            if (_slot != other._slot) return false
            if (_id != other._id) return false
            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _slot.hashCode()
            result = 31 * result + _id.hashCode()
            result = 31 * result + count
            return result
        }

        override fun toString(): String {
            return "IndexedObj(" +
                "count=$count, " +
                "slot=$slot, " +
                "id=$id" +
                ")"
        }
    }
}
