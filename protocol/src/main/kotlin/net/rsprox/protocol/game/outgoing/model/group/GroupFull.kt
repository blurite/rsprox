package net.rsprox.protocol.game.outgoing.model.group

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Group full packet performs full updates to the client's group table.
 * The add/change variable payload is currently kept as raw bytes because the
 * client decodes it from cache-defined group and member variable structures.
 * @property updates the list of decoded group updates.
 */
public class GroupFull(
    public val updates: List<GroupUpdate>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupFull

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "GroupFull(" +
            "updates=$updates" +
            ")"
    }

    public sealed interface GroupUpdate

    public class GroupDelete(
        public val index: Int,
    ) : GroupUpdate {
        init {
            require(index in 0..255) {
                "Index must be in range of 0..255"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GroupDelete

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "GroupDelete(" +
                "index=$index" +
                ")"
        }
    }

    public class GroupAddChange(
        public val index: Int,
        public val id: Int,
        public val uid: Long,
        public val variableData: ByteArray,
    ) : GroupUpdate {
        init {
            require(index in 0..255) {
                "Index must be in range of 0..255"
            }
            require(id != -1) {
                "Id must not be -1."
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GroupAddChange

            if (index != other.index) return false
            if (id != other.id) return false
            if (uid != other.uid) return false
            if (!variableData.contentEquals(other.variableData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + id
            result = 31 * result + uid.hashCode()
            result = 31 * result + variableData.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "GroupAddChange(" +
                "index=$index, " +
                "id=$id, " +
                "uid=$uid, " +
                "variableData=${variableData.contentToString()}" +
                ")"
        }
    }
}