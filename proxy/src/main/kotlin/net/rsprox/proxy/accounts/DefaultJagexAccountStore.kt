package net.rsprox.proxy.accounts

import com.github.michaelbull.logging.InlineLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.rsprox.cache.util.atomicWrite
import net.rsprox.shared.account.JagexAccount
import net.rsprox.shared.account.JagexAccountStore
import net.rsprox.shared.account.JagexCharacter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

public class DefaultJagexAccountStore(
    private val path: Path,
) : JagexAccountStore {
    override val accounts: MutableList<JagexAccount> = arrayListOf<JagexAccount>()
    override var selectedCharacterId: Int? = null
        set(value) {
            field = value
            write()
        }

    private fun init() {
        for (account in accounts) {
            refreshCharacters(account)
        }

        val characters = accounts.flatMap { it.characters }
        if (selectedCharacterId != null) {
            if (characters.none { it.accountId == selectedCharacterId }) {
                logger.warn { "Selected character id $selectedCharacterId is invalid, resetting" }
                selectedCharacterId = null
            }
        }
    }

    override fun add(account: JagexAccount) {
        check(!accounts.contains(account)) { "Account already exists" }
        accounts.add(account)
        write()

        refreshCharacters(account)
    }

    override fun delete(account: JagexAccount) {
        check(accounts.contains(account)) { "Account not found" }
        accounts.remove(account)
        write()
    }

    private fun write() {
        path.atomicWrite(buildString(this))
    }

    public fun refreshSessionId(jagexAccount: JagexAccount) {
        val request =
            Request
                .Builder()
                .url("$AUTH_GAME_SESSION_BASE/sessions")
                .post(gson.toJson(CreateSessionRequest(jagexAccount.idToken)).toRequestBody(jsonMediaType))
                .build()
        val response =
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: error("Session create request was null")
                if (!response.isSuccessful) {
                    logger.error { "Failed to retrieve session id: $body" }
                    return@use null
                }
                gson.fromJson(body, CreateSessionResponse::class.java)
            }
        if (response != null) {
            jagexAccount.sessionId = response.sessionId
            write()
        }
    }

    public fun refreshCharacters(jagexAccount: JagexAccount) {
        if (jagexAccount.sessionId == null) {
            refreshSessionId(jagexAccount)
        }
        if (jagexAccount.sessionId == null) {
            logger.error { "Failed to refresh characters list for jagex account, no session id is stored" }
            return
        }
        val request =
            Request
                .Builder()
                .url("$AUTH_GAME_SESSION_BASE/accounts")
                .header("Authorization", "Bearer ${jagexAccount.sessionId}")
                .header("Content-Type", "application/json")
                .get()
                .build()

        try {
            val characters =
                httpClient.newCall(request).execute().use { response ->
                    val body = response.body!!.string()
                    if (!response.isSuccessful) {
                        logger.error { "Failed to retrieve session id: $body" }
                        null
                    } else {
                        gson.fromJson<List<JagexCharacter>>(body, object : TypeToken<List<JagexCharacter>>() {}.type)
                    }
                }
            if (characters != null) {
                jagexAccount.updateCharacters(characters)
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Unable to load Jagex Account; continuing without."
            }
        }
    }

    public companion object {
        private const val AUTH_GAME_SESSION_BASE = "https://auth.jagex.com/game-session/v1"
        private val logger = InlineLogger()
        private val gson = Gson()
        private val httpClient = OkHttpClient()
        private val jsonMediaType = "application/json".toMediaType()

        public fun load(path: Path): DefaultJagexAccountStore {
            val store = DefaultJagexAccountStore(path)
            if (path.exists()) {
                val text = path.readText()
                for (line in text.lineSequence()) {
                    if (line.startsWith("account=")) {
                        val parts = line.substringAfter("account=").split(",", limit = 3)
                        val code = parts[0]
                        val idToken = parts[1]
                        val sessionId = parts.getOrNull(2)
                        val account = JagexAccount(code, idToken, sessionId)
                        store.add(account)
                    }
                    if (line.startsWith("selectedCharacterId=")) {
                        store.selectedCharacterId = line.substringAfter("selectedCharacterId=").toInt()
                    }
                }
            }
            store.init()
            return store
        }

        private fun buildString(store: DefaultJagexAccountStore): String {
            return buildString {
                for (account in store.accounts) {
                    append("account=${account.code},${account.idToken}")
                    if (account.sessionId != null) {
                        append(",${account.sessionId}")
                    }
                    appendLine()
                }

                if (store.selectedCharacterId != null) {
                    append("selectedCharacterId=${store.selectedCharacterId}")
                    appendLine()
                }
            }
        }
    }

    private data class CreateSessionRequest(
        val idToken: String,
    )

    private data class CreateSessionResponse(
        val sessionId: String,
    )
}
