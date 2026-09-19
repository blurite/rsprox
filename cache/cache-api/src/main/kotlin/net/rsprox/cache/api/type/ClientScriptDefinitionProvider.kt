package net.rsprox.cache.api.type

public fun interface ClientScriptDefinitionProvider {
    public fun getClientScriptDefinition(id: Int): ClientScriptDefinition?

    public companion object {
        public val EMPTY: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider { null }
    }
}
