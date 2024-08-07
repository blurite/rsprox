package net.rsprox.gui

import java.util.Properties

public data object AppProperties {
    public val version: String

    init {
        val properties =
            javaClass.getResourceAsStream("/rsprox.properties").use {
                Properties().apply { load(it) }
            }
        version = properties.getProperty("app.version")
    }
}
