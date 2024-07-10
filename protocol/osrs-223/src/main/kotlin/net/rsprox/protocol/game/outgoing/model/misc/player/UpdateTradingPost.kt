package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Update trading post packed was used to create
 * a list of offers of a specific obj in the trading
 * post interface back when it still existed, in circa
 * 2014. This packet has not had a use since then, however.
 */
public class UpdateTradingPost(
    public val updateType: TradingPostUpdateType,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateTradingPost

        return updateType == other.updateType
    }

    override fun hashCode(): Int {
        return updateType.hashCode()
    }

    override fun toString(): String {
        return "UpdateTradingPost(updateType=$updateType)"
    }

    public sealed interface TradingPostUpdateType

    public data object ResetTradingPost : TradingPostUpdateType

    public class SetTradingPostOfferList private constructor(
        public val age: Long,
        private val _obj: UShort,
        public val status: Boolean,
        public val offers: List<TradingPostOffer>,
    ) : TradingPostUpdateType {
        public constructor(
            age: Long,
            obj: Int,
            status: Boolean,
            offers: List<TradingPostOffer>,
        ) : this(
            age,
            obj.toUShort(),
            status,
            offers,
        ) {
            require(offers.size <= 65535) {
                "Offers size must fit in an unsigned short"
            }
        }

        public val obj: Int
            get() = _obj.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetTradingPostOfferList

            if (age != other.age) return false
            if (_obj != other._obj) return false
            if (status != other.status) return false
            if (offers != other.offers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = age.hashCode()
            result = 31 * result + _obj.hashCode()
            result = 31 * result + status.hashCode()
            result = 31 * result + offers.hashCode()
            return result
        }

        override fun toString(): String {
            return "SetTradingPostOfferList(" +
                "age=$age, " +
                "obj=$obj, " +
                "status=$status, " +
                "offers=$offers" +
                ")"
        }
    }

    public class TradingPostOffer private constructor(
        public val name: String,
        public val previousName: String,
        private val _world: UShort,
        public val time: Long,
        public val price: Int,
        public val count: Int,
    ) {
        public constructor(
            name: String,
            previousName: String,
            world: Int,
            time: Long,
            price: Int,
            count: Int,
        ) : this(
            name,
            previousName,
            world.toUShort(),
            time,
            price,
            count,
        )

        public val world: Int
            get() = _world.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TradingPostOffer

            if (name != other.name) return false
            if (previousName != other.previousName) return false
            if (_world != other._world) return false
            if (time != other.time) return false
            if (price != other.price) return false
            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + previousName.hashCode()
            result = 31 * result + _world.hashCode()
            result = 31 * result + time.hashCode()
            result = 31 * result + price
            result = 31 * result + count
            return result
        }

        override fun toString(): String {
            return "TradingPostOffer(" +
                "name='$name', " +
                "previousName='$previousName', " +
                "world=$world, " +
                "time=$time, " +
                "price=$price, " +
                "count=$count" +
                ")"
        }
    }
}
