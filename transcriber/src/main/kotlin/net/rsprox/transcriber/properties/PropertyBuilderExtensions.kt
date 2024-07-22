package net.rsprox.transcriber.properties

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public fun properties(builderAction: PropertyBuilder.() -> Unit): List<Property> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val builder = PropertyBuilder()
    builderAction(builder)
    return builder.build()
}

public fun emptyProperties(): List<Property> {
    return emptyList()
}
