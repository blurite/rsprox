package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoDecoder
import net.rsprox.proxy.rs3.transcriber.Rs3TranscriberPlugin
import net.rsprox.proxy.rs3.transcriber.Rs3TranscriberSession
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionTracker
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer

public class TextRs3TranscriberProvider {
    public fun provide(
        container: MessageConsumerContainer,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
        playerInfoDecoder: PlayerInfoDecoder,
    ): Rs3TranscriberSession {
        val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.Builder().build(),
            )
        val sessionState = Rs3SessionState()
        val sessionTracker = Rs3SessionTracker(sessionState, playerInfoDecoder)
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
