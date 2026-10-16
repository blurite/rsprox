package net.rsprox.proxy.replay

/** Local replay credentials only. Requests are never forwarded to an authentication service. */
internal object ReplayAuthentication {
    fun response(path: String): String? =
        when (path) {
            "/shield/oauth/token" ->
                """{"access_token":"REPLAY_ACCESS_TOKEN","refresh_token":"REPLAY_REFRESH_TOKEN"}"""
            "/public/v1/games/YCfdbvr2pM1zUYMxJRexZY/play", "/game-session/v1/tokens" -> "REPLAY_LOGIN_TOKEN"
            "/game-session/v1/sessions" -> """{"sessionId":"REPLAY_SESSION"}"""
            "/game-session/v1/accounts" -> """[{"accountId":0,"displayName":"Replay Session","userHash":0}]"""
            else -> null
        }
}
