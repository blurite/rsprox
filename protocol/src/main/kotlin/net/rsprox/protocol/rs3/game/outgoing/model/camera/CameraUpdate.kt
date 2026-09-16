package net.rsprox.protocol.rs3.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class CameraUpdate(
    public val headerFlags: Int,
    public val lookMode: Int?,
    public val positionMode: Int?,
    public val settingsMask: Int?,
    public val settings: List<Setting>,
    public val lookPayload: Controller?,
    public val positionPayload: Controller?,
) : IncomingServerGameMessage {
    public data class Vector3(public val x: Float, public val y: Float, public val z: Float)

    public data class Quaternion(public val x: Float, public val y: Float, public val z: Float, public val w: Float)

    public sealed interface Setting {
        public val bit: Int

        public data class Vector(override val bit: Int, public val value: Vector3) : Setting

        public data class Pair(override val bit: Int, public val first: Float, public val second: Float) : Setting

        public data class ByteValue(override val bit: Int, public val value: Int) : Setting

        public data class Reserved(public val value: Int) : Setting {
            override val bit: Int = 7
        }

        public data class Effects(public val updates: List<EffectUpdate>) : Setting {
            override val bit: Int = 9
        }

        public data class ShortFloat(public val parameter: Int, public val value: Float) : Setting {
            override val bit: Int = 10
        }

        public data class Motion(
            public val firstVector: Vector3,
            public val secondVector: Vector3,
            public val first: Float,
            public val second: Float,
        ) : Setting {
            override val bit: Int = 12
        }

        public data class FloatValue(override val bit: Int, public val value: Float) : Setting
    }

    public data class EffectUpdate(
        public val operation: Int,
        public val id: Int,
        public val suppliedSubtype: Int?,
        public val effectiveSubtype: Int?,
        public val payload: EffectPayload?,
    )

    public sealed interface EffectPayload {
        public data class Axis(public val axis: Int, public val first: Float, public val second: Float) : EffectPayload

        public data class Scalar(public val value: Float) : EffectPayload
    }

    public sealed interface Controller {
        public data class Coordinate(public val value: Vector3) : Controller

        public data class ActorLook(
            public val targetKind: Int,
            public val targetIndex: Int,
            public val offset: Vector3,
            public val enabled: Boolean,
        ) : Controller

        public data class ActorPosition(
            public val targetKind: Int,
            public val targetIndex: Int,
            public val offset: Vector3,
            public val rotation: Quaternion,
            public val enabled: Boolean,
            public val parameter: Int,
            public val duration: Int,
        ) : Controller

        public data class Rotation(public val value: Quaternion) : Controller

        public data class Path(public val spline: Spline) : Controller

        public data class Paths(
            public val subtype: Int,
            public val entries: List<PathEntry>,
            public val parameters: List<List<Float>>,
        ) : Controller
    }

    public data class Spline(public val pointCount: Int, public val points: List<SplinePoint>)

    /** Control vectors are retained as transmitted, before native tangent reflection. */
    public data class SplinePoint(public val point: Vector3, public val control: Vector3, public val weight: Float)

    public data class PathEntry(public val spline: Spline, public val factor: Float)
}
