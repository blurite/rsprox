package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.Controller
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.EffectPayload
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate.Setting
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int

internal fun Property.appendCameraUpdate(message: CameraUpdate) {
    int("headerflags", message.headerFlags)
    message.lookMode?.let { int("lookmode", it) }
    message.positionMode?.let { int("positionmode", it) }
    message.settingsMask?.let {
        int("settingsmask", it)
        group("settings") {
            for (setting in message.settings) {
                group("setting") {
                    int("bit", setting.bit)
                    when (setting) {
                        is Setting.Vector -> vector("value", setting.value)
                        is Setting.Pair -> {
                            any("first", setting.first)
                            any("second", setting.second)
                        }
                        is Setting.ByteValue -> int("value", setting.value)
                        is Setting.Reserved -> int("value", setting.value)
                        is Setting.FloatValue -> any("value", setting.value)
                        is Setting.ShortFloat -> {
                            int("parameter", setting.parameter)
                            any("value", setting.value)
                        }
                        is Setting.Motion -> {
                            any("first", setting.first)
                            any("second", setting.second)
                            vector("firstvector", setting.firstVector)
                            vector("secondvector", setting.secondVector)
                        }
                        is Setting.Effects ->
                            group("effects") {
                                for (effect in setting.updates) {
                                    group("effect") {
                                        int("id", effect.id)
                                        int("operation", effect.operation)
                                        effect.suppliedSubtype?.let { int("suppliedsubtype", it) }
                                        effect.effectiveSubtype?.let { int("effectivesubtype", it) }
                                        when (val payload = effect.payload) {
                                            is EffectPayload.Axis -> {
                                                int("axis", payload.axis)
                                                any("first", payload.first)
                                                any("second", payload.second)
                                            }
                                            is EffectPayload.Scalar -> any("value", payload.value)
                                            null -> Unit
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
    message.lookPayload?.let { controller("look", it) }
    message.positionPayload?.let { controller("position", it) }
}

private fun Property.vector(
    name: String,
    value: CameraUpdate.Vector3,
) {
    group(name) {
        any("x", value.x)
        any("y", value.y)
        any("z", value.z)
    }
}

private fun Property.quaternion(
    name: String,
    value: CameraUpdate.Quaternion,
) {
    group(name) {
        any("x", value.x)
        any("y", value.y)
        any("z", value.z)
        any("w", value.w)
    }
}

private fun Property.spline(value: CameraUpdate.Spline) {
    int("pointcount", value.pointCount)
    group("points") {
        for (point in value.points) {
            group("point") {
                any("weight", point.weight)
                vector("position", point.point)
                vector("control", point.control)
            }
        }
    }
}

private fun Property.controller(
    name: String,
    value: Controller,
) {
    group(name) {
        when (value) {
            is Controller.Coordinate -> {
                any("type", "coordinate")
                vector("value", value.value)
            }
            is Controller.ActorLook -> {
                any("type", "actorlook")
                int("targetkind", value.targetKind)
                int("targetindex", value.targetIndex)
                boolean("enabled", value.enabled)
                vector("offset", value.offset)
            }
            is Controller.ActorPosition -> {
                any("type", "actorposition")
                int("targetkind", value.targetKind)
                int("targetindex", value.targetIndex)
                boolean("enabled", value.enabled)
                int("parameter", value.parameter)
                int("duration", value.duration)
                vector("offset", value.offset)
                quaternion("rotation", value.rotation)
            }
            is Controller.Rotation -> {
                any("type", "rotation")
                quaternion("value", value.value)
            }
            is Controller.Path -> {
                any("type", "path")
                spline(value.spline)
            }
            is Controller.Paths -> {
                any("type", "paths")
                int("subtype", value.subtype)
                group("paths") {
                    for (entry in value.entries) {
                        group("path") {
                            any("factor", entry.factor)
                            spline(entry.spline)
                        }
                    }
                }
                group("parameters") {
                    for (parameters in value.parameters) {
                        group("parameters") {
                            parameters.forEachIndexed { index, parameter -> any("value$index", parameter) }
                        }
                    }
                }
            }
        }
    }
}
