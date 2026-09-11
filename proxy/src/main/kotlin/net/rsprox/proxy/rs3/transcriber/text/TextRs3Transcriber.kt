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
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumerContainer

public class TextRs3Transcriber(
    private val sessionState: Rs3SessionState,
    private val consumers: MessageConsumerContainer,
    private val formatter: PropertyTreeFormatter,
    filterSetStore: PropertyFilterSetStore,
    settingSetStore: SettingSetStore,
) : Rs3Transcriber,
    Rs3ClientPacketTranscriber by TextRs3ClientPacketTranscriber(
        sessionState,
        filterSetStore,
        settingSetStore,
    ),
    Rs3ServerPacketTranscriber by TextRs3ServerPacketTranscriber(
        sessionState,
        filterSetStore,
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
        sessionState.setRoot()
        return true
    }

    override fun onTranscribeEnd() {
        val root: List<RootProperty> = sessionState.root
        if (root.isEmpty()) return
        for (property in root) {
            consumers.publish(formatter, sessionState.cycle, property)
        }
        sessionState.deleteRoot()
    }
}
