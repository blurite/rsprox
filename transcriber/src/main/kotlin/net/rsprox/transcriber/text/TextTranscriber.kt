package net.rsprox.transcriber.text

import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.interfaces.ClientPacketTranscriber
import net.rsprox.transcriber.interfaces.NpcInfoTranscriber
import net.rsprox.transcriber.interfaces.PlayerInfoTranscriber
import net.rsprox.transcriber.interfaces.ServerPacketTranscriber
import net.rsprox.transcriber.prot.GameServerProt
import net.rsprox.transcriber.state.SessionState

public class TextTranscriber private constructor(
    private val sessionState: SessionState,
    cacheProvider: CacheProvider,
    private val consumers: MessageConsumerContainer,
    private val formatter: PropertyTreeFormatter,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Transcriber,
    ClientPacketTranscriber by TextClientPacketTranscriber(
        sessionState,
        cacheProvider.get(),
        filterSetStore,
        settingSetStore,
    ),
    ServerPacketTranscriber by TextServerPacketTranscriber(
        sessionState,
        cacheProvider.get(),
        filterSetStore,
        settingSetStore,
        (formatter as OmitFilteredPropertyTreeFormatter).propertyFormatterCollection, // Unsafe but works for now
    ),
    PlayerInfoTranscriber by TextPlayerInfoTranscriber(
        sessionState,
        cacheProvider.get(),
        filterSetStore,
        settingSetStore,
    ),
    NpcInfoTranscriber by TextNpcInfoTranscriber(
        sessionState,
        cacheProvider.get(),
        filterSetStore,
        settingSetStore,
    ) {
    public constructor(
        cacheProvider: CacheProvider,
        sessionState: SessionState,
        consumers: MessageConsumerContainer,
        formatter: PropertyTreeFormatter,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
    ) : this(
        sessionState,
        cacheProvider,
        consumers,
        formatter,
        filters,
        settings,
    )

    override val cache: Cache = cacheProvider.get()

    override fun onTranscribeStart() {
        sessionState.setRoot()
    }

    override fun onTranscribeEnd() {
        val root = sessionState.root
        if (root.isEmpty()) return
        var cycle = sessionState.cycle
        // Decrement the cycle if we're logging server tick end
        if (sessionState.currentProt == GameServerProt.SERVER_TICK_END.name) {
            cycle--
        }
        for (property in root) {
            if (isRegexSkipped(property)) continue
            consumers.publish(formatter, cycle, property)
        }
        root.clear()
    }

    private fun isRegexSkipped(property: RootProperty): Boolean {
        val filters =
            filterSetStore
                .getActive()
                .getRegexFilters()
                .filter { it.protName == property.prot.lowercase() }
        if (filters.isNotEmpty()) {
            val formatted = formatter.format(property)
            for (filter in filters) {
                if (filter.perLine) {
                    for (line in formatted) {
                        if (filter.regex.containsMatchIn(line)) {
                            return true
                        }
                    }
                } else {
                    val combined = formatted.joinToString(separator = System.lineSeparator())
                    if (filter.regex.containsMatchIn(combined)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
