package net.rsprox.proxy.config

@Suppress("unused")
public class ProxyProperty<T>(
    public val name: String,
    public val type: PropertyType<T>,
) {
    internal companion object {
        // proxy
        val PROXY_PORT_MIN = ProxyProperty("proxy.port.min", IntProperty)
        val WORLDLIST_ENDPOINT = ProxyProperty("endpoints.worldlist", StringProperty)
        val JAV_CONFIG_ENDPOINT = ProxyProperty("endpoints.javconfig", StringProperty)
        val BIND_TIMEOUT_SECONDS = ProxyProperty("bind.timeout.seconds", IntProperty)
        val WORLDLIST_REFRESH_SECONDS = ProxyProperty("worldlist.refresh.seconds", IntProperty)
        val BINARY_WRITE_INTERVAL_SECONDS = ProxyProperty("binary.write.interval.seconds", IntProperty)

        // gui
        val APP_THEME = ProxyProperty("app.theme", StringProperty)
        val APP_MAXIMIZED = ProxyProperty("app.maximized", BooleanProperty)
        val APP_WIDTH = ProxyProperty("app.width", IntProperty)
        val APP_HEIGHT = ProxyProperty("app.height", IntProperty)
        val APP_POSITION_X = ProxyProperty("app.position.x", IntProperty)
        val APP_POSITION_Y = ProxyProperty("app.position.y", IntProperty)
        val FILTERS_STATUS = ProxyProperty("filters.status", IntProperty)
        val SELECTED_CLIENT = ProxyProperty("app.client", IntProperty)
    }
}
