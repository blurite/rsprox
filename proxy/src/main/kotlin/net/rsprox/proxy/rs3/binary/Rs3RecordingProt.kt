package net.rsprox.proxy.rs3.binary

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprox.protocol.ProtProvider
import kotlin.enums.EnumEntries

/** Recording-only records. Never include these in a live wire protocol table. */
public enum class Rs3RecordingProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    LOGIN_INITIALIZATION(0xFC, Prot.VAR_SHORT),
    LOBBY_TRANSFER(0xFD, Prot.VAR_SHORT),
    ;

    public companion object {
        public fun provider(wire: ProtProvider<ClientProt>): ProtProvider<ClientProt> {
            val reserved = entries.associateBy { it.opcode }
            require(wire.allProts().filterIsInstance<ClientProt>().none { it.opcode in reserved }) {
                "RS3 recording opcodes collide with the live protocol"
            }
            return object : ProtProvider<ClientProt> {
                override fun get(opcode: Int): ClientProt = reserved[opcode] ?: wire[opcode]

                override fun allProts(): EnumEntries<*> = wire.allProts()
            }
        }
    }
}
