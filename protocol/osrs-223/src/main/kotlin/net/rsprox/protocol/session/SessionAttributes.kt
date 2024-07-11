package net.rsprox.protocol.session

import net.rsprox.protocol.reflection.ReflectionCheck

private var Session.reflectionCheckMap: MutableMap<Int, List<ReflectionCheck>>? by attribute()

internal fun Session.getReflectionChecks(): MutableMap<Int, List<ReflectionCheck>> {
    val existingChecks = this.reflectionCheckMap
    if (existingChecks != null) {
        return existingChecks
    }
    val checks = mutableMapOf<Int, List<ReflectionCheck>>()
    this.reflectionCheckMap = checks
    return checks
}
