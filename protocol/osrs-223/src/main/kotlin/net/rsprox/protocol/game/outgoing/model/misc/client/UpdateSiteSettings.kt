package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update site settings packet is used to identify the given client.
 * The settings are sent as part of the URL when connecting to services
 * or secure RuneScape URLs.
 * @property settings the settings string to assign
 */
public class UpdateSiteSettings(
    public val settings: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateSiteSettings

        return settings == other.settings
    }

    override fun hashCode(): Int {
        return settings.hashCode()
    }

    override fun toString(): String {
        return "UpdateSiteSettings(settings='$settings')"
    }
}
