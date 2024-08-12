package net.rsprox.patch.native

public class ConstStringSlice(
    public val old: String,
    public val new: String,
    public val failureBehaviour: FailureBehaviour,
    public val duplicateReplacementBehaviour: DuplicateReplacementBehaviour,
    public val priority: Int = 0,
)
