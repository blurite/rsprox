package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Camera target packet is used to attach to camera on another entity in the scene.
 * If the entity by the specified index cannot be found in the client, the camera
 * will always be focused back on the local player.
 * Furthermore, depth buffering (z-buffer) will be enabled if the [WorldEntityTarget] type
 * is used. Other types will use the traditional priority system.
 * @property type the camera target type to focus on.
 */
public class CamTarget(
    public val type: CamTargetType,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamTarget

        return type == other.type
    }

    override fun hashCode(): Int = type.hashCode()

    override fun toString(): String = "CamTarget(type=$type)"

    /**
     * A sealed interface for various camera target types.
     */
    public sealed interface CamTargetType

    /**
     * Camera target type for players. This will focus the camera on a specific player.
     * If the player by the specified [index] cannot be found, the camera will be set back on
     * local player.
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

            return index == other.index
        }

        override fun hashCode(): Int = index

        override fun toString(): String = "PlayerCamTarget(index=$index)"
    }

    /**
     * Camera target type for NPCs. This will focus the camera on a specific NPC.
     * If the NPC by the specified [index] cannot be found, the camera will be set back on
     * local player.
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

            return index == other.index
        }

        override fun hashCode(): Int = index

        override fun toString(): String = "NpcCamTarget(index=$index)"
    }

    /**
     * Camera target type for world entities. This will focus the camera on a specific world entity.
     * If the world entity by the specified [index] cannot be found, the camera will be set back on
     * local player. If a player index is provided, the client will try to look up that player in the
     * root player's current world entity and lock the camera onto them.
     * Additionally, depth buffering (z-buffer) will be enabled when this type of camera target is used.
     * @property index the index of the world entity who to set the camera on.
     * @property cameraLockedPlayerIndex the index of the player on the local player's world entity whom
     * to lock the camera onto.
     */
    public class WorldEntityTarget(
        public val index: Int,
        public val cameraLockedPlayerIndex: Int,
    ) : CamTargetType {
        init {
            require(index in 0..<2048) {
                "Index must be in range of 0..<2048"
            }
            require(cameraLockedPlayerIndex == -1 || cameraLockedPlayerIndex in 0..<2048) {
                "Player index must be -1, or in range of 0..<2048"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WorldEntityTarget

            if (index != other.index) return false
            if (cameraLockedPlayerIndex != other.cameraLockedPlayerIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + cameraLockedPlayerIndex
            return result
        }

        override fun toString(): String =
            "WorldEntityTarget(" +
                "index=$index, " +
                "cameraLockedPlayerIndex=$cameraLockedPlayerIndex" +
                ")"
    }
}
