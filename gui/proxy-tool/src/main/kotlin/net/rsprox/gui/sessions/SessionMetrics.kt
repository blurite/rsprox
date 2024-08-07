package net.rsprox.gui.sessions

public data class SessionMetrics(
    var username: String = "",
    var worldName: String = "",
    var bandInPerSec: Int = 0,
    var bandOutPerSec: Int = 0,
)
