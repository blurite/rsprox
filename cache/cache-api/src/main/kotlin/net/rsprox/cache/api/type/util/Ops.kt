package net.rsprox.cache.api.type.util

/**
 * An interface for entity op handling.
 */
public interface Ops {
    /**
     * Checks whether this type has a given menu option at [op].
     * @param op the option value (0-4)
     * @param varSupplier a supplier for current varp state, and varbit types,
     * so that multi ops can be verified as well.
     */
    public fun hasOp(
        op: Int,
        varSupplier: VarSupplier,
    ): Boolean

    /**
     * Gets the subop value on option [op] at subop index [subOpIndex].
     * @param op the option value (0-4)
     * @param subOpIndex the index of the subop entry in the
     * entity ops container (as iterated by using [getSubOpCount]).
     */
    public fun getSubOp(
        op: Int,
        subOpIndex: Int,
    ): Int

    /**
     * Gets the option label for the given [op].
     * @param op the option value (0-4)
     * @param varSupplier a supplier for current varp state, and varbit types,
     * so that multi op overrides can be returned as well.
     */
    public fun getOpLabel(
        op: Int,
        varSupplier: VarSupplier,
    ): String?

    /**
     * Gets the subop label for the given [op], at subop index [subOpIndex].
     * @param op the option value (0-4)
     * @param subOpIndex the index of the subop entry in the
     * entity ops container (as iterated by using [getSubOpCount]).
     * @param varSupplier a supplier for current varp state, and varbit types,
     * so that multi subop overrides can be returned as well.
     */
    public fun getSubOpLabel(
        op: Int,
        subOpIndex: Int,
        varSupplier: VarSupplier,
    ): String?

    /**
     * Checks whether this entity ops container has any valid ops.
     * Note that this method only checks for ops, not multiops.
     */
    public fun hasAnyOp(): Boolean

    /**
     * Gets the total count of subops on the provided [op].
     * This can then be combined with either [getSubOp] or [getSubOpLabel].
     */
    public fun getSubOpCount(op: Int): Int
}
