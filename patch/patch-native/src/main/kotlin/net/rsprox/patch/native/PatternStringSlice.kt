package net.rsprox.patch.native

public class PatternStringSlice(
    public val pattern: Regex,
    public val replacement: String,
    public val failureBehaviour: FailureBehaviour,
    public val duplicateReplacementBehaviour: DuplicateReplacementBehaviour,
    public val priority: Int = 0,
)
