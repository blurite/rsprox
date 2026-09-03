package net.rsprox.proxy.binary

internal fun BinaryHeader.isOldSchoolRuneScape(): Boolean = worldHost.endsWith(".runescape.com")
