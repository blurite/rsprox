@file:Suppress("LiftReturnOrAssignment", "DuplicatedCode")

package net.rsprox.cache.type.util

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.type.util.Ops
import net.rsprox.cache.api.type.util.VarSupplier

/**
 * A container for all entity ops - regular, sub and multi variants of both.
 */
public class EntityOps : Ops {
    private val ops: MutableList<Op?> = mutableListOf()
    private val subops: MutableList<MutableList<SubOp>> = mutableListOf()
    private val multiops: MutableList<MutableList<MultiOp>> = mutableListOf()
    private val multisubops: MutableList<MutableMap<Int, MutableList<MultiSubOp>>?> = mutableListOf()

    /**
     * Decodes the ops from the [buf].
     * @param opcode the opcode being decoded
     * @param minOpOpcode the minimum opcode for regular ops
     * @param maxOpOpcode the maximum opcode (inclusive) for regular ops
     * @param subopOpcode the opcode for subop decoding
     * @param multiopOpcode the opcode for multiop decoding
     * @param multisubopOpcode the opcode for multisubop decoding
     * @return whether anything was decoded
     */
    public fun gOps(
        buf: JagByteBuf,
        opcode: Int,
        minOpOpcode: Int,
        maxOpOpcode: Int,
        subopOpcode: Int,
        multiopOpcode: Int,
        multisubopOpcode: Int,
    ): Boolean {
        when (opcode) {
            in minOpOpcode..maxOpOpcode -> {
                val op = opcode - minOpOpcode
                val label = buf.gjstr()
                if (!label.equals("Hidden", ignoreCase = true)) {
                    setOp(op, label)
                }
                return true
            }
            subopOpcode -> {
                val op = buf.g1()
                val subop = buf.g1()
                val label = buf.gjstr()
                setSubOp(op, subop, label)
                return true
            }
            multiopOpcode -> {
                val op = buf.g1()
                val varp = buf.g2()
                val varbit = buf.g2()
                val min = buf.g4()
                val max = buf.g4()
                val label = buf.gjstr()
                setMultiOp(op, varp, varbit, min, max, label)
                return true
            }
            multisubopOpcode -> {
                val op = buf.g1()
                val subop = buf.g2()
                val varp = buf.g2()
                val varbit = buf.g2()
                val min = buf.g4()
                val max = buf.g4()
                val label = buf.gjstr()
                setMultiSubOp(op, subop, varp, varbit, min, max, label)
                return true
            }
            else -> return false
        }
    }

    private fun setOp(
        op: Int,
        label: String,
    ) {
        if (op < 0) return
        for (i in this.ops.size..op) {
            this.ops.add(null)
        }
        this.ops[op] = if (label.isEmpty()) Op(null) else Op(label)
    }

    private fun setSubOp(
        op: Int,
        subop: Int,
        label: String,
    ) {
        for (i in this.subops.size..op) {
            this.subops.add(mutableListOf())
        }

        val subOpList = this.subops[op]
        subOpList += SubOp(label, subop)
    }

    private fun setMultiOp(
        op: Int,
        varp: Int,
        varbit: Int,
        min: Int,
        max: Int,
        label: String,
    ) {
        for (i in this.multiops.size..op) {
            this.multiops.add(mutableListOf())
        }

        val multiOpList = this.multiops[op]
        multiOpList += MultiOp(label, varp, varbit, min, max)
    }

    private fun setMultiSubOp(
        op: Int,
        subop: Int,
        varp: Int,
        varbit: Int,
        min: Int,
        max: Int,
        label: String,
    ) {
        for (i in this.multisubops.size..op) {
            this.multisubops.add(null)
        }

        var multiSubOpMap = this.multisubops[op]
        if (multiSubOpMap == null) {
            multiSubOpMap = mutableMapOf()
            this.multisubops[op] = multiSubOpMap
        }

        if (subop !in multiSubOpMap) {
            multiSubOpMap[subop] = mutableListOf()
        }

        val multiSubOpList = multiSubOpMap.getValue(subop)
        multiSubOpList += MultiSubOp(label, subop, varp, varbit, min, max)
    }

    override fun hasOp(
        op: Int,
        varSupplier: VarSupplier,
    ): Boolean {
        return hasOp(op, 0, varSupplier)
    }

    public fun hasOp(
        op: Int,
        subop: Int,
        varSupplier: VarSupplier,
    ): Boolean {
        if (ops.getOrNull(op) == null || subop < 0) {
            return false
        }

        if (subop == 0) {
            val opLabel = getOpLabel(op, varSupplier)
            return opLabel != null && opLabel.isNotEmpty()
        }

        val subOpIndex = getSubOpIndex(op, subop)
        val subOpLabel = getSubOpLabel(op, subOpIndex, subop, varSupplier)
        return subOpLabel != null && subOpLabel.isNotEmpty()
    }

    override fun getSubOp(
        op: Int,
        subOpIndex: Int,
    ): Int {
        if (op >= this.subops.size) {
            return -1
        }

        val subops = this.subops[op]
        if (subOpIndex >= subops.size) {
            return -1
        }
        val subop = subops[subOpIndex]
        return subop.subop
    }

    override fun getOpLabel(
        op: Int,
        varSupplier: VarSupplier,
    ): String? {
        val option = this.ops.getOrNull(op) ?: return null

        if (op < this.multiops.size) {
            val multiOpIndex = getMultiOpIndex(op, varSupplier)
            if (multiOpIndex >= 0) {
                val multiops = this.multiops[op]
                val multiOp = multiops[multiOpIndex]
                return multiOp.label
            }
        }

        return option.label
    }

