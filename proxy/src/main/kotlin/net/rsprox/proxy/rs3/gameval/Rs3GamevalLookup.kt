package net.rsprox.proxy.rs3.gameval

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private data class GamevalFile(
    val revision: Int = 0,
    val url: String? = null,
    val date: String? = null,
    @SerializedName("entries") val entries: Map<String, String> = emptyMap(),
)

public object Rs3GamevalLookup {
    private val gson = Gson()

    @Volatile private var varpNames: Map<Int, String> = emptyMap()
    @Volatile private var varcNames: Map<Int, String> = emptyMap()
    @Volatile private var varbitNames: Map<Int, String> = emptyMap()
    @Volatile private var midiNames: Map<Int, String> = emptyMap()
    @Volatile private var interfaceNames: Map<Int, String> = emptyMap()
    @Volatile private var componentNames: Map<String, String> = emptyMap()
    @Volatile private var locNames: Map<Int, String> = emptyMap()
    @Volatile private var npcNames: Map<Int, String> = emptyMap()
    @Volatile private var cs2Names: Map<Int, String> = emptyMap()
    @Volatile private var seqNames: Map<Int, String> = emptyMap()
    @Volatile private var soundNames: Map<Int, String> = emptyMap()
    @Volatile private var objNames: Map<Int, String> = emptyMap()
    @Volatile private var invNames: Map<Int, String> = emptyMap()
    @Volatile private var modelNames: Map<Int, String> = emptyMap()

    public var revision: Int = 0
        private set

    public fun loadVarp(): Unit = load("/gameval/var_player.json") { varpNames = it }
    public fun loadVarc(): Unit = load("/gameval/var_client.json") { varcNames = it }
    public fun loadVarbit(): Unit = load("/gameval/varbit.json") { varbitNames = it }
    public fun loadMidi(): Unit = load("/gameval/midi.json") { midiNames = it }
    public fun loadInterface(): Unit = load("/gameval/interface.json") { interfaceNames = it }
    public fun loadComponent(): Unit = loadRaw("/gameval/component.json") { componentNames = it }
    public fun loadLoc(): Unit = load("/gameval/loc.json") { locNames = it }
    public fun loadNpc(): Unit = load("/gameval/npc.json") { npcNames = it }
    public fun loadCs2(): Unit = load("/gameval/cs2.json") { cs2Names = it }
    public fun loadSeq(): Unit = load("/gameval/seq.json") { seqNames = it }
    public fun loadSound(): Unit = load("/gameval/sound.json") { soundNames = it }
    public fun loadObj(): Unit = load("/gameval/obj.json") { objNames = it }
    public fun loadinv(): Unit = load("/gameval/inv.json") { invNames = it }
    public fun loadModel(): Unit = load("/gameval/model.json") { modelNames = it }

    public fun loadAll() {
        loadVarp()
        loadVarc()
        loadVarbit()
        loadMidi()
        loadInterface()
        loadComponent()
        loadLoc()
        loadNpc()
        loadCs2()
        loadSeq()
        loadSound()
        loadObj()
        loadinv()
        loadModel()
    }

    private fun load(
        resourcePath: String,
        assign: (Map<Int, String>) -> Unit,
    ) {
        val file = readJsonFile(resourcePath) ?: return
        val parsed = file.entries.mapNotNull { (key, name) ->
            val id = key.toIntOrNull()
            if (id == null) null else id to name
        }.toMap()
        revision = file.revision
        assign(parsed)
    }

    private fun loadRaw(
        resourcePath: String,
        assign: (Map<String, String>) -> Unit,
    ) {
        val file = readJsonFile(resourcePath) ?: return
        revision = file.revision
        assign(file.entries)
    }

    private fun readJsonFile(resourcePath: String): GamevalFile? {
        val stream = Rs3GamevalLookup::class.java.getResourceAsStream(resourcePath)
        if (stream == null) {
            return null
        }
        return try {
            stream.bufferedReader(Charsets.UTF_8).use { reader ->
                gson.fromJson(reader, GamevalFile::class.java)
            }
        } catch (e: Exception) {
            println("[gameval] failed to parse $resourcePath: $e")
            null
        }
    }

    public fun varp(id: Int): String = "${varpNames[id] ?: "?"}($id)"
    public fun varc(id: Int): String = "${varcNames[id] ?: "?"}($id)"
    public fun varbit(id: Int): String = "${varbitNames[id] ?: "?"}($id)"
    public fun midi(id: Int): String = "${midiNames[id] ?: "?"}($id)"
    public fun interfaceName(id: Int): String = "${interfaceNames[id] ?: "?"}($id)"
    public fun loc(id: Int): String = "${locNames[id] ?: "?"}($id)"
    public fun npc(id: Int): String = "${npcNames[id] ?: "?"}($id)"
    public fun cs2(id: Int): String = "${cs2Names[id] ?: "?"}($id)"
    public fun seq(id: Int): String = "${seqNames[id] ?: "?"}($id)"
    public fun sound(id: Int): String = "${soundNames[id] ?: "?"}($id)"
    public fun obj(id: Int): String = "${objNames[id] ?: "?"}($id)"
    public fun inv(id: Int): String = "${invNames[id] ?: "?"}($id)"
    public fun model(id: Int): String = "${modelNames[id] ?: "?"}($id)"

    public fun component(hash: Long): String {
        val interfaceId = ((hash ushr 16) and 0xFFFF).toInt()
        val componentId = (hash and 0xFFFF).toInt()
        val key = "$interfaceId:$componentId"

        val directComponent = componentNames[key]
        if (directComponent != null) {
            return "$directComponent($key)"
        }

        val interfaceName = interfaceNames[interfaceId] ?: "?"
        return "$interfaceName($interfaceId):$componentId"
    }

    public fun component(hash: Int): String = component(hash.toLong() and 0xFFFFFFFFL)
}
