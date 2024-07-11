package net.rsprox.protocol.session

import net.rsprox.protocol.reflection.ReflectionCheck

private var Session.reflectionChecks: MutableMap<Int, List<ReflectionCheck>>? by attribute()

internal fun Session.getReflectionChecks(): MutableMap<Int, List<ReflectionCheck>> {
    val existingChecks = this.reflectionChecks
    if (existingChecks != null) {
        return existingChecks
    }
    val checks = mutableMapOf<Int, List<ReflectionCheck>>()
    this.reflectionChecks = checks
    return checks
}
