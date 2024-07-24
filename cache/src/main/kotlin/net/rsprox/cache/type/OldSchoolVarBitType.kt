package net.rsprox.cache.type

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.type.VarBitType

public class OldSchoolVarBitType(
    override val id: Int,
) : VarBitType {
    override var basevar: Int = 0
    override var startbit: Int = 0
    override var endbit: Int = 0

    public fun decode(
        revision: Int,
        buffer: JagByteBuf,
    ) {
        while (true) {
            val opcode = buffer.g1()
            if (opcode == 0) {
                break
            }
            decode(revision, opcode, buffer)
        }
    }

    private fun decode(
        @Suppress("UNUSED_PARAMETER") revision: Int,
        opcode: Int,
        buffer: JagByteBuf,
    ) {
        when (opcode) {
            1 -> {
                this.basevar = buffer.g2()
                this.startbit = buffer.g1()
                this.endbit = buffer.g1()
            }
            else -> {
                error("Invalid opcode: $opcode")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OldSchoolVarBitType

        if (id != other.id) return false
        if (basevar != other.basevar) return false
        if (startbit != other.startbit) return false
        if (endbit != other.endbit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + basevar
        result = 31 * result + startbit
        result = 31 * result + endbit
        return result
    }

    override fun toString(): String {
        return "OldSchoolVarBitType(" +
            "id=$id, " +
            "basevar=$basevar, " +
            "startbit=$startbit, " +
            "endbit=$endbit" +
            ")"
    }

    public companion object {
        public fun get(
            revision: Int,
            id: Int,
            buffer: JagByteBuf,
        ): VarBitType {
            val type = OldSchoolVarBitType(id)
            type.decode(revision, buffer)
            return type
        }
    }
}
