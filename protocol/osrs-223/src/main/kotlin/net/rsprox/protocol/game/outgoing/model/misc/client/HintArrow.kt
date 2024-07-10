package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Hint arrow packets are used to render a hint arrow
 * at a specific player, NPC or a tile.
 * Only a single hint arrow can exist at a time in OldSchool.
 * @property type the hint arrow type to render.
 */
public class HintArrow(
    public val type: HintArrowType,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HintArrow

        return type == other.type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return "HintArrow(type=$type)"
    }

    public sealed interface HintArrowType

    /**
     * Reset hint arrow message is used to clear out any
     * existing hint arrows.
     */
    public data object ResetHintArrow : HintArrowType

    /**
     * NPC hint arrows are used to render a hint arrow
     * on-top of a specific NPC.
     * @property index the index of the NPC who is receiving
     * the hint arrow. Note that this is the real index without
     * any offsets or additions.
     */
    public class NpcHintArrow(
        public val index: Int,
    ) : HintArrowType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NpcHintArrow

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "NpcHintArrow(index=$index)"
        }
    }

    /**
     * Player hint arrows are used to render a hint arrow
     * on-top of a specific player.
     * @property index the index of the player who is receiving
     * the hint arrow. Note that this is the real index without
     * any offsets or additions.
     */
    public class PlayerHintArrow(
        public val index: Int,
    ) : HintArrowType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PlayerHintArrow

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "PlayerHintArrow(index=$index)"
        }
    }

    /**
     * Tile hint arrows are used to render a hint arrow at
     * a specific coordinate.
     * @property x the absolute x coordinate of the hint arrow.
     * @property z the absolute z coordinate of the hint arrow.
     * @property height the height of the hint arrow,
     * with the expected range being 0 to 255 (inclusive).
     * @property position the position of the hint arrow within
     * the target tile.
     */
    public class TileHintArrow private constructor(
        private val _x: UShort,
        private val _z: UShort,
        private val _height: UByte,
        private val _position: UByte,
    ) : HintArrowType {
        public constructor(
            x: Int,
            z: Int,
            height: Int,
            tilePosition: HintArrowTilePosition,
        ) : this(
            x.toUShort(),
            z.toUShort(),
            height.toUByte(),
            tilePosition.id.toUByte(),
        )

        public constructor(
            x: Int,
            z: Int,
            height: Int,
            tilePosition: Int,
        ) : this(
            x.toUShort(),
            z.toUShort(),
            height.toUByte(),
            tilePosition.toUByte(),
        )

        public val x: Int
            get() = _x.toInt()
        public val z: Int
            get() = _z.toInt()
        public val height: Int
            get() = _height.toInt()
        public val position: HintArrowTilePosition
            get() = HintArrowTilePosition[_position.toInt()]
        public val positionId: Int
            get() = _position.toInt()

        /**
         * Hint arrow tile positions define where within a tile
         * the given hint arrow will render. All the options here
         * are centered on the tile, e.g. [WEST] will be at the
         * western section of the tile, whilst being centered
         * on the z-axis.
         *
         * @property id the id of the hint arrow position,
         * as expected by the client.
         */
        public enum class HintArrowTilePosition(
            public val id: Int,
        ) {
            CENTER(2),
            WEST(3),
            EAST(4),
            SOUTH(5),
            NORTH(6),
            ;

            internal companion object {
                operator fun get(id: Int): HintArrowTilePosition {
                    return entries.first { it.id == id }
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TileHintArrow

            if (_x != other._x) return false
            if (_z != other._z) return false
            if (_height != other._height) return false
            if (_position != other._position) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _x.hashCode()
            result = 31 * result + _z.hashCode()
            result = 31 * result + _height.hashCode()
            result = 31 * result + _position.hashCode()
            return result
        }

        override fun toString(): String {
            return "TileHintArrow(" +
                "x=$x, " +
                "z=$z, " +
                "height=$height, " +
                "position=$position" +
                ")"
        }
    }
}
