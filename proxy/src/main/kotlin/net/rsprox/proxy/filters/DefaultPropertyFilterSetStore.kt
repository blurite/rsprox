package net.rsprox.proxy.filters

import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import java.nio.file.Path

public class DefaultPropertyFilterSetStore(
    private val rootPath: Path,
    private val filterSets: MutableList<PropertyFilterSet> = mutableListOf(),
) : PropertyFilterSetStore {
    override fun create(index: Int): PropertyFilterSet {
        if (index == 0) {
            throw IllegalArgumentException("Element cannot be created at index 0.")
        }
        for (filter in filterSets) {
            if (filter is DefaultPropertyFilterSet) {
                filter.setActive(false)
            }
        }
        val filter = DefaultPropertyFilterSet.create(rootPath, "Unnamed")
        filterSets += filter
        return filter
    }

    override fun delete(index: Int): PropertyFilterSet? {
        if (index == 0) throw IllegalArgumentException("Element at index 0 cannot be deleted.")
        val element = filterSets.getOrNull(index) ?: return null
        filterSets.removeAt(index)
        return element
    }

    override fun get(index: Int): PropertyFilterSet? {
        return filterSets.getOrNull(index)
    }

    override fun getActive(): PropertyFilterSet {
        val activeFilter =
            filterSets
                .filterIsInstance<DefaultPropertyFilterSet>()
                .find { it.isActive() }
        return activeFilter ?: filterSets.first()
    }

    override fun setActive(index: Int) {
        for (filter in filterSets) {
            if (filter is DefaultPropertyFilterSet) {
                filter.setActive(false)
            }
        }
        val filterSet = filterSets.getOrNull(index) ?: return
        if (filterSet is DefaultPropertyFilterSet) {
            filterSet.setActive(true)
        }
    }

    public companion object {
        public fun load(path: Path): PropertyFilterSetStore {
            val list: MutableList<PropertyFilterSet> = mutableListOf(UnmodifiablePropertyFilterSet())
            val results = path.toFile().walkTopDown()
            for (file in results) {
                if (!file.isFile) continue
                val set =
                    try {
                        DefaultPropertyFilterSet.load(file.toPath())
                    } catch (e: Exception) {
                        continue
                    }
                list += set
            }
            return DefaultPropertyFilterSetStore(path, list)
        }
    }
}
