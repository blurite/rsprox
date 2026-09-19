package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.proxy.rs3.transcriber.Rs3Transcriber
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ClientPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ServerPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.string
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer

public class TextRs3Transcriber(
    private val sessionState: Rs3SessionState,
    private val consumers: MessageConsumerContainer,
    private val formatter: PropertyTreeFormatter,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3Transcriber,
    Rs3ClientPacketTranscriber by TextRs3ClientPacketTranscriber(
        sessionState,
        filterSetStore,
        settingSetStore,
    ),
    Rs3ServerPacketTranscriber by TextRs3ServerPacketTranscriber(
        sessionState,
        filterSetStore,
        settingSetStore,
    ) {
    private val npcInfoTranscriber =
        TextRs3NpcInfoTranscriber(
            sessionState,
            filterSetStore,
            settingSetStore,
        )

    private val playerInfoTranscriber =
        TextRs3PlayerInfoTranscriber(
            sessionState,
            filterSetStore,
            settingSetStore,
        )

    override fun npcInfo(message: NpcInfo) {
        npcInfoTranscriber.npcInfo(message)
    }

    override fun playerInfo(message: PlayerInfo) {
        playerInfoTranscriber.playerInfo(message)
    }

    override fun onTranscribeStart(): Boolean {
        val settings = settingSetStore.getActive()
        if (sessionState.isLobby) {
            if (settings[Setting.HIDE_RS3_LOBBY]) return false
        } else if (sessionState.cycle == 0 && settings[Setting.SKIP_FIRST_TICK]) {
            return false
        }
        sessionState.setRoot()
        return true
    }

    override fun onTranscribeFailure(exception: Exception) {
        if (sessionState.root.isEmpty()) sessionState.setRoot()
        sessionState.root.last().string(
            "transcribeFailure",
            "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}",
        )
    }

    override fun onTranscribeEnd() {
        val root: List<RootProperty> = sessionState.root
        if (root.isEmpty()) return
        // A consumer callback may start another transcript. Do not iterate or clear its live roots.
        sessionState.root = mutableListOf()
        val cycle = sessionState.cycle
        for (property in root) {
            if (isRegexSkipped(property)) continue
            consumers.publish(formatter, cycle, property)
        }
    }

    private fun isRegexSkipped(property: RootProperty): Boolean {
        val name = property.prot.lowercase()
        val filters =
            filterSetStore
                .getActive()
                .getRegexFilters()
                .filter { it.protName == name }
        if (filters.isEmpty()) {
            return false
        }
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
        return false
    }
}
