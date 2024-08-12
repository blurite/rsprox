package net.rsprox.patch.native

public enum class DuplicateReplacementBehaviour {
    ERROR_ON_DUPLICATES,
    WARN_ON_DUPLICATES,
    SKIP_ON_DUPLICATES,
    REPLACE_FIRST_OCCURRENCE_ONLY,
    REPLACE_ALL_OCCURRENCES,
}
