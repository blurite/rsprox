package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.Controller
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.EffectPayload
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.Setting
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

internal class CameraUpdateDecoder : ProxyMessageDecoder<CameraUpdate> {
    override val prot: ClientProt = GameServerProt.CAMERA_UPDATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CameraUpdate {
        val previous = session.cameraState ?: State()
        var lookReader = previous.lookReader
        var positionReader = previous.positionReader
        val effects = previous.effects.toMutableMap()
        val flags = buffer.g1()
        val lookMode = if (flags and 8 != 0) buffer.g1() else null
        val positionMode = if (flags and 16 != 0) buffer.g1() else null
        // Unknown mode IDs retain the previous native reader object.
        if (lookMode != null && lookMode in 0..6) lookReader = lookMode
        if (positionMode != null && positionMode in 0..4) positionReader = positionMode
        val mask = if (flags and 128 != 0) buffer.g2() else null
        val settings =
            buildList {
                if (mask != null) {
                    for (bit in 0..14) {
                        if (mask and (1 shl bit) == 0) continue
                        add(readSetting(buffer, bit, effects))
                    }
                }
            }
        val look = if (flags and 32 != 0 && lookReader != null) readController(buffer, lookReader, false) else null
        val position =
            if (flags and 64 != 0 && positionReader != null) readController(buffer, positionReader, true) else null
        require(buffer.readableBytes() == 0) { "CAMERA_UPDATE has ${buffer.readableBytes()} unexpected bytes" }
        // Failed/truncated packets must not poison the next packet's state, nor another session.
        session.cameraState = State(lookReader, positionReader, effects)
        return CameraUpdate(flags, lookMode, positionMode, mask, settings, look, position)
    }

    private fun readSetting(
        buffer: JagByteBuf,
        bit: Int,
        effects: MutableMap<Int, Int>,
    ): Setting =
        when (bit) {
            in 0..3 -> Setting.Vector(bit, vector(buffer))
            4, 5 -> Setting.Pair(bit, float(buffer), float(buffer))
            6, 8, 11 -> Setting.ByteValue(bit, buffer.g1())
            7 -> Setting.Reserved(buffer.g4())
            9 -> Setting.Effects(List(buffer.g1()) { readEffect(buffer, effects) })
            10 -> Setting.ShortFloat(buffer.g2(), float(buffer))
            12 -> Setting.Motion(vector(buffer), vector(buffer), float(buffer), float(buffer))
            13, 14 -> Setting.FloatValue(bit, float(buffer))
            else -> error("Unexpected camera setting bit $bit")
        }

    private fun readEffect(
        buffer: JagByteBuf,
        effects: MutableMap<Int, Int>,
    ): CameraUpdate.EffectUpdate {
        val operation = buffer.g1()
        val id = buffer.g1()
        if (operation == 0) {
            effects.remove(id)
            return CameraUpdate.EffectUpdate(operation, id, null, null, null)
        }
        val suppliedSubtype = buffer.g1()
        val subtype = effects[id] ?: suppliedSubtype
        val payload =
            when (subtype) {
                0 -> EffectPayload.Axis(buffer.g1(), float(buffer), float(buffer))
                1 -> EffectPayload.Scalar(float(buffer))
                else -> null
            }
        if (payload != null) effects[id] = subtype
        return CameraUpdate.EffectUpdate(operation, id, suppliedSubtype, subtype, payload)
    }

    private fun readController(
        buffer: JagByteBuf,
        mode: Int,
        position: Boolean,
    ): Controller =
        when {
            mode == 0 -> Controller.Coordinate(vector(buffer))
            mode == 1 && position ->
                Controller.ActorPosition(
                    targetKind = buffer.g1(),
                    targetIndex = buffer.g2(),
                    offset = vector(buffer),
                    rotation = quaternion(buffer),
                    enabled = buffer.g1() == 1,
                    parameter = buffer.g2(),
                    duration = buffer.g2(),
                )
            mode == 1 -> Controller.ActorLook(buffer.g1(), buffer.g2(), vector(buffer), buffer.g1() == 1)
            !position && mode == 3 -> Controller.Rotation(quaternion(buffer))
            !position && mode == 5 -> Controller.Path(spline(buffer))
            else -> {
                val subtype = if (position) mode - 1 else mode / 2
                val entries = List(buffer.g1()) { CameraUpdate.PathEntry(spline(buffer), float(buffer)) }
                val parameters =
                    if (subtype == 1) {
                        emptyList()
                    } else {
                        List(entries.size) { List(if (subtype == 2) 3 else 2) { float(buffer) } }
                    }
                Controller.Paths(subtype, entries, parameters)
            }
        }

    private fun spline(buffer: JagByteBuf): CameraUpdate.Spline {
        val count = buffer.gSmart1or2()
        val points = List(maxOf(count, 2)) { CameraUpdate.SplinePoint(vector(buffer), vector(buffer), float(buffer)) }
        return CameraUpdate.Spline(count, points)
    }

    private fun vector(buffer: JagByteBuf): CameraUpdate.Vector3 =
        CameraUpdate.Vector3(float(buffer), float(buffer), float(buffer))

    private fun quaternion(buffer: JagByteBuf): CameraUpdate.Quaternion =
        CameraUpdate.Quaternion(float(buffer), float(buffer), float(buffer), float(buffer))

    private fun float(buffer: JagByteBuf): Float = Float.fromBits(buffer.g4())

    private data class State(
        val lookReader: Int? = null,
        val positionReader: Int? = null,
        val effects: Map<Int, Int> = emptyMap(),
    )

    private companion object {
        var Session.cameraState: State? by attribute()
    }
}
