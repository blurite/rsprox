package net.rsprox.transcriber.rs3.text

import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.rs3.Rs3TranscriberPlugin
import net.rsprox.transcriber.rs3.Rs3TranscriberSession
import net.rsprox.transcriber.rs3.state.Rs3SessionState
import net.rsprox.transcriber.rs3.state.Rs3SessionTracker

public class TextRs3TranscriberProvider(
    private val symbols: SymbolDictionary,
) {
    public fun provide(
        container: MessageConsumerContainer,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        clientScripts: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider.EMPTY,
        isLobby: Boolean = false,
    ): Rs3TranscriberSession {
        val formatter = Rs3PropertyFormatter.create(settings, symbols, clientScripts)
        val sessionState = Rs3SessionState(clientScripts, isLobby)
        val sessionTracker = Rs3SessionTracker(sessionState)
        val plugin =
            Rs3TranscriberPlugin(
                TextRs3Transcriber(
                    sessionState,
                    container,
                    formatter,
                    filters,
                    settings,
                ),
            )
        return Rs3TranscriberSession(plugin, sessionTracker, sessionState)
    }
}
