package net.rsprox.proxy.config

import net.rsprox.proxy.config.ProxyProperty.Companion.APP_HEIGHT
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_THEME
import net.rsprox.proxy.config.ProxyProperty.Companion.APP_WIDTH
import net.rsprox.proxy.config.ProxyProperty.Companion.BINARY_WRITE_INTERVAL_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT_HTTP
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT_MIN
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.readText

@JvmInline
public value class ProxyProperties private constructor(
    @PublishedApi
    internal val properties: Properties,
) {
    public constructor(path: Path) : this(loadFromPath(path))

    public fun <T> getProperty(property: ProxyProperty<T>): T {
        return properties.getValue(property)
    }

    public fun <T> getPropertyOrNull(property: ProxyProperty<T>): T? {
        return properties.getValueOrNull(property)
    }

    public fun <T> setProperty(
        property: ProxyProperty<T>,
        value: T,
    ) {
        properties.setValue(property, value)
    }

    public fun saveProperties(path: Path) {
        properties.store(path.toFile().bufferedWriter(DEFAULT_PROPERTIES_CHARSET), null)
    }

    public fun entryPairList(): List<Pair<Any, Any>> {
        return properties.toList()
    }

    private companion object {
        private val DEFAULT_PROPERTIES_CHARSET: Charset = Charsets.ISO_8859_1

        private fun loadFromPath(path: Path): Properties {
            return if (path.exists()) {
                loadProperties(path.readText(DEFAULT_PROPERTIES_CHARSET))
            } else {
                val properties = createDefaultProperties()
                saveDefaultProperties(path, properties)
                properties
            }
        }

        private fun saveDefaultProperties(
            path: Path,
            properties: Properties,
        ) {
            properties.store(
                path.toFile().bufferedWriter(DEFAULT_PROPERTIES_CHARSET),
                "This properties file was automatically generated\r\n" +
                    "to show all the possible configuration options.",
            )
        }

        private fun loadProperties(text: String): Properties {
            val properties = Properties(createDefaultProperties())
            properties.load(text.byteInputStream(DEFAULT_PROPERTIES_CHARSET))
            return properties
        }

        private fun createDefaultProperties(): Properties {
            val properties = Properties()
            // proxy
            properties.setValue(PROXY_PORT_MIN, 43601)
            properties.setValue(PROXY_PORT_HTTP, 43600)
            properties.setValue(WORLDLIST_ENDPOINT, "worldlist.ws")
            properties.setValue(JAV_CONFIG_ENDPOINT, "javconfig.ws")
            properties.setValue(BIND_TIMEOUT_SECONDS, 30)
            properties.setValue(WORLDLIST_REFRESH_SECONDS, 5)
            properties.setValue(BINARY_WRITE_INTERVAL_SECONDS, 5 * 60)
            // gui
            properties.setValue(APP_THEME, "MaterialDeepOcean")
            properties.setValue(APP_WIDTH, 800)
            properties.setValue(APP_HEIGHT, 600)
            return properties
        }
    }
}