    override fun getSubOpLabel(
        op: Int,
        subOpIndex: Int,
        varSupplier: VarSupplier,
    ): String? {
        if (op >= this.ops.size) {
            return null
        }

        val subop = this.getSubOp(op, subOpIndex)
        if (subop == -1) {
            return null
        }
        return getSubOpLabel(op, subOpIndex, subop, varSupplier)
    }

    public fun getSubOpLabel(
        op: Int,
        subOpIndex: Int,
        subop: Int,
        varSupplier: VarSupplier,
    ): String? {
        if (op >= this.ops.size) {
            return null
        }

        val multiSubOpIndex = getMultiSubOpIndex(op, subop, varSupplier)
        if (multiSubOpIndex != -1) {
            val map = checkNotNull(this.multisubops[op])
            val list = checkNotNull(map[subop])
            val multisubop = list[multiSubOpIndex]
            return multisubop.label
        }

        val list = this.subops[op]
        val subOp = list[subOpIndex]
        return subOp.label
    }

    override fun hasAnyOp(): Boolean {
        return ops.any { it != null && it.label != null }
    }

    override fun getSubOpCount(op: Int): Int {
        return this.subops.getOrNull(op)?.size ?: 0
    }

    private fun getSubOpIndex(
        op: Int,
        subop: Int,
    ): Int {
        if (op >= subops.size) {
            return -1
        }

        return this.subops[op]
            .indexOfFirst { it.subop == subop }
    }

    private fun getMultiOpIndex(
        op: Int,
        varSupplier: VarSupplier,
    ): Int {
        if (op >= this.multiops.size) {
            return -1
        }

        val multiops = this.multiops[op]
        for (i in multiops.indices) {
            val multiOp = multiops[i]
            val varValue =
                if (multiOp.varbit != 0xFFFF) {
                    getVarbitValue(varSupplier, multiOp.varbit)
                } else {
                    varSupplier.getVarps()[multiOp.varp]
                }
            if (varValue in multiOp.min..multiOp.max) {
                return i
            }
        }

        return -1
    }

    private fun getMultiSubOpIndex(
        op: Int,
        subop: Int,
        varSupplier: VarSupplier,
    ): Int {
        if (op >= this.multisubops.size) {
            return -1
        }

        val multiSubOpMap =
            this.multisubops[op]
                ?: return -1

        val multiSubOps =
            multiSubOpMap[subop]
                ?: return -1

        for (i in multiSubOps.indices) {
            val multiSubOp = multiSubOps[i]
            val varValue =
                if (multiSubOp.varbit != 0xFFFF) {
                    getVarbitValue(varSupplier, multiSubOp.varbit)
                } else {
                    varSupplier.getVarps()[multiSubOp.varp]
                }

            if (varValue in multiSubOp.min..multiSubOp.max) {
                return i
            }
        }

        return -1
    }

    private fun getVarbitValue(
        varSupplier: VarSupplier,
        varbitId: Int,
    ): Int {
        val type =
            varSupplier.getVarBitType(varbitId)
                ?: return 0
        val varps = varSupplier.getVarps()
        val varpValue = varps[type.basevar]
        val bitmask = type.bitmask((type.endbit - type.startbit) + 1)
        return varpValue shr type.startbit and bitmask
    }

    public fun clearOps() {
        this.ops.clear()
        this.subops.clear()
        this.multiops.clear()
        this.multisubops.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityOps

        if (ops != other.ops) return false
        if (subops != other.subops) return false
        if (multiops != other.multiops) return false
        if (multisubops != other.multisubops) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ops.hashCode()
        result = 31 * result + subops.hashCode()
        result = 31 * result + multiops.hashCode()
        result = 31 * result + multisubops.hashCode()
        return result
    }

    override fun toString(): String {
        return "EntityOps(" +
            "ops=$ops, " +
            "subops=$subops, " +
            "multiops=$multiops, " +
            "multisubops=$multisubops" +
            ")"
    }

    private open class Op(
        val label: String?,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Op

            return label == other.label
        }

        override fun hashCode(): Int {
            return label?.hashCode() ?: 0
        }

        override fun toString(): String {
            return "Op(" +
                "label=$label" +
                ")"
        }
    }

    private class SubOp(
        label: String,
        val subop: Int,
    ) : Op(label) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as SubOp

            return subop == other.subop
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + subop
            return result
        }

        override fun toString(): String {
            return "SubOp(" +
                "subop=$subop" +
                ")"
        }
    }

    private open class MultiOp(
        label: String,
        val varp: Int,
        val varbit: Int,
        val min: Int,
        val max: Int,
    ) : Op(label) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as MultiOp

            if (varp != other.varp) return false
            if (varbit != other.varbit) return false
            if (min != other.min) return false
            if (max != other.max) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + varp
            result = 31 * result + varbit
            result = 31 * result + min
            result = 31 * result + max
            return result
        }

        override fun toString(): String {
            return "MultiOp(" +
                "varp=$varp, " +
                "varbit=$varbit, " +
                "min=$min, " +
                "max=$max" +
                ")"
        }
    }

    private class MultiSubOp(
        label: String,
        val subop: Int,
        varp: Int,
        varbit: Int,
        min: Int,
        max: Int,
    ) : MultiOp(
            label,
            varp,
            varbit,
            min,
            max,
        ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as MultiSubOp

            return subop == other.subop
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + subop
            return result
        }

        override fun toString(): String {
            return "MultiSubOp(" +
                "subop=$subop" +
                ")"
        }
    }
}
