package net.rsprox.proxy.settings

import net.rsprox.cache.util.atomicWrite
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingCategory
import net.rsprox.shared.settings.SettingGroup
import net.rsprox.shared.settings.SettingSet
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText

public class DefaultSettingSet(
    private val path: Path,
    private val creationTime: Long,
    private var version: Int,
    private var name: String,
    private var active: Boolean,
    private val filters: MutableMap<String, Boolean>,
) : SettingSet {
    override fun getCreationTime(): Long {
        return creationTime
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        this.name = name
    }

    override fun deleteBackingFile() {
        path.deleteIfExists()
    }

    override fun get(setting: Setting): Boolean {
        return filters[setting.name] ?: setting.enabled
    }

    override fun set(
        setting: Setting,
        enabled: Boolean,
    ) {
        filters[setting.name] = enabled
        save()
    }

    override fun set(
        category: SettingCategory,
        enabled: Boolean,
    ) {
        for (filter in Setting.entries) {
            if (filter.category == category) {
                filters[filter.name] = enabled
            }
        }
        save()
    }

    override fun set(
        group: SettingGroup,
        enabled: Boolean,
    ) {
        for (filter in Setting.entries) {
            if (filter.group == group) {
                filters[filter.name] = enabled
            }
        }
        save()
    }

    override fun setAll(enabled: Boolean) {
        for (filter in Setting.entries) {
            filters[filter.name] = enabled
        }
        save()
    }

    override fun setDefaults() {
        for (filter in Setting.entries) {
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
        ): DefaultSettingSet {
            val time = System.currentTimeMillis()
            return DefaultSettingSet(
                rootPath.resolve("settings-$time.txt"),
                time,
                DEFAULT_VERSION,
                name,
                true,
                mutableMapOf(),
            ).apply {
                setDefaults()
            }
        }

        private fun buildString(propertyFilterSet: DefaultSettingSet): String {
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

        public fun load(path: Path): DefaultSettingSet {
            val text = path.readText()
            var version: Int = -1
            var name: String? = null
            var creationTime: Long = 0
            var active = false
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
            return DefaultSettingSet(
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
