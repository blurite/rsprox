package net.rsprox.gui.auth

import com.github.michaelbull.logging.InlineLogger
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.text.Regex

public class JagexAuthenticator {
    private val httpServer = AuthHttpServer()
    private var state: Int = 0

    public fun requestOAuth2(): CompletableFuture<AuthHttpServer.OAuth2Response> {
        val state = state++
        val url =
            "$AUTH_ENDPOINT?response_type=id_token+code" +
                "&client_id=$CLIENT_ID" +
                "&nonce=00000000" +
                "&state=${"%08d".format(state)}" +
                "&prompt=login" +
                "&scope=openid+offline"
        val future = httpServer.waitForResponse(state)
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            SwingUtilities.invokeLater {
                Desktop.getDesktop().browse(URI(url))
            }
        } else if (System.getProperty("os.name").contains("Linux")) {
            var username: String

            val loggedInUsers =
                Runtime
                    .getRuntime()
                    .exec("w")
                    .inputStream
                    .readBytes()
                    .toString(Charsets.UTF_8)

            // might be some fringe cases where this mistakenly matches but it's close enough
            val regex =
                Regex(
                    ".+sudo(?:.+gradlew proxy|.+rsprox(?:-launcher\\.jar|\\.AppImage))",
                    RegexOption.IGNORE_CASE,
                )

            val regexMatches = regex.findAll(loggedInUsers).toList()

            if (regexMatches.isNotEmpty()) {
                // just uses first match since rsprox wouldn't support multiple users due to ports anyway
                username = regexMatches[0].value.split(" ")[0].trim()
            } else {
                // fallback to previous method, prob non-default filename
                val homeDirectories = Path("/home/").listDirectoryEntries()

                if (homeDirectories.isEmpty()) {
                    logger.error { "No valid linux users found." }
                    return future
                }

                username = homeDirectories[0].name.replace("/home/", "")
            }

            Runtime.getRuntime().exec("sudo --user $username xdg-open $url")
        } else {
            error("Unsupported OS for opening browser")
        }
        return future
    }

    public fun ensureAuthServerOnline() {
        if (httpServer.isOnline) return
        httpServer.start()
    }

    private companion object {
        const val AUTH_ENDPOINT = "https://account.jagex.com/oauth2/auth"
        const val CLIENT_ID = "1fddee4e-b100-4f4e-b2b0-097f9088f9d2"
        private val logger = InlineLogger()
    }
}
