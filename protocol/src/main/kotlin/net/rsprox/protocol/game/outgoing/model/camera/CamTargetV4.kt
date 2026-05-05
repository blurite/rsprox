package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Camera target packet is used to attach to camera on another entity in the scene.
 * If the entity by the specified index cannot be found in the client, the camera
 * will always be focused back on the local player.
 * @property type the camera target type to focus on.
 */
public class CamTargetV4(
    public val type: CamTargetType,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamTargetV4

        return type == other.type
    }

    override fun hashCode(): Int = type.hashCode()

    override fun toString(): String = "CamTargetV4(type=$type)"

    /**
     * A sealed interface for various camera target types.
     */
    public sealed interface CamTargetType

    /**
     * Camera target type for players. This will focus the camera on the player with the
     * specified index.
     * @property index the index of the player who to set the camera on.
     */
    public class PlayerCamTarget(
        public val index: Int,
    ) : CamTargetType {
        init {
            require(index in 0..<2048) {
                "Index must be in range of 0..<2048"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PlayerCamTarget

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "PlayerCamTarget(" +
                "index=$index" +
                ")"
        }
    }

    /**
     * Camera target type for NPCs. This will focus the camera on the NPC with the specified index.
     * @property index the index of the NPC who to set the camera on.
     */
    public class NpcCamTarget(
        public val index: Int,
    ) : CamTargetType {
        init {
            require(index in 0..<65536) {
                "Index must be in range of 0..<65536"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NpcCamTarget

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "NpcCamTarget(" +
                "index=$index" +
                ")"
        }
    }

    /**
     * Camera target type for world entities. This will focus the camera on the specified worldentity.
     * @property index the index of the world entity who to set the camera on.
     */
    public class WorldEntityTarget(
        public val index: Int,
    ) : CamTargetType {
        init {
            require(index in 0..<2048) {
                "World entity target index must be in range of 0..<2048"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WorldEntityTarget

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "WorldEntityTarget(index=$index)"
        }
    }

    /**
     * Camera type for CoordGrid targets, allowing the camera to be pointed at the center of
     * a specific coordinate.
     * @property coordGrid the coordinate to point at.
     */
    public class CoordGridTarget(
        public val coordGrid: CoordGrid,
    ) : CamTargetType {
        public constructor(
            level: Int,
            x: Int,
            z: Int,
        ) : this(
            CoordGrid(level, x, z),
        )

        public val level: Int
            get() = coordGrid.level
        public val x: Int
            get() = coordGrid.x
        public val z: Int
            get() = coordGrid.z

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CoordGridTarget

            return coordGrid == other.coordGrid
        }

        override fun hashCode(): Int {
            return coordGrid.hashCode()
        }

        override fun toString(): String {
            return "CoordGridTarget(" +
                "level=$level, " +
                "x=$x, " +
                "z=$z" +
                ")"
        }
    }
}
