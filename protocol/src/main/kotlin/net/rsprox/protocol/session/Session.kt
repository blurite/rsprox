package net.rsprox.protocol.session

import net.rsprox.protocol.reflection.ReflectionCheck

public class Session(
    public val localPlayerIndex: Int,
    public val reflectionChecks: MutableMap<Int, List<ReflectionCheck>>,
)
