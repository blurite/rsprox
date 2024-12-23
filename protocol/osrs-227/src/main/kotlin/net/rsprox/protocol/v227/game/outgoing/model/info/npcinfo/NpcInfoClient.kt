package net.rsprox.protocol.v227.game.outgoing.model.info.npcinfo

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.exceptions.DecodeError
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.MoveSpeed
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BaseAnimationSetExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BodyCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.CombatLevelChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.EnabledOpsExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.FaceCoordExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadIconCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.NameChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.TransformationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ModelCustomisation
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ResetCustomisation
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FacePathingEntityExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.Headbar
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.Hit
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.HitExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SayExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.Spotanim
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.TintingExtendedInfo

@Suppress("DuplicatedCode")
internal class NpcInfoClient(
    val cache: CacheProvider,
) : NpcInfoDecoder {
    private var deletedNPCCount: Int = 0
    private var deletedNPC = IntArray(1000)
    private var npc = arrayOfNulls<Npc>(65536)
    private var transmittedNPCCount = 0
    private var transmittedNPC = IntArray(65536)
    private var extraUpdateNPCCount: Int = 0
    private var extraUpdateNPC: IntArray = IntArray(250)
    private var cycle = 0

    private val updates: MutableMap<Int, UpdateType> = mutableMapOf()
    private val extendedInfoBlocks: MutableMap<Int, List<ExtendedInfo>> = mutableMapOf()

    override fun decode(
        buffer: ByteBuf,
        large: Boolean,
        baseCoord: CoordGrid,
    ): NpcInfo {
        deletedNPCCount = 0
        extraUpdateNPCCount = 0
        buffer.toBitBuf().use { bitBuffer ->
            processHighResolution(bitBuffer)
            processLowResolution(large, bitBuffer, baseCoord)
        }
        processExtendedInfo(buffer.toJagByteBuf())
        for (i in 0..<deletedNPCCount) {
            val index = deletedNPC[i]
            if (cycle != checkNotNull(npc[index]).lastTransmitCycle) {
                npc[index] = null
            }
        }
        if (buffer.isReadable) {
            throw IllegalStateException("npc info buffer still readable: ${buffer.readableBytes()}")
        }
        for (i in 0..<transmittedNPCCount) {
            if (npc[transmittedNPC[i]] == null) {
                throw IllegalStateException("Npc null at i $i")
            }
        }
        cycle++
        val result = mutableMapOf<Int, NpcUpdateType>()
        for ((index, update) in updates) {
            when (update) {
                UpdateType.IDLE -> {
                    // Too spammy, continue
                    continue
                }
                UpdateType.LOW_RESOLUTION_TO_HIGH_RESOLUTION -> {
                    val npc = checkNotNull(npc[index])
                    val extendedInfo = this.extendedInfoBlocks[index] ?: emptyList()
                    result[index] =
                        NpcUpdateType.LowResolutionToHighResolution(
                            npc.id,
                            npc.spawnCycle,
                            npc.coord.x,
                            npc.coord.z,
                            npc.coord.level,
                            npc.angle,
                            npc.jump,
                            extendedInfo,
                        )
                }
                UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION -> {
                    result[index] = NpcUpdateType.HighResolutionToLowResolution
                }
                UpdateType.ACTIVE -> {
                    val npc = checkNotNull(npc[index])
                    val extendedInfo = this.extendedInfoBlocks[index] ?: emptyList()
                    result[index] =
                        NpcUpdateType.Active(
                            npc.coord.x,
                            npc.coord.z,
                            npc.coord.level,
                            npc.steps,
                            npc.moveSpeed,
                            extendedInfo,
                            npc.jump,
                        )
                }
            }
        }
        this.updates.clear()
        this.extendedInfoBlocks.clear()
        return NpcInfo(result)
    }

    private fun processExtendedInfo(buffer: JagByteBuf) {
        for (i in 0..<extraUpdateNPCCount) {
            val index = extraUpdateNPC[i]
            val npc = checkNotNull(npc[index])
            var flag = buffer.g1()
            if ((flag and EXTENDED_SHORT) != 0) {
                flag += buffer.g1() shl 8
            }
            if ((flag and EXTENDED_MEDIUM) != 0) {
                flag += buffer.g1() shl 16
            }
            val blocks = mutableListOf<ExtendedInfo>()

            this.extendedInfoBlocks[index] = blocks

            if (flag and OLD_SPOTANIM_UNUSED != 0) {
                throw IllegalStateException("Old spotanim used!")
            }
            if (flag and HEADICON_CUSTOMISATION != 0) {
                decodeHeadiconCustomisation(buffer, blocks)
            }
            if (flag and TRANSFORMATION != 0) {
                decodeTransformation(buffer, blocks, npc)
            }
            if (flag and FACE_COORD != 0) {
                decodeFaceCoord(buffer, blocks)
            }
            if (flag and FACE_PATHINGENTITY != 0) {
                decodeFacePathingEntity(buffer, blocks)
            }
            if (flag and LEVEL_CHANGE != 0) {
                decodeCombatLevelChange(buffer, blocks)
            }
            if (flag and NAME_CHANGE != 0) {
                decodeNameChange(buffer, blocks)
            }
            if (flag and EXACT_MOVE != 0) {
                decodeExactMove(buffer, blocks)
            }
            if (flag and HEAD_CUSTOMISATION != 0) {
                decodeHeadCustomisation(npc.id, buffer, blocks)
            }
            if (flag and BODY_CUSTOMISATION != 0) {
                decodeBodyCustomisation(npc.id, buffer, blocks)
            }
            if (flag and TINTING != 0) {
                decodeTinting(buffer, blocks)
            }
            if (flag and SPOTANIM != 0) {
                decodeSpotanim(buffer, blocks)
            }
            if (flag and SAY != 0) {
                decodeSay(buffer, blocks)
            }
            if (flag and OPS != 0) {
                decodeEnabledOps(buffer, blocks)
            }
            if (flag and SEQUENCE != 0) {
                decodeSequence(buffer, blocks)
            }
            if (flag and BAS_CHANGE != 0) {
                decodeBaseAnimationSet(buffer, blocks)
            }
            if (flag and HITS != 0) {
                decodeHits(buffer, blocks)
            }
        }
    }

    private fun decodeBaseAnimationSet(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val flag = buffer.g4Alt1()
        val turnLeftAnim = if (flag and 0x1 != 0) buffer.g2Alt2() else null
        val turnRightAnim = if (flag and 0x2 != 0) buffer.g2() else null
        val walkAnim = if (flag and 0x4 != 0) buffer.g2() else null
        val walkAnimBack = if (flag and 0x8 != 0) buffer.g2Alt3() else null
        val walkAnimLeft = if (flag and 0x10 != 0) buffer.g2Alt2() else null
        val walkAnimRight = if (flag and 0x20 != 0) buffer.g2Alt3() else null
        val runAnim = if (flag and 0x40 != 0) buffer.g2Alt2() else null
        val runAnimBack = if (flag and 0x80 != 0) buffer.g2Alt2() else null
        val runAnimLeft = if (flag and 0x100 != 0) buffer.g2Alt3() else null
        val runAnimRight = if (flag and 0x200 != 0) buffer.g2() else null
        val crawlAnim = if (flag and 0x400 != 0) buffer.g2Alt2() else null
        val crawlAnimBack = if (flag and 0x800 != 0) buffer.g2() else null
        val crawlAnimLeft = if (flag and 0x1000 != 0) buffer.g2Alt3() else null
        val crawlAnimRight = if (flag and 0x2000 != 0) buffer.g2Alt3() else null
        val readyAnim = if (flag and 0x4000 != 0) buffer.g2Alt1() else null
        blocks +=
            BaseAnimationSetExtendedInfo(
                turnLeftAnim,
                turnRightAnim,
                walkAnim,
                walkAnimBack,
                walkAnimLeft,
                walkAnimRight,
                runAnim,
                runAnimBack,
                runAnimLeft,
                runAnimRight,
                crawlAnim,
                crawlAnimBack,
                crawlAnimLeft,
                crawlAnimRight,
                readyAnim,
            )
    }

    private fun decodeHits(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val hitCount = buffer.g1Alt1()
        val hits = ArrayList<Hit>(hitCount)
        for (i in 0..<hitCount) {
            when (val type = buffer.gSmart1or2()) {
                0x7FFE -> {
                    val delay = buffer.gSmart1or2()
                    hits +=
                        Hit(
                            0x7FFE,
                            -1,
                            -1,
                            -1,
                            delay,
                        )
                }
                0x7FFF -> {
                    val mainType = buffer.gSmart1or2()
                    val value = buffer.gSmart1or2()
                    val soakType = buffer.gSmart1or2()
                    val soakValue = buffer.gSmart1or2()
                    val delay = buffer.gSmart1or2()
                    hits +=
                        Hit(
                            mainType,
                            value,
                            soakType,
                            soakValue,
                            delay,
                        )
                }
                else -> {
                    val value = buffer.gSmart1or2()
                    val delay = buffer.gSmart1or2()
                    hits +=
                        Hit(
                            type,
                            value,
                            -1,
                            -1,
                            delay,
                        )
                }
            }
        }

        val headbarCount = buffer.g1Alt1()
        val headbars = ArrayList<Headbar>(headbarCount)
        for (i in 0..<headbarCount) {
            val type = buffer.gSmart1or2()
            val endTime = buffer.gSmart1or2()
            if (endTime == 0x7FFF) {
                headbars +=
                    Headbar(
                        type,
                        -1,
                        -1,
                        -1,
                        -1,
                    )
                continue
            }
            val startTime = buffer.gSmart1or2()
            val startFill = buffer.g1Alt2()
            val endFill =
                if (endTime > 0) {
                    buffer.g1()
                } else {
                    startFill
                }
            headbars +=
                Headbar(
                    type,
                    startFill,
                    endFill,
                    startTime,
                    endTime,
                )
        }
        blocks += HitExtendedInfo(hits, headbars)
    }

    private fun decodeSpotanim(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val spotanims = mutableMapOf<Int, Spotanim>()
        val count = buffer.g1Alt2()
        for (i in 0..<count) {
            val slot = buffer.g1Alt2()
            val id = buffer.g2Alt1()
            val heightAndDelay = buffer.g4Alt3()
            val height = heightAndDelay ushr 16
            val delay = heightAndDelay and 0xFFFF
            spotanims[slot] = Spotanim(id, delay, height)
        }
        blocks += SpotanimExtendedInfo(spotanims)
    }

    private fun decodeSequence(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val id = buffer.g2Alt3()
        val delay = buffer.g1()
        blocks += SequenceExtendedInfo(id, delay)
    }

    private fun decodeCombatLevelChange(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val level = buffer.g4Alt1()
        blocks += CombatLevelChangeExtendedInfo(level)
    }

    private fun decodeTinting(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val start = buffer.g2()
        val end = buffer.g2Alt3()
        val hue = buffer.g1Alt1()
        val saturation = buffer.g1Alt3()
        val lightness = buffer.g1Alt3()
        val weight = buffer.g1Alt2()
        blocks +=
            TintingExtendedInfo(
                start,
                end,
                hue,
                saturation,
                lightness,
                weight,
            )
    }

    private fun decodeTransformation(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
        npc: Npc,
    ) {
        val id = buffer.g2Alt1()
        blocks += TransformationExtendedInfo(id)
        npc.id = id
    }

    private fun decodeEnabledOps(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val flag = buffer.g1Alt2()
        blocks += EnabledOpsExtendedInfo(flag)
    }

    private fun decodeFacePathingEntity(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        var index = buffer.g2Alt3()
        index += buffer.g1Alt2() shl 16
        blocks += FacePathingEntityExtendedInfo(index)
    }

    private fun decodeBodyCustomisation(
        id: Int,
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val flag = buffer.g1Alt3()
        if (flag and 0x1 != 0) {
            blocks += BodyCustomisationExtendedInfo(ResetCustomisation)
            return
        }
        val models =
            if (flag and 0x2 != 0) {
                val count = buffer.g1()
                val models = ArrayList<Int>(count)
                for (i in 0..<count) {
                    val modelId = buffer.g2Alt2()
                    models += if (modelId == 0xFFFF) -1 else modelId
                }
                models
            } else {
                null
            }
        val recolours =
            if (flag and 0x4 != 0) {
                val cache = cache.get()
                val npc =
                    cache.getNpcType(id)
                        ?: throw DecodeError("Npc $id not found in cache $cache! Npc info decoding cannot continue.")
                val length = npc.recoldest.size
                val recolours = ArrayList<Int>(length)
                for (i in 0..<length) {
                    recolours += buffer.g2Alt2()
                }
                recolours
            } else {
                null
            }
        val retextures =
            if (flag and 0x8 != 0) {
                val cache = cache.get()
                val npc =
                    cache.getNpcType(id)
                        ?: throw DecodeError("Npc $id not found in cache $cache! Npc info decoding cannot continue.")
                val length = npc.retexdest.size
                val retextures = ArrayList<Int>(length)
                for (i in 0..<length) {
                    retextures += buffer.g2Alt3()
                }
                retextures
            } else {
                null
            }
        val mirror =
            if (flag and 0x10 != 0) {
                buffer.g1Alt2() == 1
            } else {
                null
            }
        blocks +=
            BodyCustomisationExtendedInfo(
                ModelCustomisation(
                    models,
                    recolours,
                    retextures,
                    mirror,
                ),
            )
    }

    private fun decodeHeadCustomisation(
        id: Int,
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val flag = buffer.g1Alt1()
        if (flag and 0x1 != 0) {
            blocks += BodyCustomisationExtendedInfo(ResetCustomisation)
            return
        }
        val models =
            if (flag and 0x2 != 0) {
                val count = buffer.g1()
                val models = ArrayList<Int>(count)
                for (i in 0..<count) {
                    val modelId = buffer.g2()
                    models += if (modelId == 0xFFFF) -1 else modelId
                }
                models
            } else {
                null
            }
        val recolours =
            if (flag and 0x4 != 0) {
                val cache = cache.get()
                val npc =
                    cache.getNpcType(id)
                        ?: throw DecodeError("Npc $id not found in cache $cache! Npc info decoding cannot continue.")
                val length = npc.recoldest.size
                val recolours = ArrayList<Int>(length)
                for (i in 0..<length) {
                    recolours += buffer.g2()
                }
                recolours
            } else {
                null
            }
        val retextures =
            if (flag and 0x8 != 0) {
                val cache = cache.get()
                val npc =
                    cache.getNpcType(id)
                        ?: throw DecodeError("Npc $id not found in cache $cache! Npc info decoding cannot continue.")
                val length = npc.retexdest.size
                val retextures = ArrayList<Int>(length)
                for (i in 0..<length) {
                    retextures += buffer.g2Alt2()
                }
                retextures
            } else {
                null
            }
        val mirror =
            if (flag and 0x10 != 0) {
                buffer.g1Alt2() == 1
            } else {
                null
            }
        blocks +=
            HeadCustomisationExtendedInfo(
                ModelCustomisation(
                    models,
                    recolours,
                    retextures,
                    mirror,
                ),
            )
    }

    private fun decodeSay(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val text = buffer.gjstr()
        blocks += SayExtendedInfo(text)
    }

    private fun decodeExactMove(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val deltaX1 = buffer.g1sAlt3()
        val deltaZ1 = buffer.g1s()
        val deltaX2 = buffer.g1sAlt1()
        val deltaZ2 = buffer.g1sAlt3()
        val delay1 = buffer.g2Alt2()
        val delay2 = buffer.g2()
        val direction = buffer.g2Alt3()
        blocks +=
            ExactMoveExtendedInfo(
                deltaX1,
                deltaZ1,
                delay1,
                deltaX2,
                deltaZ2,
                delay2,
                direction,
            )
    }

    private fun decodeNameChange(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val text = buffer.gjstr()
        blocks += NameChangeExtendedInfo(text)
    }

    private fun decodeHeadiconCustomisation(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val flag = buffer.g1Alt2()
        val groups = IntArray(8)
        val indices = IntArray(8)
        for (i in 0..<8) {
            if (flag and (1 shl i) != 0) {
                groups[i] = buffer.gSmart2or4null()
                indices[i] = buffer.gSmart1or2null()
            } else {
                groups[i] = -1
                indices[i] = -1
            }
        }
        blocks += HeadIconCustomisationExtendedInfo(groups, indices)
    }

    private fun decodeFaceCoord(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val x = buffer.g2Alt1()
        val z = buffer.g2Alt2()
        val instant = buffer.g1Alt2() == 1
        blocks += FaceCoordExtendedInfo(x, z, instant)
    }

    private fun processHighResolution(buffer: BitBuf) {
        val count = buffer.gBits(8)
        if (count < transmittedNPCCount) {
            for (i in count..<transmittedNPCCount) {
                deletedNPC[deletedNPCCount++] = transmittedNPC[i]
                updates[transmittedNPC[i]] = UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION
            }
        }
        require(count <= transmittedNPCCount) {
            "Too many npcs to process: $count, $transmittedNPCCount"
        }
        transmittedNPCCount = 0
        for (i in 0..<count) {
            val index = transmittedNPC[i]
            val npc = requireNotNull(npc[index])
            npc.steps.clear()
            val hasUpdate = buffer.gBits(1)
            if (hasUpdate == 0) {
                transmittedNPC[transmittedNPCCount++] = index
                npc.lastTransmitCycle = cycle
                updates[index] = UpdateType.IDLE
                continue
            }
            val updateType = buffer.gBits(2)
            when (updateType) {
                0 -> {
                    transmittedNPC[transmittedNPCCount++] = index
                    npc.lastTransmitCycle = cycle
                    extraUpdateNPC[extraUpdateNPCCount++] = index
                    updates[index] = UpdateType.ACTIVE
                }
                1 -> {
                    transmittedNPC[transmittedNPCCount++] = index
                    npc.lastTransmitCycle = cycle
                    val walkDirection = buffer.gBits(3)
                    npc.addRouteWaypointAdjacent(
                        walkDirection,
                        MoveSpeed.WALK,
                    )
                    val extendedInfo = buffer.gBits(1)
                    if (extendedInfo == 1) {
                        this.extraUpdateNPC[extraUpdateNPCCount++] = index
                    }
                    updates[index] = UpdateType.ACTIVE
                }
                2 -> {
                    transmittedNPC[transmittedNPCCount++] = index
                    npc.lastTransmitCycle = cycle
                    if (buffer.gBits(1) == 1) {
                        val walkDirection = buffer.gBits(3)
                        npc.addRouteWaypointAdjacent(
                            walkDirection,
                            MoveSpeed.RUN,
                        )
                        val runDirection = buffer.gBits(3)
                        npc.addRouteWaypointAdjacent(
                            runDirection,
                            MoveSpeed.RUN,
                        )
                    } else {
                        val crawlDirection = buffer.gBits(3)
                        npc.addRouteWaypointAdjacent(
                            crawlDirection,
                            MoveSpeed.CRAWL,
                        )
                    }
                    val extendedInfo = buffer.gBits(1)
                    if (extendedInfo == 1) {
                        this.extraUpdateNPC[extraUpdateNPCCount++] = index
                    }
                    updates[index] = UpdateType.ACTIVE
                }
                3 -> {
                    deletedNPC[deletedNPCCount++] = index
                    updates[index] = UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION
                }
            }
        }
    }

    private fun processLowResolution(
        large: Boolean,
        buffer: BitBuf,
        baseCoord: CoordGrid,
    ) {
        while (true) {
            val indexBitCount = 16
            val capacity = (1 shl indexBitCount)
            if (buffer.readableBits() >= indexBitCount + 12) {
                val index = buffer.gBits(indexBitCount)
                if (capacity - 1 != index) {
                    var isNew = false
                    if (npc[index] == null) {
                        npc[index] = Npc(-1, CoordGrid.INVALID)
                        isNew = true
                    }
                    val existingType = updates[index]
                    if (existingType == UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION) {
                        // Teleport
                        updates[index] = UpdateType.ACTIVE
                    } else {
                        updates[index] = UpdateType.LOW_RESOLUTION_TO_HIGH_RESOLUTION
                    }
                    val npc = checkNotNull(npc[index])
                    transmittedNPC[transmittedNPCCount++] = index
                    npc.lastTransmitCycle = cycle

                    val deltaX = decodeDelta(large, buffer)
                    val jump = buffer.gBits(1)
                    val deltaZ = decodeDelta(large, buffer)
                    npc.id = buffer.gBits(14)
                    val hasSpawnCycle = buffer.gBits(1) == 1
                    if (hasSpawnCycle) {
                        npc.spawnCycle = buffer.gBits(32)
                    }
                    val extendedInfo = buffer.gBits(1)
                    if (extendedInfo == 1) {
                        this.extraUpdateNPC[extraUpdateNPCCount++] = index
                    }
                    val angle = NPC_TURN_ANGLES[buffer.gBits(3)]
                    if (isNew) {
                        npc.turnAngle = angle
                        npc.angle = angle
                    }
                    // reset bas
                    if (npc.turnSpeed == 0) {
                        npc.angle = 0
                    }
                    npc.addRouteWaypoint(
                        baseCoord,
                        deltaX,
                        deltaZ,
                        jump == 1,
                    )
                    continue
                }
            }
            return
        }
    }

    private fun decodeDelta(
        large: Boolean,
        buffer: BitBuf,
    ): Int =
        if (large) {
            var delta = buffer.gBits(8)
            if (delta > 127) {
                delta -= 256
            }
            delta
        } else {
            var delta = buffer.gBits(6)
            if (delta > 31) {
                delta -= 64
            }
            delta
        }

    private class Npc(
        var id: Int,
        var coord: CoordGrid,
    ) {
        var lastTransmitCycle: Int = 0
        var moveSpeed: MoveSpeed = MoveSpeed.STATIONARY
        var turnAngle = 0
        var angle = 0
        var spawnCycle = 0
        var turnSpeed = 32
        var jump: Boolean = false
        var steps: MutableList<Int> = mutableListOf()

        fun addRouteWaypoint(
            baseCoord: CoordGrid,
            relativeX: Int,
            relativeZ: Int,
            jump: Boolean,
        ) {
            coord = CoordGrid(baseCoord.level, baseCoord.x + relativeX, baseCoord.z + relativeZ)
            moveSpeed = MoveSpeed.STATIONARY
            this.jump = jump
        }

        fun addRouteWaypointAdjacent(
            opcode: Int,
            speed: MoveSpeed,
        ) {
            steps += opcode
            var x = coord.x
            var z = coord.z
            if (opcode == 0) {
                --x
                ++z
            }

            if (opcode == 1) {
                ++z
            }

            if (opcode == 2) {
                ++x
                ++z
            }

            if (opcode == 3) {
                --x
            }

            if (opcode == 4) {
                ++x
            }

            if (opcode == 5) {
                --x
                --z
            }

            if (opcode == 6) {
                --z
            }

            if (opcode == 7) {
                ++x
                --z
            }

            coord = CoordGrid(coord.level, x, z)
            moveSpeed = speed
        }
    }

    private companion object {
        private val NPC_TURN_ANGLES = intArrayOf(768, 1024, 1280, 512, 1536, 256, 0, 1792)
        private const val SEQUENCE: Int = 0x1
        private const val EXTENDED_SHORT: Int = 0x2
        private const val HITS: Int = 0x4
        private const val OLD_SPOTANIM_UNUSED: Int = 0x8
        private const val FACE_COORD: Int = 0x10
        private const val FACE_PATHINGENTITY: Int = 0x20
        private const val SAY: Int = 0x40
        private const val TRANSFORMATION: Int = 0x80
        private const val EXTENDED_MEDIUM: Int = 0x100
        private const val BODY_CUSTOMISATION: Int = 0x200
        private const val TINTING: Int = 0x400
        private const val LEVEL_CHANGE: Int = 0x800
        private const val HEAD_CUSTOMISATION: Int = 0x1000
        private const val NAME_CHANGE: Int = 0x2000
        private const val EXACT_MOVE: Int = 0x4000
        private const val OPS: Int = 0x8000
        private const val SPOTANIM: Int = 0x10000
        private const val BAS_CHANGE: Int = 0x20000
        private const val HEADICON_CUSTOMISATION: Int = 0x40000

        private enum class UpdateType {
            IDLE,
            LOW_RESOLUTION_TO_HIGH_RESOLUTION,
            HIGH_RESOLUTION_TO_LOW_RESOLUTION,
            ACTIVE,
        }
    }
}
