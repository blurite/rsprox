package net.rsprox.proxy.rs3.gameval

import com.google.gson.Gson
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary

/** Bundled RS3 symbols, kept separate from OSRS's cache and custom dictionaries. */
public object Rs3GamevalLookup : SymbolDictionary {
    private val npcStatNames = listOf("attack", "defence", "strength", "hitpoints", "ranged", "magic", "necromancy")

    // Publish one complete snapshot. Concurrent startup/formatter lookups share the same load.
    private val dictionary: Dictionary by lazy { loadDictionary() }

    public val revision: Int
        get() = dictionary.revision

    override fun start() {
        dictionary
    }

    // Classpath resources are read once and closed immediately; no watcher needs stopping.
    override fun stop(): Unit = Unit

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? =
        when (type) {
            ScriptVarType.NPC_STAT -> npcStatNames.getOrNull(id)
            ScriptVarType.OBJ, ScriptVarType.NAMEDOBJ -> name(Gameval.OBJ, id)
            ScriptVarType.LOC -> name(Gameval.LOC, id)
            ScriptVarType.NPC -> name(Gameval.NPC, id)
            ScriptVarType.SEQ -> name(Gameval.SEQ, id)
            ScriptVarType.BAS -> name(Gameval.BAS, id)
            ScriptVarType.MODEL -> name(Gameval.MODEL, id)
            ScriptVarType.GRAPHIC -> name(Gameval.GRAPHIC, id)
            ScriptVarType.STRUCT -> name(Gameval.STRUCT, id)
            ScriptVarType.MAPELEMENT -> name(Gameval.MAPELEMENT, id)
            ScriptVarType.CURSOR -> name(Gameval.CURSOR, id)
            ScriptVarType.FONTMETRICS -> name(Gameval.FONTMETRICS, id)
            ScriptVarType.QUEST -> name(Gameval.QUEST, id)
            ScriptVarType.CATEGORY -> name(Gameval.CATEGORY, id)
            ScriptVarType.DBROW -> name(Gameval.DBROW, id)
            ScriptVarType.DBTABLE -> name(Gameval.DBTABLE, id)
            ScriptVarType.ENUM -> name(Gameval.ENUM, id)
            ScriptVarType.HEADBAR -> name(Gameval.HEADBAR, id)
            ScriptVarType.HITMARK -> name(Gameval.HITMARK, id)
            ScriptVarType.MATERIAL -> name(Gameval.MATERIAL, id)
            ScriptVarType.INV -> name(Gameval.INV, id)
            ScriptVarType.MIDI -> name(Gameval.MIDI, id)
            ScriptVarType.SYNTH -> name(Gameval.SOUND, id)
            ScriptVarType.INTERFACE -> name(Gameval.INTERFACE, id)
            ScriptVarType.COMPONENT -> name(Gameval.COMPONENT, id)
            else -> null
        }

    override fun getVarpName(id: Int): String? = name(Gameval.VARP, id)

    override fun getVarcName(id: Int): String? = name(Gameval.VARC, id)

    override fun getVarNpcName(id: Int): String? = name(Gameval.VARNPC, id)

    override fun getVarObjName(id: Int): String? = name(Gameval.VAROBJ, id)

    override fun getVarbitName(id: Int): String? = name(Gameval.VARBIT, id)

    // Script names come from the session's cache-specific archived signatures, not bundled gamevals.
    override fun getScriptName(id: Int): String? = null

    private fun name(gameval: Gameval, id: Int): String? = dictionary.names.getValue(gameval)[id]

    private fun loadDictionary(): Dictionary {
        val gson = Gson()
        var revision: Int? = null
        val names =
            Gameval.entries.associateWith { gameval ->
                val path = "/gameval/${gameval.resource}.json"
                try {
                    val stream = checkNotNull(javaClass.getResourceAsStream(path)) { "Missing resource" }
                    val file =
                        stream.bufferedReader(Charsets.UTF_8).use { reader ->
                            checkNotNull(gson.fromJson(reader, GamevalFile::class.java)) { "Empty resource" }
                        }
                    val fileRevision = checkNotNull(file.revision) { "Missing revision" }
                    check(revision == null || revision == fileRevision) {
                        "Mixed gameval revisions: $revision and $fileRevision"
                    }
                    revision = fileRevision
                    val entries = checkNotNull(file.entries) { "Missing entries" }
                    val parsed = HashMap<Int, String>(entries.size)
                    for ((key, value) in entries) {
                        val name = checkNotNull(value) { "Missing name for $key" }
                        check(name.isNotBlank()) { "Blank name for $key" }
                        val id = parseId(gameval, key)
                        check(parsed.put(id, name) == null) { "Duplicate ID: $key" }
                    }
                    parsed
                } catch (exception: Exception) {
                    throw IllegalStateException("Unable to load RS3 gamevals from $path", exception)
                }
            }
        return Dictionary(checkNotNull(revision), names)
    }

    private fun parseId(gameval: Gameval, key: String): Int {
        if (gameval != Gameval.COMPONENT) {
            return key.toInt().also { require(it >= 0) { "Invalid ID: $key" } }
        }
        val parts = key.split(':')
        require(parts.size == 2) { "Invalid component ID: $key" }
        val interfaceId = parts[0].toInt()
        val componentId = parts[1].toInt()
        require(interfaceId in 0..0xFFFF && componentId in 0..0xFFFF) { "Invalid component ID: $key" }
        return (interfaceId shl 16) or componentId
    }

    private data class Dictionary(
        val revision: Int,
        val names: Map<Gameval, Map<Int, String>>,
    )

    private data class GamevalFile(
        val revision: Int? = null,
        val entries: Map<String, String?>? = null,
    )

    private enum class Gameval(
        val resource: String,
    ) {
        VARP("var_player"),
        VARC("var_client"),
        VARNPC("var_npc"),
        VAROBJ("var_object"),
        VARBIT("varbit"),
        MIDI("midi"),
        INTERFACE("interface"),
        COMPONENT("component"),
        LOC("loc"),
        NPC("npc"),
        SEQ("seq"),
        BAS("bas"),
        SOUND("sound"),
        OBJ("obj"),
        INV("inv"),
        MODEL("model"),
        GRAPHIC("graphic"),
        STRUCT("struct"),
        MAPELEMENT("map_element"),
        CURSOR("cursor"),
        FONTMETRICS("fontmetrics"),
        QUEST("quest"),
        CATEGORY("category"),
        DBROW("dbrow"),
        DBTABLE("dbtable"),
        ENUM("enum"),
        HEADBAR("headbar"),
        HITMARK("hitmark"),
        MATERIAL("material"),
        // These dictionaries currently have no corresponding shared ScriptVarType.
        VAR_PLAYER_GROUP("var_player_group"),
        VAR_CLAN("var_clan"),
        VAR_CLAN_SETTING("var_clan_setting"),
        ACHIEVEMENT("achievement"),
        STYLESHEET("stylesheet"),
        PARAM("param"),
        UI_ANIM("ui_anim"),
        UI_ANIM_CURVE("ui_anim_curve"),
    }
}
