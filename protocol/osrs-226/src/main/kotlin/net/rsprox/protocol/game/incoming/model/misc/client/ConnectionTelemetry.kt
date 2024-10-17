package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Connection telemetry is sent as part of the first packets during login,
 * written during the part of login that handles login rebuild messages
 * and player info initialization.
 * While the packet sends more properties than the four listed here,
 * they are never assigned a value, so they're just dummy zeros.
 * @property connectionLostDuration how long the connection was lost for.
 * Each unit here equals 10 milliseconds. The value is coerced in 0..65535
 * @property loginDuration how long the login took to complete.
 * Each unit here equals 10 milliseconds. The value is coerced in 0..65535
 * @property clientState the state the client is in
 * @property loginCount how many login attempts have occurred.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class ConnectionTelemetry private constructor(
    private val _connectionLostDuration: UShort,
    private val _loginDuration: UShort,
    private val _clientState: UShort,
    private val _loginCount: UShort,
) : IncomingGameMessage {
    public constructor(
        connectionLostDuration: Int,
        loginDuration: Int,
        clientState: Int,
        loginCount: Int,
    ) : this(
        connectionLostDuration.toUShort(),
        loginDuration.toUShort(),
        clientState.toUShort(),
        loginCount.toUShort(),
    )

    public val connectionLostDuration: Int
        get() = _connectionLostDuration.toInt()
    public val loginDuration: Int
        get() = _loginDuration.toInt()
    public val clientState: Int
        get() = _clientState.toInt()
    public val loginCount: Int
        get() = _loginCount.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionTelemetry

        if (_connectionLostDuration != other._connectionLostDuration) return false
        if (_loginDuration != other._loginDuration) return false
        if (_clientState != other._clientState) return false
        if (_loginCount != other._loginCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _connectionLostDuration.hashCode()
        result = 31 * result + _loginDuration.hashCode()
        result = 31 * result + _clientState.hashCode()
        result = 31 * result + _loginCount.hashCode()
        return result
    }

    override fun toString(): String =
        "ConnectionTelemetry(" +
            "connectionLostDuration=$connectionLostDuration, " +
            "loginDuration=$loginDuration, " +
            "clientState=$clientState, " +
            "loginCount=$loginCount" +
            ")"
}
