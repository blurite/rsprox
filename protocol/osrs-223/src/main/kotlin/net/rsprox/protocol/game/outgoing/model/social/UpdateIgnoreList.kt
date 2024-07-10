package net.rsprox.protocol.game.outgoing.model.social

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Update ignorelist is used to perform changes to the ignore list.
 * Unlike friend list, it is possible to delete ignore list entries
 * from the server's perspective.
 * @property ignores the list of ignores to add or remove.
 */
public class UpdateIgnoreList(
    public val ignores: List<IgnoredPlayer>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateIgnoreList

        return ignores == other.ignores
    }

    override fun hashCode(): Int {
        return ignores.hashCode()
    }

    override fun toString(): String {
        return "UpdateIgnoreList(ignores=$ignores)"
    }

    public sealed interface IgnoredPlayer {
        public val name: String
    }

    /**
     * Removed ignored entry is an ignored entry that is requested to be
     * deleted from the ignore list of this player.
     * @property name the name of the ignored player
     */
    public class RemovedIgnoredEntry(
        override val name: String,
    ) : IgnoredPlayer {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RemovedIgnoredEntry

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun toString(): String {
            return "RemovedIgnoredEntry(name='$name')"
        }
    }

    /**
     * Added ignore entry encompasses all the ignore list entries
     * which are added to the ignore list, be that during login or
     * individual additions of new entries.
     * @property name the name of the player to be added to the ignore list
     * @property previousName the previous name of that player, if they had any.
     * Set to null if there is no previous name associated.
     * @property note the note attached to this player.
     * This property is not used in any of the OldSchool RuneScape clients.
     * @property added whether the ignore list entry was just added, or if it's
     * a historic entry sent during login.
     * If the property is false, the client skips any existing name checks.
     */
    public class AddedIgnoredEntry(
        override val name: String,
        public val previousName: String?,
        public val note: String,
        public val added: Boolean,
    ) : IgnoredPlayer {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AddedIgnoredEntry

            if (name != other.name) return false
            if (previousName != other.previousName) return false
            if (note != other.note) return false
            if (added != other.added) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + (previousName?.hashCode() ?: 0)
            result = 31 * result + note.hashCode()
            result = 31 * result + added.hashCode()
            return result
        }

        override fun toString(): String {
            return "AddedIgnoredEntry(" +
                "name='$name', " +
                "previousName=$previousName, " +
                "note='$note', " +
                "added=$added" +
                ")"
        }
    }
}
