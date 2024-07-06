package net.rsprox.proxy.config

import net.rsprox.proxy.config.ProxyProperty.Companion.BIND_TIMEOUT_SECONDS
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.JAV_CONFIG_URL
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_HTTP_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.PROXY_PORT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_ENDPOINT
import net.rsprox.proxy.config.ProxyProperty.Companion.WORLDLIST_REFRESH_SECONDS
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.readText

@JvmInline
internal value class ProxyProperties private constructor(
    @PublishedApi
    internal val properties: Properties,
) {
    constructor(path: Path) : this(loadFromPath(path))

    fun <T> getProperty(property: ProxyProperty<T>): T {
        return properties.getValue(property)
    }

    fun entryPairList(): List<Pair<Any, Any>> {
        return properties.toList()
    }

    companion object {
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
            properties.setValue(PROXY_PORT, 43595)
            properties.setValue(PROXY_HTTP_PORT, 43596)
            properties.setValue(WORLDLIST_ENDPOINT, "worldlist.ws")
            properties.setValue(JAV_CONFIG_ENDPOINT, "javconfig.ws")
            properties.setValue(JAV_CONFIG_URL, "https://oldschool.runescape.com/jav_config.ws")
            properties.setValue(BIND_TIMEOUT_SECONDS, 30)
            properties.setValue(WORLDLIST_REFRESH_SECONDS, 5)
            return properties
        }
    }
}
