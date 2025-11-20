package net.rsprox.protocol.v235.game.outgoing.model.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordFine
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.EnabledOpsExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV1
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV2
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV3
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV4
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV5
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV6
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityMoveSpeed
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityUpdateType

public class WorldEntityInfoClient : WorldEntityInfoDecoder {
    private var transmittedWorldEntityCount: Int = 0
    private val transmittedWorldEntity: IntArray = IntArray(25)
    private val worldEntity: Array<Any?> = arrayOfNulls(4096)
    private val updates: MutableMap<Int, WorldEntityUpdateType> = mutableMapOf()

    override fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
        version: Int,
    ): WorldEntityInfo {
        updates.clear()
        if (version >= 6) {
            decodeHighResolutionV4(buffer)
            decodeLowResolutionV4(buffer, baseCoord)
        } else if (version >= 5) {
            decodeHighResolutionV3(buffer)
            decodeLowResolutionV3(buffer, baseCoord)
        } else if (version >= 3) {
            decodeHighResolutionV2(buffer)
            decodeLowResolutionV2(buffer, baseCoord, version)
        } else {
            decodeHighResolutionV1(buffer)
            decodeLowResolutionV1(buffer, baseCoord)
        }
        val updates = updates.toMap()
        return when (version) {
            1 -> WorldEntityInfoV1(updates)
            2 -> WorldEntityInfoV2(updates)
            3 -> WorldEntityInfoV3(updates)
            4 -> WorldEntityInfoV4(updates)
            5 -> WorldEntityInfoV5(updates)
            6 -> WorldEntityInfoV6(updates)
            else -> error("Invalid version: $version")
        }
    }

    private fun decodeHighResolutionV1(buffer: JagByteBuf) {
        val count = buffer.g1()
        if (count < transmittedWorldEntityCount) {
            throw RuntimeException("dang")
        }
        if (count > transmittedWorldEntityCount) {
            throw RuntimeException("dang!")
        }
        this.transmittedWorldEntityCount = 0
        for (i in 0..<count) {
            val index = this.transmittedWorldEntity[i]
            val worldEntity = checkNotNull(this.worldEntity[index]) as WorldEntityV1
            val keep = buffer.g1() == 1
            if (!keep) {
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
                continue
            }
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            val dx = buffer.g1()
            val dz = buffer.g1()
            val angle = buffer.g2()
            val moveSpeed = buffer.g1()
            val coord = worldEntity.coordGrid
            worldEntity.coordGrid = CoordGrid(coord.level, coord.x + dx, coord.z + dz)
            worldEntity.angle = angle
            worldEntity.moveSpeed = WorldEntityMoveSpeed.Companion[moveSpeed]
            if (dx == 0 &&
                dz == 0 &&
                angle == worldEntity.angle &&
                worldEntity.moveSpeed ==
                WorldEntityMoveSpeed.ZERO
            ) {
                updates[index] = WorldEntityUpdateType.Idle
            } else {
                updates[index] =
                    WorldEntityUpdateType.ActiveV1(
                        angle,
                        worldEntity.coordGrid,
                        worldEntity.moveSpeed,
                    )
            }
        }
    }

    private fun decodeHighResolutionV2(buffer: JagByteBuf) {
        val count = buffer.g1()
        if (count < transmittedWorldEntityCount) {
            throw RuntimeException("dang")
        }
        if (count > transmittedWorldEntityCount) {
            throw RuntimeException("dang!")
        }
        this.transmittedWorldEntityCount = 0
        for (i in 0..<count) {
            val index = this.transmittedWorldEntity[i]
            val worldEntity = checkNotNull(this.worldEntity[index]) as WorldEntityV2
            val opcode = buffer.g1()
            val remove = opcode == 0
            if (remove) {
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
                continue
            }
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            if (opcode == 1) {
                updates[index] = WorldEntityUpdateType.Idle
                continue
            }
            val teleport = opcode == 3
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                val angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                val current = worldEntity.coordFine
                val next = CoordFine(current.x + deltaX, current.y + deltaY, current.z + deltaZ)
                worldEntity.coordFine = next
                worldEntity.angle = (worldEntity.angle + angle) and 2047
            }
            updates[index] =
                WorldEntityUpdateType.ActiveV2(
                    worldEntity.angle,
                    worldEntity.coordFine,
                    teleport,
                )
        }
    }

    private fun decodeHighResolutionV3(buffer: JagByteBuf) {
        val count = buffer.g1()
        if (count < transmittedWorldEntityCount) {
            for (i in count..<transmittedWorldEntityCount) {
                val index = this.transmittedWorldEntity[i]
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
            }
        }
        if (count > transmittedWorldEntityCount) {
            throw RuntimeException("dang!")
        }
        this.transmittedWorldEntityCount = 0
        for (i in 0..<count) {
            val index = this.transmittedWorldEntity[i]
            val worldEntity = checkNotNull(this.worldEntity[index]) as WorldEntityV3
            val opcode = buffer.g1()
            val remove = opcode == 0
            if (remove) {
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
                continue
            }
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            if (opcode == 1) {
                updates[index] = WorldEntityUpdateType.Idle
                continue
            }
            val teleport = opcode == 3
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                val angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                val current = worldEntity.coordFine
                val next = CoordFine(current.x + deltaX, current.y + deltaY, current.z + deltaZ)
                worldEntity.coordFine = next
                worldEntity.angle = (worldEntity.angle + angle) and 2047
            }
            updates[index] =
                WorldEntityUpdateType.ActiveV2(
                    worldEntity.angle,
                    worldEntity.coordFine,
                    teleport,
                )
        }
    }

    private fun decodeHighResolutionV4(buffer: JagByteBuf) {
        val count = buffer.g1()
        if (count < transmittedWorldEntityCount) {
            for (i in count..<transmittedWorldEntityCount) {
                val index = this.transmittedWorldEntity[i]
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
            }
        }
        if (count > transmittedWorldEntityCount) {
            throw RuntimeException("dang!")
        }
        this.transmittedWorldEntityCount = 0
        for (i in 0..<count) {
            val index = this.transmittedWorldEntity[i]
            val worldEntity = checkNotNull(this.worldEntity[index]) as WorldEntityV3
            val opcode = buffer.g1()
            val remove = opcode == 0
            if (remove) {
                this.worldEntity[index] = null
                updates[index] = WorldEntityUpdateType.HighResolutionToLowResolution
                continue
            }
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            if (opcode == 1) {
                val extendedInfo = decodeWorldEntityInfoExtendedInfo(buffer)
                if (extendedInfo.isEmpty()) {
                    updates[index] = WorldEntityUpdateType.Idle
                } else {
                    updates[index] = WorldEntityUpdateType.ExtendedInfoOnly(extendedInfo)
                }
                continue
            }
            val teleport = opcode == 3
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                val angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                val current = worldEntity.coordFine
                val next = CoordFine(current.x + deltaX, current.y + deltaY, current.z + deltaZ)
                worldEntity.coordFine = next
                worldEntity.angle = (worldEntity.angle + angle) and 2047
            }
            val extendedInfo = decodeWorldEntityInfoExtendedInfo(buffer)
            updates[index] =
                WorldEntityUpdateType.ActiveV3(
                    worldEntity.angle,
                    worldEntity.coordFine,
                    teleport,
                    extendedInfo,
                )
        }
    }

    private fun decodeWorldEntityInfoExtendedInfo(buffer: JagByteBuf): List<ExtendedInfo> {
        val flags = buffer.g1()
        if (flags == 0) {
            return emptyList()
        }
        val blocks = mutableListOf<ExtendedInfo>()
        if (flags and 0x1 != 0) {
            val id = buffer.g2()
            val delay = buffer.g1()
            blocks += SequenceExtendedInfo(id, delay)
        }

        if (flags and 0x2 != 0) {
            blocks += EnabledOpsExtendedInfo(buffer.g1())
        }
        return blocks
    }

    private fun decodeAngledCoordFineComponent(
        buffer: JagByteBuf,
        bitpackedOpcode: Int,
        shift: Int,
    ): Int {
        val opcode = (bitpackedOpcode shr shift) and 0x3
        return when (opcode) {
            3 -> buffer.g4()
            2 -> buffer.g2s()
            1 -> buffer.g1s()
            else -> 0
        }
    }

    private fun decodeLowResolutionV1(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ) {
        while (buffer.isReadable(10)) {
            val index = buffer.g2()
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            val sizeX = buffer.g1() * 8
            val sizeZ = buffer.g1() * 8
            val xInBuildArea = buffer.g1()
            val zInBuildArea = buffer.g1()
            val angle = buffer.g2()
            val unknownProperty = buffer.g2()
            val coord = CoordGrid(baseCoord.level, baseCoord.x + xInBuildArea, baseCoord.z + zInBuildArea)
            val worldEntity =
                WorldEntityV1(
                    index,
                    sizeX,
                    sizeZ,
                    unknownProperty,
                    coord,
                    angle,
                    WorldEntityMoveSpeed.ZERO,
                )
            this.worldEntity[index] = worldEntity
            this.updates[index] =
                WorldEntityUpdateType.LowResolutionToHighResolutionV1(
                    sizeX,
                    sizeZ,
                    angle,
                    unknownProperty,
                    coord,
                )
        }
    }

    private fun decodeLowResolutionV2(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
        version: Int,
    ) {
        while (buffer.isReadable(10)) {
            val index = buffer.g2()
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            val sizeX = buffer.g1() * 8
            val sizeZ = buffer.g1() * 8
            val level = buffer.g1s()
            var coordFine = CoordFine(0, 0, 0)
            var angle = 0
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                coordFine = CoordFine(coordFine.x + deltaX, coordFine.y + deltaY, coordFine.z + deltaZ)
            }
            coordFine =
                CoordFine(
                    (baseCoord.x shl 7) + coordFine.x,
                    coordFine.y,
                    (baseCoord.z shl 7) + coordFine.z,
                )
            val centerFineOffsetX = if (version >= 4) buffer.g2s() else null
            val centerFineOffsetZ = if (version >= 4) buffer.g2s() else null
            val worldEntity =
                WorldEntityV2(
                    index,
                    sizeX,
                    sizeZ,
                    coordFine,
                    angle,
                    level,
                    centerFineOffsetX,
                    centerFineOffsetZ,
                )
            this.worldEntity[index] = worldEntity
            this.updates[index] =
                WorldEntityUpdateType.LowResolutionToHighResolutionV2(
                    sizeX,
                    sizeZ,
                    angle,
                    coordFine,
                    level,
                    centerFineOffsetX,
                    centerFineOffsetZ,
                )
        }
    }

    private fun decodeLowResolutionV3(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ) {
        while (buffer.isReadable(10)) {
            val index = buffer.g2()
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            val sizeX = buffer.g1() * 8
            val sizeZ = buffer.g1() * 8
            val id = buffer.g2()
            var coordFine = CoordFine(0, 0, 0)
            var angle = 0
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                coordFine = CoordFine(coordFine.x + deltaX, coordFine.y + deltaY, coordFine.z + deltaZ)
            }
            coordFine =
                CoordFine(
                    (baseCoord.x shl 7) + coordFine.x,
                    coordFine.y,
                    (baseCoord.z shl 7) + coordFine.z,
                )
            val priority = buffer.g1()
            val worldEntity =
                WorldEntityV3(
                    index,
                    id,
                    sizeX,
                    sizeZ,
                    priority,
                    coordFine,
                    angle,
                )
            this.worldEntity[index] = worldEntity
            this.updates[index] =
                WorldEntityUpdateType.LowResolutionToHighResolutionV3(
                    id,
                    sizeX,
                    sizeZ,
                    angle,
                    priority,
                    coordFine,
                )
        }
    }

    private fun decodeLowResolutionV4(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ) {
        while (buffer.isReadable) {
            val index = buffer.g2()
            this.transmittedWorldEntity[this.transmittedWorldEntityCount++] = index
            val sizeX = buffer.g1() * 8
            val sizeZ = buffer.g1() * 8
            val id = buffer.g2()
            var coordFine = CoordFine(0, 0, 0)
            var angle = 0
            val bitpackedAngledCoordFineOpcodes = buffer.g1s()
            if (bitpackedAngledCoordFineOpcodes != 0) {
                val deltaX = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 0)
                val deltaY = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 2)
                val deltaZ = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 4)
                angle = decodeAngledCoordFineComponent(buffer, bitpackedAngledCoordFineOpcodes, 6)
                coordFine = CoordFine(coordFine.x + deltaX, coordFine.y + deltaY, coordFine.z + deltaZ)
            }
            coordFine =
                CoordFine(
                    (baseCoord.x shl 7) + coordFine.x,
                    coordFine.y,
                    (baseCoord.z shl 7) + coordFine.z,
                )
            val priority = buffer.g1()
            val worldEntity =
                WorldEntityV3(
                    index,
                    id,
                    sizeX,
                    sizeZ,
                    priority,
                    coordFine,
                    angle,
                )
            val extendedInfo = decodeWorldEntityInfoExtendedInfo(buffer)
            this.worldEntity[index] = worldEntity
            this.updates[index] =
                WorldEntityUpdateType.LowResolutionToHighResolutionV4(
                    id,
                    sizeX,
                    sizeZ,
                    angle,
                    priority,
                    coordFine,
                    extendedInfo,
                )
        }
    }

    @Suppress("unused")
    private class WorldEntityV1(
        val index: Int,
        val sizeX: Int,
        val sizeZ: Int,
        val unknownProperty: Int,
        var coordGrid: CoordGrid,
        var angle: Int,
        var moveSpeed: WorldEntityMoveSpeed,
    )

    @Suppress("unused")
    private class WorldEntityV2(
        val index: Int,
        val sizeX: Int,
        val sizeZ: Int,
        var coordFine: CoordFine,
        var angle: Int,
        val level: Int,
        val centerFineOffsetX: Int?,
        val centerFineOffsetZ: Int?,
    )

    @Suppress("unused")
    private class WorldEntityV3(
        val index: Int,
        val id: Int,
        val sizeX: Int,
        val sizeZ: Int,
        val priority: Int,
        var coordFine: CoordFine,
        var angle: Int,
    )
}
