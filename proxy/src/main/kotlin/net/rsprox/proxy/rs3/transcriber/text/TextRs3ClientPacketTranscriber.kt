package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3v949.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.rs3v949.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.rs3v949.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.rs3v949.game.incoming.model.objs.OpObj
import net.rsprox.protocol.rs3v949.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.rs3v949.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.rs3v949.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.rs3v949.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.rs3v949.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ClientPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore

public class TextRs3ClientPacketTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3ClientPacketTranscriber {
    private val root: RootProperty
        get() = checkNotNull(sessionState.root.lastOrNull()) {
            "No active root - onTranscribeStart() must run before dispatching to a transcriber method"
        }
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()
    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    override fun if3Button(message: If3Button) {
        if (!filters[PropertyFilter.IF_BUTTON]) return omit()

        val componentName = Rs3GamevalLookup.component(message.combinedId)
        root.children += AnyProperty("component", componentName, String::class.java)
        root.children += AnyProperty("op", message.op, Int::class.java)

        if (message.obj != -1) {
            root.children += AnyProperty("obj", Rs3GamevalLookup.obj(message.obj), String::class.java)
        }
        if (message.slot != -1) {
            root.children += AnyProperty("slot", message.slot, Int::class.java)
        }
    }

    override fun opNpc(message: OpNpc) {
        if (!filters[PropertyFilter.OPNPC]) return omit()
        root.children += AnyProperty("npc", sessionState.npcLabel(message.index), String::class.java)
        root.children += AnyProperty("op", message.op, Int::class.java)
        root.children += AnyProperty("run", message.run, Boolean::class.java)
    }

    override fun opLoc(message: OpLoc) {
        if (!filters[PropertyFilter.OPLOC]) return omit()
        root.children += AnyProperty("loc", Rs3GamevalLookup.loc(message.id), String::class.java)
        root.children += AnyProperty("x", message.x, Int::class.java)
        root.children += AnyProperty("y", message.y, Int::class.java)
        root.children += AnyProperty("op", message.op, Int::class.java)
        root.children += AnyProperty("run", message.run, Boolean::class.java)
    }

    override fun opObj(message: OpObj) {
        if (!filters[PropertyFilter.OPOBJ]) return omit()
        root.children += AnyProperty("obj", Rs3GamevalLookup.obj(message.id), String::class.java)
        root.children += AnyProperty("x", message.x, Int::class.java)
        root.children += AnyProperty("y", message.y, Int::class.java)
        root.children += AnyProperty("op", message.op, Int::class.java)
        root.children += AnyProperty("run", message.run, Boolean::class.java)
    }

    override fun opPlayer(message: OpPlayer) {
        if (!filters[PropertyFilter.OPPLAYER]) return omit()
        root.children += AnyProperty("player", sessionState.playerLabel(message.index), String::class.java)
        root.children += AnyProperty("op", message.op, Int::class.java)
        root.children += AnyProperty("run", message.run, Boolean::class.java)
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        if (!filters[PropertyFilter.EVENT_APPLET_FOCUS]) return omit()
        root.children += AnyProperty("inFocus", message.inFocus, Boolean::class.java)
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        if (!filters[PropertyFilter.EVENT_NATIVE_MOUSE_CLICK]) return omit()
        root.children += AnyProperty("code", message.code, Int::class.java)
        root.children += AnyProperty("x", message.x, Int::class.java)
        root.children += AnyProperty("y", message.y, Int::class.java)
        root.children += AnyProperty("lastTransmittedMouseClick", message.lastTransmittedMouseClick, Int::class.java)
    }

    override fun moveGameClick(message: MoveGameClick) {
        if (!filters[PropertyFilter.MOVE_GAMECLICK]) return omit()
        root.children += AnyProperty("x", message.x, Int::class.java)
        root.children += AnyProperty("y", message.y, Int::class.java)
        root.children += AnyProperty("run", message.run, Boolean::class.java)
    }

    override fun unknownClientOpcode(message: RawUnknownClientPacket) {
        if (!filters[PropertyFilter.UNKNOWN_CLIENT_OPCODE_HEX]) return omit()
        root.children += AnyProperty("opcode", message.opcode, Int::class.java)
        root.children += AnyProperty("name", message.name, String::class.java)
        root.children += AnyProperty(
            "bytes",
            message.bytes.joinToString(" ") { "%02x".format(it) },
            String::class.java,
        )
    }
}
