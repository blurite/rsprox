package net.rsprox.proxy.rs3.transcriber.interfaces

import net.rsprox.protocol.rs3v949.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.rs3v949.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.rs3v949.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.rs3v949.game.incoming.model.objs.OpObj
import net.rsprox.protocol.rs3v949.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.rs3v949.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.rs3v949.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.rs3v949.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.rs3v949.game.incoming.model.unknown.RawUnknownClientPacket

public interface Rs3ClientPacketTranscriber {
    public fun if3Button(message: If3Button)

    public fun opNpc(message: OpNpc)

    public fun opLoc(message: OpLoc)

    public fun opObj(message: OpObj)

    public fun opPlayer(message: OpPlayer)

    public fun eventAppletFocus(message: EventAppletFocus)

    public fun eventNativeMouseClick(message: EventNativeMouseClick)

    public fun moveGameClick(message: MoveGameClick)

    public fun unknownClientOpcode(message: RawUnknownClientPacket)
}
