package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.EnvironmentOverride
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class EnvironmentOverrideDecoder : ProxyMessageDecoder<EnvironmentOverride> {
    override val prot: ClientProt = GameServerProt.ENVIRONMENT_OVERRIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EnvironmentOverride {
        val flags = buffer.g8()
        val fields = mutableListOf<EnvironmentOverride.Field>()
        // Native wire order differs from numeric flag order at bits 45 and 46.
        val order = (0..5) + listOf(45, 46) + (6..44) + 47
        for (bit in order) {
            if (flags and (1L shl bit) == 0L) continue
            fields +=
                when (bit) {
                    0, 6, 20, 21, 27, 43, 44 -> EnvironmentOverride.IntegerValue(bit, buffer.g4())
                    7, 15 -> EnvironmentOverride.WordValue(bit, buffer.g2())
                    16 -> EnvironmentOverride.WordReserved(bit, buffer.g2(), List(8) { buffer.g1() })
                    17, 18, 19 -> EnvironmentOverride.WordScalar(bit, buffer.g2(), Float.fromBits(buffer.g4()))
                    4, 32, 33, 34 ->
                        EnvironmentOverride.VectorValue(
                            bit,
                            Float.fromBits(buffer.g4()),
                            Float.fromBits(buffer.g4()),
                            Float.fromBits(buffer.g4()),
                        )
                    else -> EnvironmentOverride.ScalarValue(bit, Float.fromBits(buffer.g4()))
                }
        }
        val duration = buffer.g2()
        return EnvironmentOverride(flags, fields, duration)
    }
}
