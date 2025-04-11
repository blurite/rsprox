package net.rsprox.gui.auth

import com.github.michaelbull.logging.InlineLogger
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities

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
            Runtime.getRuntime().exec("sudo --user ${System.getenv("SUDO_USER")} xdg-open $url")
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
