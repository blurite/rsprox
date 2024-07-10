package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Update stockmarket slot packet is used to set up
 * an offer on the Grand Exchange, or to clear out an
 * offer.
 * @property update the update type to perform, either
 * [ResetStockMarketSlot] or [SetStockMarketSlot].
 */
public class UpdateStockMarketSlot private constructor(
    private val _slot: UByte,
    public val update: StockMarketUpdateType,
) : OutgoingGameMessage {
    public constructor(
        slot: Int,
        update: StockMarketUpdateType,
    ) : this(
        slot.toUByte(),
        update,
    )

    public val slot: Int
        get() = _slot.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateStockMarketSlot

        return update == other.update
    }

    override fun hashCode(): Int {
        return update.hashCode()
    }

    override fun toString(): String {
        return "UpdateStockMarketSlot(" +
            "slot=$slot, " +
            "update=$update" +
            ")"
    }

    public sealed interface StockMarketUpdateType

    public data object ResetStockMarketSlot : StockMarketUpdateType

    /**
     * Set stockmarket slot update creates an offer
     * on the Grand Exchange.
     * @property status the status of the offer to create.
     * Note that if the status value is 0, it will be treated
     * as a request to clear out the slot and all the remaining
     * data will be ignored in the process.
     * @property obj the obj to set in the specified slot
     * @property price the price per item
     * @property count the count to buy or sell
     * @property completedCount the amount already bought or sold
     * @property completedGold the amount of gold received
     */
    public class SetStockMarketSlot private constructor(
        private val _status: Byte,
        private val _obj: UShort,
        public val price: Int,
        public val count: Int,
        public val completedCount: Int,
        public val completedGold: Int,
    ) : StockMarketUpdateType {
        public constructor(
            status: Int,
            obj: Int,
            price: Int,
            count: Int,
            completedCount: Int,
            completedGold: Int,
        ) : this(
            status.toByte(),
            obj.toUShort(),
            price,
            count,
            completedCount,
            completedGold,
        )

        public val status: Int
            get() = _status.toInt()
        public val obj: Int
            get() = _obj.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetStockMarketSlot

            if (_status != other._status) return false
            if (_obj != other._obj) return false
            if (price != other.price) return false
            if (count != other.count) return false
            if (completedCount != other.completedCount) return false
            if (completedGold != other.completedGold) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _status.toInt()
            result = 31 * result + _obj.hashCode()
            result = 31 * result + price
            result = 31 * result + count
            result = 31 * result + completedCount
            result = 31 * result + completedGold
            return result
        }

        override fun toString(): String {
            return "SetStockMarketSlot(" +
                "status=$status, " +
                "obj=$obj, " +
                "price=$price, " +
                "count=$count, " +
                "completedCount=$completedCount, " +
                "completedGold=$completedGold" +
                ")"
        }
    }
}
