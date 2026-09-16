package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo

import net.rsprox.cache.api.rs3.Rs3NpcDefinition
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

private var Session.npcMorphVariables: NpcMorphVariables? by attribute()

internal fun Session.npcMorphVariables(): NpcMorphVariables =
    npcMorphVariables ?: NpcMorphVariables().also { npcMorphVariables = it }

/**
 * Server-visible player variables, not NPC-local config parameters.
 * Native actor+0x1068 points at client+0x19b60. Client-script overrides/timed
 * synchronization are not observable from packets and are not simulated here.
 */
internal class NpcMorphVariables {
    private val values = mutableMapOf<Int, Int>()

    fun clear() {
        values.clear()
    }

    fun set(
        id: Int,
        value: Int,
    ) {
        values[id] = value
    }

    fun setBit(
        id: Int,
        value: Int,
        definitions: Rs3PacketDefinitions,
    ) {
        val bit = definitions.getVarbit(id)
        if (bit.domain != 0) return
        // All revision-950 NPC selectors are integer (script type 0). Other domains/types
        // still decode normally; they are not inputs to this NPC-specific state.
        if (definitions.getVariable(Rs3VariableDomain.PLAYER, bit.base).scriptType != 0) return
        require(bit.endBit < 32) { "NPC morph variable requires an integer player varbit: $id" }
        val mask = (1 shl (bit.endBit - bit.startBit + 1)) - 1
        // Native 0x78c460 leaves the old value unchanged for an out-of-range update.
        if (value < 0 || value.toLong() > (mask.toLong() and 0xffffffffL)) return
        val old = get(bit.base, definitions)
        values[bit.base] = (old and (mask shl bit.startBit).inv()) or (value shl bit.startBit)
    }

    private fun get(
        id: Int,
        definitions: Rs3PacketDefinitions,
    ): Int {
        values[id]?.let { return it }
        val definition = definitions.getVariable(Rs3VariableDomain.PLAYER, id)
        check(
            definition.scriptType == 0,
        ) { "Uninitialized NPC morph selector $id has script type ${definition.scriptType}" }
        return 0
    }

    fun resolve(
        type: Int,
        definitions: Rs3PacketDefinitions,
    ): Rs3NpcDefinition {
        val base = definitions.getNpc(type)
        val morph = base.morph ?: return base
        val selector =
            when {
                morph.varbit != -1 -> {
                    val bit = definitions.getVarbit(morph.varbit)
                    require(bit.domain == 0 && bit.endBit < 32) { "NPC morph uses non-player varbit ${morph.varbit}" }
                    (get(bit.base, definitions) ushr bit.startBit) and ((1 shl (bit.endBit - bit.startBit + 1)) - 1)
                }
                morph.varp != -1 -> get(morph.varp, definitions)
                else -> -1
            }
        val selected = if (selector in 0 until morph.types.lastIndex) morph.types[selector] else morph.types.last()
        // Native 0x77f690 performs one lookup, not recursive morph resolution.
        return if (selected == -1) Rs3NpcDefinition(0, 0) else definitions.getNpc(selected)
    }
}

internal fun Session.updateNpcMorphVarbit(
    id: Int,
    value: Int,
) {
    val definitions = rs3PacketDefinitions ?: return
    npcMorphVariables().setBit(id, value, definitions)
}
