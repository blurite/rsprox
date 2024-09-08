package net.rsprox.shared.filters

public data class RegexFilter(
    public val protName: String,
    public val regex: Regex,
    public val perLine: Boolean,
)
