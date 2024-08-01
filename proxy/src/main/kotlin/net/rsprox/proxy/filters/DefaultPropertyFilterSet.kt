package net.rsprox.proxy.filters

import net.rsprox.cache.util.atomicWrite
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.ProtCategory
import java.nio.file.Path
import kotlin.io.path.readText

public class DefaultPropertyFilterSet(
    private val path: Path,
    private val creationTime: Long,
    private var version: Int,
    private var name: String,
    private var active: Boolean,
    private val filters: MutableMap<String, Boolean>,
) : PropertyFilterSet {
    override fun getCreationTime(): Long {
        return creationTime
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        this.name = name
    }

    override fun get(filter: PropertyFilter): Boolean {
        return filters[filter.name] ?: filter.enabled
    }

    override fun set(
        filter: PropertyFilter,
        enabled: Boolean,
    ) {
        filters[filter.name] = enabled
        save()
    }

    override fun set(
        category: ProtCategory,
        enabled: Boolean,
    ) {
        for (filter in PropertyFilter.entries) {
            if (filter.category == category) {
                filters[filter.name] = enabled
            }
        }
        save()
    }

    override fun set(
        streamDirection: StreamDirection,
        enabled: Boolean,
    ) {
        for (filter in PropertyFilter.entries) {
            if (filter.direction == streamDirection) {
                filters[filter.name] = enabled
            }
        }
        save()
    }

    override fun setAll(enabled: Boolean) {
        for (filter in PropertyFilter.entries) {
            filters[filter.name] = enabled
        }
        save()
    }

    override fun setDefaults() {
        for (filter in PropertyFilter.entries) {
            filters[filter.name] = filter.enabled
        }
        save()
    }

    public fun isActive(): Boolean {
        return this.active
    }

    public fun setActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        save()
    }

    private fun save() {
        path.atomicWrite(buildString(this))
    }

    public companion object {
        private const val DEFAULT_VERSION: Int = 0

        public fun create(
            rootPath: Path,
            name: String,
        ): DefaultPropertyFilterSet {
            val time = System.currentTimeMillis()
            return DefaultPropertyFilterSet(
                rootPath.resolve("filters-$time.txt"),
                time,
                DEFAULT_VERSION,
                name,
                true,
                mutableMapOf(),
            ).apply {
                setDefaults()
            }
        }

        private fun buildString(propertyFilterSet: DefaultPropertyFilterSet): String {
            val builder = StringBuilder()
            builder.append("version=").append(propertyFilterSet.version).appendLine()
            builder.append("creationtime=").append(propertyFilterSet.creationTime).appendLine()
            builder.append("name=").append(propertyFilterSet.name).appendLine()
            for ((k, v) in propertyFilterSet.filters) {
                builder
                    .append(k)
                    .append('=')
                    .append(v)
                    .appendLine()
            }
            return builder.toString()
        }

        public fun load(path: Path): DefaultPropertyFilterSet {
            val text = path.readText()
            var version: Int = -1
            var name: String? = null
            var creationTime: Long = 0
            var active: Boolean = false
            val properties: MutableMap<String, Boolean> = mutableMapOf()
            for (line in text.lineSequence()) {
                if (line.startsWith("version=")) {
                    version = line.substringAfter("version=").toInt()
                    continue
                }
                if (line.startsWith("creationtime=")) {
                    creationTime = line.substringAfter("creationtime=").toLong()
                    continue
                }
                if (line.startsWith("name=")) {
                    name = line.substringAfter("name=")
                    continue
                }
                if (line.startsWith("active=")) {
                    active = line.substringAfter("active=").toBoolean()
                    continue
                }
                val signIndex = line.indexOf('=')
                if (signIndex == -1) continue
                val split = line.split('=')
                if (split.size != 2) continue
                val (filterName, filterValue) = split
                if (filterName.isBlank()) continue
                val booleanValue = filterValue.toBooleanStrictOrNull() ?: continue
                properties[filterName] = booleanValue
            }
            check(version != -1) {
                "Version not assigned"
            }
            check(name != null) {
                "Name not assigned"
            }
            return DefaultPropertyFilterSet(
                path,
                creationTime,
                version,
                name,
                active,
                properties,
            )
        }
    }
}
