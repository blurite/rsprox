package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordGrid

public class WorldEntityInfoClient {
    private var transmittedWorldEntityCount: Int = 0
    private val transmittedWorldEntity: IntArray = IntArray(2048)
    private val worldEntity: Array<WorldEntity?> = arrayOfNulls(2048)
    private val updates: MutableMap<Int, WorldEntityUpdateType> = mutableMapOf()

    public fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): WorldEntityInfoV3 {
        updates.clear()
        decodeHighResolution(buffer)
        decodeLowResolution(buffer, baseCoord)
        return WorldEntityInfoV3(updates.toMap())
    }

    private fun decodeHighResolution(buffer: JagByteBuf) {
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
            val worldEntity = checkNotNull(this.worldEntity[index])
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
                    WorldEntityUpdateType.Active(
                        angle,
                        worldEntity.coordGrid,
                        worldEntity.moveSpeed,
                    )
            }
        }
    }

    private fun decodeLowResolution(
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
                WorldEntity(
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
                WorldEntityUpdateType.LowResolutionToHighResolution(
                    sizeX,
                    sizeZ,
                    angle,
                    unknownProperty,
                    coord,
                )
        }
    }

    @Suppress("unused")
    private class WorldEntity(
        val index: Int,
        val sizeX: Int,
        val sizeZ: Int,
        val unknownProperty: Int,
        var coordGrid: CoordGrid,
        var angle: Int,
        var moveSpeed: WorldEntityMoveSpeed,
    )
}
