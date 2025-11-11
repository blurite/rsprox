package net.rsprox.processor.filters

import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.filters.ProtCategory
import net.rsprox.shared.filters.RegexFilter

public object ProcessorPropertyFilterSetStore : PropertyFilterSetStore {
    override val size: Int
        get() = 1

    override fun create(name: String): PropertyFilterSet {
        throw UnsupportedOperationException("Cannot mutate processor property filter set store.")
    }

    override fun delete(index: Int): PropertyFilterSet? {
        throw UnsupportedOperationException("Cannot mutate processor property filter set store.")
    }

    override fun get(index: Int): PropertyFilterSet? {
        if (index != 0) {
            throw IndexOutOfBoundsException("Processor property filter only has a single property filter set.")
        }
        return ProcessorFilterSet
    }

    override fun getActive(): PropertyFilterSet {
        return ProcessorFilterSet
    }

    override fun setActive(index: Int) {
        throw UnsupportedOperationException("Cannot mutate processor property filter set store.")
    }

    private object ProcessorFilterSet : PropertyFilterSet {
        override fun getCreationTime(): Long {
            return System.currentTimeMillis()
        }

        override fun getName(): String {
            return "Processor"
        }

        override fun setName(name: String) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun deleteBackingFile() {
            throw UnsupportedOperationException("Processor filter set does not have a backing file.")
        }

        override fun get(filter: PropertyFilter): Boolean {
            return true
        }

        override fun set(filter: PropertyFilter, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun set(category: ProtCategory, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun set(streamDirection: StreamDirection, enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun setAll(enabled: Boolean) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun setDefaults() {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun getRegexFilters(): List<RegexFilter> {
            return emptyList()
        }

        override fun addRegexFilter(regexFilter: RegexFilter) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun removeRegexFilter(regexFilter: RegexFilter) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun replaceRegexFilter(
            oldRegexFilter: RegexFilter,
            newRegexFilter: RegexFilter
        ) {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }

        override fun clearRegexFilters() {
            throw UnsupportedOperationException("Cannot mutate processor property filter set.")
        }
    }
}
