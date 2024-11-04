package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * URL open packets are used to open a site on the target's default
 * browser.
 * @property url the url to connect to
 */
public class UrlOpen(
    public val url: String,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UrlOpen

        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override fun toString(): String {
        return "UrlOpen(url='$url')"
    }
}
