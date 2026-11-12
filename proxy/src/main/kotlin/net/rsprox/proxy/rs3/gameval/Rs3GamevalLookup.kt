package net.rsprox.proxy.rs3.gameval

import net.rsprox.cache.api.rs3.Rs3Gameval
import net.rsprox.cache.api.rs3.Rs3Gamevals
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary

public class Rs3GamevalLookup(
    private val gamevals: Rs3Gamevals = Rs3Gamevals.EMPTY,
) : SymbolDictionary {
    override fun start(): Unit = Unit

    override fun stop(): Unit = Unit

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? =
        when (type) {
            ScriptVarType.LOC_SHAPE -> LOC_SHAPE_NAMES.getOrNull(id)
            ScriptVarType.NPC_STAT -> NPC_STAT_NAMES.getOrNull(id)
            ScriptVarType.OBJ, ScriptVarType.NAMEDOBJ -> gamevals[Rs3Gameval.OBJ, id]
            ScriptVarType.LOC -> gamevals[Rs3Gameval.LOC, id]
            ScriptVarType.NPC -> gamevals[Rs3Gameval.NPC, id]
            ScriptVarType.SEQ -> gamevals[Rs3Gameval.SEQ, id]
            ScriptVarType.BAS -> gamevals[Rs3Gameval.BAS, id]
            ScriptVarType.MODEL -> gamevals[Rs3Gameval.MODEL, id]
            ScriptVarType.GRAPHIC -> gamevals[Rs3Gameval.GRAPHIC, id]
            ScriptVarType.STRUCT -> gamevals[Rs3Gameval.STRUCT, id]
            ScriptVarType.MAPELEMENT -> gamevals[Rs3Gameval.MAPELEMENT, id]
            ScriptVarType.CURSOR -> gamevals[Rs3Gameval.CURSOR, id]
            ScriptVarType.FONTMETRICS -> gamevals[Rs3Gameval.FONTMETRICS, id]
            ScriptVarType.QUEST -> gamevals[Rs3Gameval.QUEST, id]
            ScriptVarType.CATEGORY -> gamevals[Rs3Gameval.CATEGORY, id]
            ScriptVarType.DBROW -> gamevals[Rs3Gameval.DBROW, id]
            ScriptVarType.DBTABLE -> gamevals[Rs3Gameval.DBTABLE, id]
            ScriptVarType.ENUM -> gamevals[Rs3Gameval.ENUM, id]
            ScriptVarType.HEADBAR -> gamevals[Rs3Gameval.HEADBAR, id]
            ScriptVarType.HITMARK -> gamevals[Rs3Gameval.HITMARK, id]
            ScriptVarType.MATERIAL -> gamevals[Rs3Gameval.MATERIAL, id]
            ScriptVarType.INV, ScriptVarType.WRITEINV -> gamevals[Rs3Gameval.INV, id]
            ScriptVarType.MIDI -> gamevals[Rs3Gameval.MIDI, id]
            ScriptVarType.SYNTH -> gamevals[Rs3Gameval.SOUND, id]
            ScriptVarType.INTERFACE, ScriptVarType.TOPLEVELINTERFACE,
            ScriptVarType.OVERLAYINTERFACE, ScriptVarType.CLIENTINTERFACE -> gamevals[Rs3Gameval.INTERFACE, id]
            ScriptVarType.COMPONENT -> gamevals[Rs3Gameval.COMPONENT, id]
            else -> null
        }

    override fun getVarpName(id: Int): String? = gamevals[Rs3Gameval.VARP, id]

    override fun getRs3GamevalName(id: Int, type: Rs3Gameval): String? = gamevals[type, id]

    override fun getVarcName(id: Int): String? = gamevals[Rs3Gameval.VARC, id]

    override fun getVarNpcName(id: Int): String? = gamevals[Rs3Gameval.VARNPC, id]

    override fun getVarObjName(id: Int): String? = gamevals[Rs3Gameval.VAROBJ, id]

    override fun getVarbitName(id: Int): String? = gamevals[Rs3Gameval.VARBIT, id]

    // Scripts use the session's separately archived clientscript signatures.
    override fun getScriptName(id: Int): String? = null

    private companion object {
        val NPC_STAT_NAMES = listOf("attack", "defence", "strength", "hitpoints", "ranged", "magic", "necromancy")
        val LOC_SHAPE_NAMES = listOf(
            "wall_straight", "wall_diagonalcorner", "wall_l", "wall_squarecorner",
            "walldecor_straight_nooffset", "walldecor_straight_offset",
            "walldecor_diagonal_offset", "walldecor_diagonal_nooffset", "walldecor_diagonal_both",
            "wall_diagonal", "centrepiece_straight", "centrepiece_diagonal",
            "roof_straight", "roof_diagonal_with_roofedge", "roof_diagonal", "roof_l_concave",
            "roof_l_convex", "roof_flat", "roofedge_straight", "roofedge_diagonalcorner",
            "roofedge_l", "roofedge_squarecorner", "grounddecor", "wall_l_alt", "walldecor_diagonal_both_alt",
        )
    }
}
