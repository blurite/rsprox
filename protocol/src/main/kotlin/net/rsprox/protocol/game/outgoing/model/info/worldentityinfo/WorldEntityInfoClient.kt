package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordFine
import net.rsprox.protocol.common.CoordGrid

public class WorldEntityInfoClient {
    private var transmittedWorldEntityCount: Int = 0
    private val transmittedWorldEntity: IntArray = IntArray(2048)
    private val worldEntity: Array<Any?> = arrayOfNulls(2048)
    private val updates: MutableMap<Int, WorldEntityUpdateType> = mutableMapOf()

    public fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
        version: Int,
    ): WorldEntityInfo {
        updates.clear()
        if (version >= 3) {
            decodeHighResolutionV2(buffer)
            decodeLowResolutionV2(buffer, baseCoord, version)
        } else {
            decodeHighResolutionV1(buffer)
            decodeLowResolutionV1(buffer, baseCoord)
        }
        return when (version) {
            1 -> WorldEntityInfoV1(updates.toMap())
            2 -> WorldEntityInfoV2(updates.toMap())
            3 -> WorldEntityInfoV3(updates.toMap())
            4 -> WorldEntityInfoV4(updates.toMap())
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
            worldEntity.moveSpeed = WorldEntityMoveSpeed[moveSpeed]
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
            val sizeX = buffer.g1()
            val sizeZ = buffer.g1()
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
            val sizeX = buffer.g1()
            val sizeZ = buffer.g1()
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
}
