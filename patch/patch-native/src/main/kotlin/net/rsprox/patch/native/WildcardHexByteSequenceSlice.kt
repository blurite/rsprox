package net.rsprox.patch.native

import net.rsprox.patch.native.processors.utils.HexBytePattern

public class WildcardHexByteSequenceSlice(
    public val old: HexBytePattern,
    public val new: HexBytePattern,
    public val failureBehaviour: FailureBehaviour,
    public val duplicateReplacementBehaviour: DuplicateReplacementBehaviour,
    public val priority: Int = 0,
)
