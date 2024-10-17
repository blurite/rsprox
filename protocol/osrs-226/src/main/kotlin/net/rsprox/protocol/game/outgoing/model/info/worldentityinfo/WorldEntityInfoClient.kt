package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordFine
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
                WorldEntityUpdateType.Active(
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

    private fun decodeLowResolution(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
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
            val worldEntity =
                WorldEntity(
                    index,
                    sizeX,
                    sizeZ,
                    coordFine,
                    angle,
                    level,
                )
            this.worldEntity[index] = worldEntity
            this.updates[index] =
                WorldEntityUpdateType.LowResolutionToHighResolution(
                    sizeX,
                    sizeZ,
                    angle,
                    coordFine,
                    level,
                )
        }
    }

    @Suppress("unused")
    private class WorldEntity(
        val index: Int,
        val sizeX: Int,
        val sizeZ: Int,
        var coordFine: CoordFine,
        var angle: Int,
        val level: Int,
    )
}
