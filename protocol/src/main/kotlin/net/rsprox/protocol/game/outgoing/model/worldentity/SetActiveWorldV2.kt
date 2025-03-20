package net.rsprox.protocol.game.outgoing.model.worldentity

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Set active world packet is used to set the currently active world in the client,
 * allowing for various world-specific packets to perform changes to a different world
 * than the usual root.
 * Packets such as zone updates, player info, NPC info are a few examples of what may be sent afterwards.
 * @property worldType the world type to update next.
 */
public class SetActiveWorldV2(
    public val worldType: WorldType,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetActiveWorldV2

        return worldType == other.worldType
    }

    override fun hashCode(): Int {
        return worldType.hashCode()
    }

    override fun toString(): String {
        return "SetActiveWorldV2(worldType=$worldType)"
    }

    /**
     * A world type to set as the currently active world, allowing for updates
     * to be done to that specific world.
     */
    public sealed interface WorldType

    /**
     * The root world type, resetting currently world to the main one.
     * @property activeLevel the level at which various events will take place, such as
     * zone updates.
     */
    public class RootWorldType private constructor(
        private val _activeLevel: UByte,
    ) : WorldType {
        public constructor(activeLevel: Int) : this(activeLevel.toUByte()) {
            require(activeLevel in 0..<4) {
                "Active level must be in range of 0..<4"
            }
        }

        public val activeLevel: Int
            get() = _activeLevel.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RootWorldType

            return _activeLevel == other._activeLevel
        }

        override fun hashCode(): Int = _activeLevel.hashCode()

        override fun toString(): String = "RootWorldType(activeLevel=$activeLevel)"
    }

    /**
     * A dynamic world type is used to mark one of the world entities' worlds as
     * the active world, allowing for changes to be sent to that world entity.
     * @property index the index of the world entity whose world is about to be updated,
     * in range of 0..<2048.
     * @property activeLevel the level at which various events will take place, such as
     * zone updates.
     */
    public class DynamicWorldType private constructor(
        private val _index: UShort,
        private val _activeLevel: UByte,
    ) : WorldType {
        public constructor(
            index: Int,
            activeLevel: Int,
        ) : this(
            index.toUShort(),
            activeLevel.toUByte(),
        ) {
            require(index in 0..<4096) {
                "Index must be in range of 0..<4096"
            }
            require(activeLevel in 0..<4) {
                "Active level must be in range of 0..<4"
            }
        }

        public val index: Int
            get() = _index.toInt()
        public val activeLevel: Int
            get() = _activeLevel.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DynamicWorldType

            if (_index != other._index) return false
            if (_activeLevel != other._activeLevel) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _index.hashCode()
            result = 31 * result + _activeLevel.hashCode()
            return result
        }

        override fun toString(): String =
            "DynamicWorldType(" +
                "index=$index, " +
                "activeLevel=$activeLevel" +
                ")"
    }
}
