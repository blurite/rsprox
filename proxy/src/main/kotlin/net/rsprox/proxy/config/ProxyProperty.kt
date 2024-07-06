package net.rsprox.proxy.config

@Suppress("unused")
internal class ProxyProperty<T>(
    val name: String,
    val type: PropertyType<T>,
) {
    internal companion object {
        val PROXY_PORT = ProxyProperty("proxy.port", IntProperty)
        val PROXY_HTTP_PORT = ProxyProperty("proxy.http.port", IntProperty)
        val WORLDLIST_ENDPOINT = ProxyProperty("endpoints.worldlist", StringProperty)
        val JAV_CONFIG_ENDPOINT = ProxyProperty("endpoints.javconfig", StringProperty)
        val JAV_CONFIG_URL = ProxyProperty("url.javconfig", StringProperty)
        val BIND_TIMEOUT_SECONDS = ProxyProperty("bind.timeout.seconds", IntProperty)
        val WORLDLIST_REFRESH_SECONDS = ProxyProperty("worldlist.refresh.seconds", IntProperty)
    }
}
