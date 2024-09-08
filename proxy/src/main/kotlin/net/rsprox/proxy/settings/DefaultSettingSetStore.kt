package net.rsprox.proxy.settings

import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import java.nio.file.Path

public class DefaultSettingSetStore(
    private val rootPath: Path,
    private val filterSets: MutableList<SettingSet> = mutableListOf(),
) : SettingSetStore {
    override val size: Int
        get() = filterSets.size

    override fun create(name: String): SettingSet {
        for (filter in filterSets) {
            if (filter is DefaultSettingSet) {
                filter.setActive(false)
            }
        }
        val filter = DefaultSettingSet.create(rootPath, name)
        filterSets += filter
        return filter
    }

    override fun delete(index: Int): SettingSet? {
        if (index == 0) throw IllegalArgumentException("Element at index 0 cannot be deleted.")
        val element = filterSets.getOrNull(index) ?: return null
        element.deleteBackingFile()
        filterSets.removeAt(index)
        return element
    }

    override fun get(index: Int): SettingSet? {
        return filterSets.getOrNull(index)
    }

    override fun getActive(): SettingSet {
        val activeFilter =
            filterSets
                .filterIsInstance<DefaultSettingSet>()
                .find { it.isActive() }
        return activeFilter ?: filterSets.first()
    }

    override fun setActive(index: Int) {
        for (filter in filterSets) {
            if (filter is DefaultSettingSet) {
                filter.setActive(false)
            }
        }
        val filterSet = filterSets.getOrNull(index) ?: return
        if (filterSet is DefaultSettingSet) {
            filterSet.setActive(true)
        }
    }

    public companion object {
        public fun load(path: Path): SettingSetStore {
            val list: MutableList<SettingSet> = mutableListOf()
            val results = path.toFile().walkTopDown()
            for (file in results) {
                if (!file.isFile) continue
                val set =
                    try {
                        DefaultSettingSet.load(file.toPath())
                    } catch (e: Exception) {
                        continue
                    }
                list += set
            }
            if (list.isEmpty()) {
                list += DefaultSettingSet.create(path, "Settings")
            }
            return DefaultSettingSetStore(path, list)
        }
    }
}
