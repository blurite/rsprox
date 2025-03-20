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
public class CamTargetV3(
    public val type: CamTargetType,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamTargetV3

        return type == other.type
    }

    override fun hashCode(): Int = type.hashCode()

    override fun toString(): String = "CamTargetV3(type=$type)"

    /**
     * A sealed interface for various camera target types.
     */
    public sealed interface CamTargetType

    /**
     * Camera target type for players. This will focus the camera on a player on a specific world entity.
     * If the player by the specified [targetIndex] cannot be found, the camera will be set back on
     * local player.
     * @property targetIndex the index of the player who to set the camera on.
     */
    public class PlayerCamTarget(
        public val worldEntityIndex: Int,
        public val targetIndex: Int,
    ) : CamTargetType {
        init {
            require(worldEntityIndex == -1 || worldEntityIndex in 0..<4096) {
                "World entity index must be -1, or in range of 0..<4096"
            }
            require(targetIndex in 0..<2048) {
                "Index must be in range of 0..<2048"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PlayerCamTarget

            if (worldEntityIndex != other.worldEntityIndex) return false
            if (targetIndex != other.targetIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = worldEntityIndex
            result = 31 * result + targetIndex
            return result
        }

        override fun toString(): String {
            return "PlayerCamTarget(" +
                "worldEntityIndex=$worldEntityIndex, " +
                "targetIndex=$targetIndex" +
                ")"
        }
    }

    /**
     * Camera target type for NPCs. This will focus the camera on a specific NPC on a specific worldentity.
     * If the NPC by the specified [targetIndex] cannot be found, the camera will be set back on
     * local player.
     * @property targetIndex the index of the NPC who to set the camera on.
     */
    public class NpcCamTarget(
        public val worldEntityIndex: Int,
        public val targetIndex: Int,
    ) : CamTargetType {
        init {
            require(worldEntityIndex == -1 || worldEntityIndex in 0..<4096) {
                "World entity index must be -1, or in range of 0..<4096"
            }
            require(targetIndex in 0..<65536) {
                "Index must be in range of 0..<65536"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NpcCamTarget

            if (worldEntityIndex != other.worldEntityIndex) return false
            if (targetIndex != other.targetIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = worldEntityIndex
            result = 31 * result + targetIndex
            return result
        }

        override fun toString(): String {
            return "NpcCamTarget(" +
                "worldEntityIndex=$worldEntityIndex, " +
                "targetIndex=$targetIndex" +
                ")"
        }
    }

    /**
     * Camera target type for world entities. This will focus the camera on a specific world entity.
     * If the world entity by the specified [targetIndex] cannot be found, the camera will be set back on
     * local player.
     * Additionally, depth buffering (z-buffer) will be enabled when this type of camera target is used.
     * @property targetIndex the index of the world entity who to set the camera on.
     */
    public class WorldEntityTarget(
        public val worldEntityIndex: Int,
        public val targetIndex: Int,
    ) : CamTargetType {
        init {
            require(worldEntityIndex == -1 || worldEntityIndex in 0..<4096) {
                "Source world entity index must be -1 or in range of 0..<4096"
            }
            require(targetIndex in 0..<4096) {
                "World entity target index must be in range of 0..<4096"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WorldEntityTarget

            if (worldEntityIndex != other.worldEntityIndex) return false
            if (targetIndex != other.targetIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = worldEntityIndex
            result = 31 * result + targetIndex
            return result
        }

        override fun toString(): String {
            return "WorldEntityTarget(" +
                "worldEntityIndex=$worldEntityIndex, " +
                "targetIndex=$targetIndex" +
                ")"
        }
    }
}
