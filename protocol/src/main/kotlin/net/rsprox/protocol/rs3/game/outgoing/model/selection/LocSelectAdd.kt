package net.rsprox.protocol.rs3.game.outgoing.model.selection

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Coordinate bytes encode x/z offsets biased by 127; shapeRotation retains the extension bit. */
public data class LocSelectAdd(
    public val coordinate: Int,
    public val id: Int,
    public val shapeRotation: Int,
    public val transform: Transform?,
) : IncomingServerGameMessage {
    /** Signed wire components, before native axis inversion and quaternion/scale normalization. */
    public data class Transform(
        public val flags: Int,
        public val quaternion: List<Int>?,
        public val translateX: Int?,
        public val translateY: Int?,
        public val translateZ: Int?,
        public val uniformScale: Int?,
        public val scaleX: Int?,
        public val scaleY: Int?,
        public val scaleZ: Int?,
    )
}
