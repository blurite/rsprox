package net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class MapAnim(
    public val id: Int,
    public val xInZone: Int,
    public val zInZone: Int,
    public val unk1: Int,
    public val unk2: Int,
    public val unk3: Int,
    public val unk4: Int,
    public val unk5: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "MapAnim(" +
            "id=$id, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone, " +
            "unk1=$unk1, " +
            "unk2=$unk2, " +
            "unk3=$unk3, " +
            "unk4=$unk4, " +
            "unk5=$unk5" +
            ")"
    }
}
