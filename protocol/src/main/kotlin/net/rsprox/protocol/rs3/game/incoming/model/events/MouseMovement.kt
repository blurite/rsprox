package net.rsprox.protocol.rs3.game.incoming.model.events

/** Delta time uses 20-ms ticks. The long absolute code 8191 also serves as the initial-sample marker. */
public sealed interface MouseMovement {
    public val elapsedTicks: Int
    public val nativeFlags: Int?

    public data class Delta(
        override val elapsedTicks: Int,
        public val dx: Int,
        public val dy: Int,
        override val nativeFlags: Int?,
    ) : MouseMovement

    /** (-1, -1) represents the native out-of-window position sentinel. */
    public data class Absolute(
        override val elapsedTicks: Int,
        public val x: Int,
        public val y: Int,
        override val nativeFlags: Int?,
    ) : MouseMovement
}
