package net.rsprox.protocol.v234.game.outgoing.model.info.playerinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.AppearanceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.ChatExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.MoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.NameExtrasExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.TemporaryMoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.LowResolutionPosition
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FaceAngleExtendedInfo
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
internal class PlayerInfoClient(
    private val localIndex: Int,
    private val huffmanCodec: HuffmanCodec,
) : PlayerInfoDecoder {
    private var extendedInfoCount: Int = 0
    private val extendedInfoIndices: IntArray = IntArray(2048)
    private var highResolutionCount: Int = 0
    private val highResolutionIndices: IntArray = IntArray(2048)
    private var lowResolutionCount: Int = 0
    private val lowResolutionIndices: IntArray = IntArray(2048)
    private val unmodifiedFlags: ByteArray = ByteArray(2048)
    private val cachedPlayers: Array<Player?> = arrayOfNulls(2048)
    private val lowResolutionPositions: IntArray = IntArray(2048)
    private val updateTypes: Array<UpdateType> =
        Array(2048) {
            UpdateType.LOW_RESOLUTION_IDLE
        }

    override fun gpiInit(initBlock: PlayerInfoInitBlock) {
        val localPlayer = Player()
        cachedPlayers[localIndex] = localPlayer
        localPlayer.coord = initBlock.localPlayerCoord
        highResolutionCount = 0
        highResolutionIndices[highResolutionCount++] = localIndex
        unmodifiedFlags[localIndex] = 0
        lowResolutionCount = 0
        for (idx in 1..<2048) {
            if (idx == localIndex) continue
            val packed = initBlock.getLowResolutionPosition(idx).packed
            val level = packed shr 16
            val x = packed shr 8 and 597
            val z = packed and 597
            lowResolutionPositions[idx] = CoordGrid(level, x, z).packed
            lowResolutionIndices[lowResolutionCount++] = idx
            unmodifiedFlags[idx] = 0
        }
    }

    override fun reset() {
        for (i in cachedPlayers.indices) {
            cachedPlayers[i] = null
        }
        highResolutionCount = 0
        lowResolutionCount = 0
        unmodifiedFlags.fill(0)
        lowResolutionPositions.fill(0)
        lowResolutionIndices.fill(0)
    }

    override fun decode(buffer: ByteBuf): PlayerInfo {
        extendedInfoCount = 0
        updateTypes.fill(UpdateType.LOW_RESOLUTION_IDLE)
        for (player in cachedPlayers) {
            player?.extendedInfoBlocks = emptyList()
        }
        val updates = mutableMapOf<Int, PlayerUpdateType>()
        decodeBitCodes(buffer)
        for ((index, updateType) in updateTypes.withIndex()) {
            when (updateType) {
                UpdateType.LOW_RESOLUTION_IDLE -> {
                    // updates[index] = PlayerUpdateType.LowResolutionIdle
                    //  ^Ignore these as they are too spammy
                }
                UpdateType.HIGH_RESOLUTION_IDLE -> {
                    val player = checkNotNull(cachedPlayers[index])
                    updates[index] = PlayerUpdateType.HighResolutionIdle(player.extendedInfoBlocks)
                }
                UpdateType.LOW_RESOLUTION_TO_HIGH_RESOLUTION -> {
                    val player = checkNotNull(cachedPlayers[index])
                    updates[index] =
                        PlayerUpdateType.LowResolutionToHighResolution(
                            player.coord,
                            player.extendedInfoBlocks,
                        )
                }
                UpdateType.HIGH_RESOLUTION_MOVEMENT -> {
                    val player = checkNotNull(cachedPlayers[index])
                    updates[index] =
                        PlayerUpdateType.HighResolutionMovement(
                            player.coord,
                            player.extendedInfoBlocks,
                        )
                }
                UpdateType.LOW_RESOLUTION_MOVEMENT -> {
                    val coord = CoordGrid(this.lowResolutionPositions[index])
                    val lowResX = coord.x
                    val lowResZ = coord.z
                    val level = coord.level
                    updates[index] =
                        PlayerUpdateType.LowResolutionMovement(
                            LowResolutionPosition(
                                lowResX,
                                lowResZ,
                                level,
                            ),
                        )
                }
                UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION -> {
                    val coord = CoordGrid(this.lowResolutionPositions[index])
                    val lowResX = coord.x
                    val lowResZ = coord.z
                    val level = coord.level
                    updates[index] =
                        PlayerUpdateType.HighResolutionToLowResolution(
                            LowResolutionPosition(
                                lowResX,
                                lowResZ,
                                level,
                            ),
                        )
                }
            }
        }
        return PlayerInfo(updates)
    }

    private fun setUpdateType(
        idx: Int,
        updateType: UpdateType,
    ) {
        this.updateTypes[idx] = updateType
    }

    private fun decodeBitCodes(byteBuf: ByteBuf) {
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<highResolutionCount) {
                val idx = highResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE == 0) {
                    if (skipped > 0) {
                        --skipped
                        setUpdateType(idx, UpdateType.HIGH_RESOLUTION_IDLE)
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            setUpdateType(idx, UpdateType.HIGH_RESOLUTION_IDLE)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else {
                            getHighResolutionPlayerPosition(buffer, idx)
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<highResolutionCount) {
                val idx = highResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE != 0) {
                    if (skipped > 0) {
                        --skipped
                        setUpdateType(idx, UpdateType.HIGH_RESOLUTION_IDLE)
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            setUpdateType(idx, UpdateType.HIGH_RESOLUTION_IDLE)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else {
                            getHighResolutionPlayerPosition(buffer, idx)
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }

        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<lowResolutionCount) {
                val idx = lowResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE != 0) {
                    if (skipped > 0) {
                        --skipped
                        setUpdateType(idx, UpdateType.LOW_RESOLUTION_IDLE)
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            setUpdateType(idx, UpdateType.LOW_RESOLUTION_IDLE)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else if (getLowResolutionPlayerPosition(buffer, idx)) {
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        byteBuf.toBitBuf().use { buffer ->
            var skipped = 0
            for (i in 0..<lowResolutionCount) {
                val idx = lowResolutionIndices[i]
                if (unmodifiedFlags[idx].toInt() and CUR_CYCLE_INACTIVE == 0) {
                    if (skipped > 0) {
                        --skipped
                        setUpdateType(idx, UpdateType.LOW_RESOLUTION_IDLE)
                        unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                    } else {
                        val active = buffer.gBits(1)
                        if (active == 0) {
                            skipped = readStationary(buffer)
                            setUpdateType(idx, UpdateType.LOW_RESOLUTION_IDLE)
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        } else if (getLowResolutionPlayerPosition(buffer, idx)) {
                            unmodifiedFlags[idx] = (unmodifiedFlags[idx].toInt() or NEXT_CYCLE_INACTIVE).toByte()
                        }
                    }
                }
            }
            if (skipped != 0) {
                throw RuntimeException()
            }
        }
        lowResolutionCount = 0
        highResolutionCount = 0
        for (i in 1..<2048) {
            unmodifiedFlags[i] = (unmodifiedFlags[i].toInt() shr 1).toByte()
            val cachedPlayer = cachedPlayers[i]
            if (cachedPlayer != null) {
                highResolutionIndices[highResolutionCount++] = i
            } else {
                lowResolutionIndices[lowResolutionCount++] = i
            }
        }
        decodeExtendedInfo(byteBuf.toJagByteBuf())
    }

    private fun decodeExtendedInfo(buffer: JagByteBuf) {
        for (i in 0..<extendedInfoCount) {
            val index = extendedInfoIndices[i]
            val player = checkNotNull(cachedPlayers[index])
            var flag = buffer.g1()
            if (flag and EXTENDED_SHORT != 0) {
                flag += buffer.g1() shl 8
            }
            if (flag and EXTENDED_MEDIUM != 0) {
                flag += buffer.g1() shl 16
            }
            val blocks = mutableListOf<ExtendedInfo>()
            player.extendedInfoBlocks = blocks
            decodeExtendedInfoBlocks(buffer, flag, blocks)
        }
    }

    private fun decodeExtendedInfoBlocks(
        buffer: JagByteBuf,
        flags: Int,
        blocks: MutableList<ExtendedInfo>,
    ) {
        if (flags and MOVE_SPEED != 0) {
            decodeMoveSpeed(buffer, blocks)
        }
        if (flags and FACE_PATHINGENTITY != 0) {
            decodeFacePathingEntity(buffer, blocks)
        }
        if (flags and EXACT_MOVE != 0) {
            decodeExactMove(buffer, blocks)
        }
        if (flags and TINTING != 0) {
            decodeTinting(buffer, blocks)
        }
        if (flags and APPEARANCE != 0) {
            val len = buffer.g1Alt3()
            val data = ByteArray(len)
            buffer.gdata(data)
            decodeAppearance(Unpooled.wrappedBuffer(data).toJagByteBuf(), blocks)
        }
        if (flags and TEMP_MOVE_SPEED != 0) {
            decodeTemporaryMoveSpeed(buffer, blocks)
        }
        if (flags and CHAT_OLD != 0) {
            throw IllegalStateException("Old chat used!")
        }
        if (flags and SEQUENCE != 0) {
            decodeSequence(buffer, blocks)
        }
        if (flags and NAME_EXTRAS != 0) {
            decodeNameExtras(buffer, blocks)
        }
        if (flags and CHAT != 0) {
            decodeChat(buffer, blocks)
        }
        if (flags and HITS != 0) {
            decodeHit(buffer, blocks)
        }
        if (flags and SAY != 0) {
            decodeSay(buffer, blocks)
        }
        if (flags and SPOTANIM != 0) {
            decodeSpotanims(buffer, blocks)
        }
        if (flags and FACE_ANGLE != 0) {
            decodeFaceAngle(buffer, blocks)
        }
    }

    private fun decodeMoveSpeed(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        blocks += MoveSpeedExtendedInfo(buffer.g1sAlt1())
    }

    private fun decodeTemporaryMoveSpeed(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        blocks += TemporaryMoveSpeedExtendedInfo(buffer.g1sAlt2())
    }

    private fun decodeSequence(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val id = buffer.g2Alt3()
        val delay = buffer.g1Alt3()
        blocks += SequenceExtendedInfo(id, delay)
    }

    private fun decodeFacePathingEntity(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        var index = buffer.g2Alt1()
        index += buffer.g1Alt2() shl 16
        blocks += FacePathingEntityExtendedInfo(index)
    }

    private fun decodeFaceAngle(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        blocks += FaceAngleExtendedInfo(buffer.g2Alt1())
    }

    private fun decodeSay(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        blocks += SayExtendedInfo(buffer.gjstr())
    }

    private fun decodeNameExtras(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val beforeName = buffer.gjstr()
        val afterName = buffer.gjstr()
        val afterCombatLevel = buffer.gjstr()
        blocks += NameExtrasExtendedInfo(beforeName, afterName, afterCombatLevel)
    }

    private fun decodeChat(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val colourAndEffectsPacked = buffer.g2Alt1()
        val modIcon = buffer.g1Alt3()
        val autotyper = buffer.g1Alt2() == 1
        val huffmanLength = buffer.g1()
        val data = ByteArray(huffmanLength)
        buffer.gdataAlt1(data)
        val text = huffmanCodec.decode(Unpooled.wrappedBuffer(data))
        val colour = colourAndEffectsPacked ushr 8
        val effects = colourAndEffectsPacked and 0xFF
        val patternLength = if (colour in 13..20) colour - 12 else 0
        val pattern =
            if (patternLength in 1..8) {
                val array = ByteArray(patternLength)
                for (i in 0..<patternLength) {
                    array[i] = buffer.g1Alt1().toByte()
                }
                array
            } else {
                null
            }
        blocks +=
            ChatExtendedInfo(
                colour,
                effects,
                modIcon,
                autotyper,
                text,
                pattern,
            )
    }

    private fun decodeExactMove(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val deltaX1 = buffer.g1sAlt1()
        val deltaZ1 = buffer.g1sAlt1()
        val deltaX2 = buffer.g1sAlt2()
        val deltaZ2 = buffer.g1s()
        val delay1 = buffer.g2Alt1()
        val delay2 = buffer.g2Alt2()
        val direction = buffer.g2Alt1()
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

    private fun decodeSpotanims(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val spotanims = mutableMapOf<Int, Spotanim>()
        val count = buffer.g1()
        for (i in 0..<count) {
            val slot = buffer.g1Alt2()
            val id = buffer.g2()
            val heightAndDelay = buffer.g4()
            val height = heightAndDelay ushr 16
            val delay = heightAndDelay and 0xFFFF
            spotanims[slot] = Spotanim(id, delay, height)
        }
        blocks += SpotanimExtendedInfo(spotanims)
    }

    private fun decodeHit(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val hitCount = buffer.g1()
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

        val headbarCount = buffer.g1()
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
            val startFill = buffer.g1Alt1()
            val endFill =
                if (endTime > 0) {
                    buffer.g1Alt3()
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

    private fun decodeTinting(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val start = buffer.g2Alt1()
        val end = buffer.g2Alt2()
        val hue = buffer.g1sAlt3()
        val saturation = buffer.g1sAlt2()
        val lightness = buffer.g1s()
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

    private fun decodeAppearance(
        buffer: JagByteBuf,
        blocks: MutableList<ExtendedInfo>,
    ) {
        val gender = buffer.g1s()
        val skullIcon = buffer.g1s()
        val overheadIcon = buffer.g1s()
        val identKit = IntArray(12)
        var transformedNpcId: Int = -1
        for (i in 0..<12) {
            val flag = buffer.g1()
            if (flag == 0) {
                identKit[i] = 0
                continue
            }
            val extra = buffer.g1()
            identKit[i] = (flag shl 8) + extra
            if (i == 0 && identKit[i] == 65535) {
                transformedNpcId = buffer.g2()
                break
            }
        }
        val interfaceInentKit = IntArray(12)
        for (i in 0..<12) {
            val value = buffer.g1()
            if (value == 0) {
                interfaceInentKit[i] = 0
            } else {
                interfaceInentKit[i] = (value shl 8) + buffer.g1()
            }
        }
        val colours = IntArray(5)
        for (i in 0..<5) {
            colours[i] = buffer.g1()
        }
        val readyAnim = buffer.g2()
        val turnAnim = buffer.g2()
        val walkAnim = buffer.g2()
        val walkAnimBack = buffer.g2()
        val walkAnimLeft = buffer.g2()
        val walkAnimRight = buffer.g2()
        val runAnim = buffer.g2()
        val name = buffer.gjstr()
        val combatLevel = buffer.g1()
        val skillLevel = buffer.g2()
        val hidden = buffer.g1() == 1
        val customisationFlag = buffer.g2()
        val forceRefresh = customisationFlag shr 15 and 0x1 == 1
        val objTypeCustomisation: Array<ObjTypeCustomisation?>? =
            if (customisationFlag > 0 && customisationFlag != 32768) {
                val customisation = arrayOfNulls<ObjTypeCustomisation?>(12)
                for (i in 0..<12) {
                    val hasCustomisation = customisationFlag shr (12 - i) and 1
                    if (hasCustomisation == 1) {
                        var recolIndices: Int = -1
                        var recol1: Int = -1
                        var recol2: Int = -1
                        var retexIndices: Int = -1
                        var retex1: Int = -1
                        var retex2: Int = -1
                        var manWear: Int = -1
                        var womanWear: Int = -1
                        var manHead: Int = -1
                        var womanHead: Int = -1

                        val slotFlag = buffer.g1()
                        val recol = slotFlag and 0x1 != 0
                        val retex = slotFlag and 0x2 != 0
                        val wearModels = slotFlag and 0x4 != 0
                        val headModels = slotFlag and 0x8 != 0
                        if (recol) {
                            recolIndices = buffer.g1()
                            val recolIndex1 = recolIndices and 15
                            val recolIndex2 = recolIndices ushr 4 and 15
                            recol1 =
                                if (recolIndex1 != 15) {
                                    buffer.g2()
                                } else {
                                    -1
                                }
                            recol2 =
                                if (recolIndex2 != 15) {
                                    buffer.g2()
                                } else {
                                    -1
                                }
                        }

                        if (retex) {
                            retexIndices = buffer.g1()
                            val retexIndex1 = retexIndices and 15
                            val retexIndex2 = retexIndices ushr 4 and 15
                            retex1 =
                                if (retexIndex1 != 15) {
                                    buffer.g2()
                                } else {
                                    -1
                                }
                            retex2 =
                                if (retexIndex2 != 15) {
                                    buffer.g2()
                                } else {
                                    -1
                                }
                        }

                        if (wearModels) {
                            manWear = buffer.g2()
                            womanWear = buffer.g2()
                        }

                        if (headModels) {
                            manHead = buffer.g2()
                            womanHead = buffer.g2()
                        }
                        customisation[i] =
                            ObjTypeCustomisation(
                                recolIndices,
                                recol1,
                                recol2,
                                retexIndices,
                                retex1,
                                retex2,
                                manWear,
                                womanWear,
                                manHead,
                                womanHead,
                            )
                    }
                }
                customisation
            } else {
                null
            }
        val beforeName = buffer.gjstr()
        val afterName = buffer.gjstr()
        val afterCombatLevel = buffer.gjstr()
        val textGender = buffer.g1s()
        blocks +=
            AppearanceExtendedInfo(
                name,
                combatLevel,
                skillLevel,
                hidden,
                gender,
                textGender,
                skullIcon,
                overheadIcon,
                transformedNpcId,
                identKit,
                interfaceInentKit,
                colours,
                readyAnim,
                turnAnim,
                walkAnim,
                walkAnimBack,
                walkAnimLeft,
                walkAnimRight,
                runAnim,
                beforeName,
                afterName,
                afterCombatLevel,
                forceRefresh,
                objTypeCustomisation,
            )
    }

    private fun getHighResolutionPlayerPosition(
        buffer: BitBuf,
        idx: Int,
    ) {
        val extendedInfo = buffer.gBits(1) == 1
        if (extendedInfo) {
            extendedInfoIndices[extendedInfoCount++] = idx
        }
        val opcode = buffer.gBits(2)
        val cachedPlayer = checkNotNull(cachedPlayers[idx])
        if (opcode == 0) {
            if (extendedInfo) {
                cachedPlayer.queuedMove = false
                setUpdateType(idx, UpdateType.HIGH_RESOLUTION_IDLE)
            } else if (localIndex == idx) {
                throw RuntimeException()
            } else {
                lowResolutionPositions[idx] =
                    (cachedPlayer.coord.level shl 28)
                        .or(cachedPlayer.coord.z shr 13)
                        .or(cachedPlayer.coord.x shr 13 shl 14)
                cachedPlayers[idx] = null
                setUpdateType(idx, UpdateType.HIGH_RESOLUTION_TO_LOW_RESOLUTION)
                if (buffer.gBits(1) != 0) {
                    getLowResolutionPlayerPosition(buffer, idx)
                }
            }
        } else if (opcode == 1) {
            setUpdateType(idx, UpdateType.HIGH_RESOLUTION_MOVEMENT)
            val movementOpcode = buffer.gBits(3)
            var curX = cachedPlayer.coord.x
            var curZ = cachedPlayer.coord.z
            when (movementOpcode) {
                0 -> {
                    --curX
                    --curZ
                }
                1 -> {
                    --curZ
                }
                2 -> {
                    ++curX
                    --curZ
                }
                3 -> {
                    --curX
                }
                4 -> {
                    ++curX
                }
                5 -> {
                    --curX
                    ++curZ
                }
                6 -> {
                    ++curZ
                }
                7 -> {
                    ++curX
                    ++curZ
                }
            }
            cachedPlayer.coord = CoordGrid(cachedPlayer.coord.level, curX, curZ)
            cachedPlayer.queuedMove = extendedInfo
        } else if (opcode == 2) {
            setUpdateType(idx, UpdateType.HIGH_RESOLUTION_MOVEMENT)
            val movementOpcode = buffer.gBits(4)
            var curX = cachedPlayer.coord.x
            var curZ = cachedPlayer.coord.z
            when (movementOpcode) {
                0 -> {
                    curX -= 2
                    curZ -= 2
                }
                1 -> {
                    --curX
                    curZ -= 2
                }
                2 -> {
                    curZ -= 2
                }
                3 -> {
                    ++curX
                    curZ -= 2
                }
                4 -> {
                    curX += 2
                    curZ -= 2
                }
                5 -> {
                    curX -= 2
                    --curZ
                }
                6 -> {
                    curX += 2
                    --curZ
                }
                7 -> {
                    curX -= 2
                }
                8 -> {
                    curX += 2
                }
                9 -> {
                    curX -= 2
                    ++curZ
                }
                10 -> {
                    curX += 2
                    ++curZ
                }
                11 -> {
                    curX -= 2
                    curZ += 2
                }
                12 -> {
                    --curX
                    curZ += 2
                }
                13 -> {
                    curZ += 2
                }
                14 -> {
                    ++curX
                    curZ += 2
                }
                15 -> {
                    curX += 2
                    curZ += 2
                }
            }
            cachedPlayer.coord = CoordGrid(cachedPlayer.coord.level, curX, curZ)
            cachedPlayer.queuedMove = extendedInfo
        } else {
            setUpdateType(idx, UpdateType.HIGH_RESOLUTION_MOVEMENT)
            val far = buffer.gBits(1)
            if (far == 0) {
                val coord = buffer.gBits(12)
                val deltaLevel = coord shr 10
                var deltaX = coord shr 5 and 31
                if (deltaX > 15) {
                    deltaX -= 32
                }
                var deltaZ = coord and 31
                if (deltaZ > 15) {
                    deltaZ -= 32
                }
                var curLevel = cachedPlayer.coord.level
                var curX = cachedPlayer.coord.x
                var curZ = cachedPlayer.coord.z
                curX += deltaX
                curZ += deltaZ
                curLevel = (curLevel + deltaLevel) and 0x3
                cachedPlayer.coord = CoordGrid(curLevel, curX, curZ)
                cachedPlayer.queuedMove = extendedInfo
            } else {
                val coord = buffer.gBits(30)
                val deltaLevel = coord shr 28
                val deltaX = coord shr 14 and 16383
                val deltaZ = coord and 16383
                var curLevel = cachedPlayer.coord.level
                var curX = cachedPlayer.coord.x
                var curZ = cachedPlayer.coord.z
                curX = (curX + deltaX) and 16383
                curZ = (curZ + deltaZ) and 16383
                curLevel = (curLevel + deltaLevel) and 0x3
                cachedPlayer.coord = CoordGrid(curLevel, curX, curZ)
                cachedPlayer.queuedMove = extendedInfo
            }
        }
    }

    private fun getLowResolutionPlayerPosition(
        buffer: BitBuf,
        idx: Int,
    ): Boolean {
        val opcode = buffer.gBits(2)
        when (opcode) {
            0 -> {
                if (buffer.gBits(1) != 0) {
                    getLowResolutionPlayerPosition(buffer, idx)
                }
                val x = buffer.gBits(13)
                val z = buffer.gBits(13)
                val extendedInfo = buffer.gBits(1) == 1
                if (extendedInfo) {
                    this.extendedInfoIndices[extendedInfoCount++] = idx
                }
                if (cachedPlayers[idx] != null) {
                    throw RuntimeException()
                }
                val player = Player()
                cachedPlayers[idx] = player
                // cached appearance decoding
                val lowResolutionPosition = lowResolutionPositions[idx]
                val level = lowResolutionPosition shr 28
                val lowResX = lowResolutionPosition shr 14 and 0xFF
                val lowResZ = lowResolutionPosition and 0xFF
                player.coord = CoordGrid(level, (lowResX shl 13) + x, (lowResZ shl 13) + z)
                player.queuedMove = false
                setUpdateType(idx, UpdateType.LOW_RESOLUTION_TO_HIGH_RESOLUTION)
                return true
            }
            1 -> {
                val levelDelta = buffer.gBits(2)
                val lowResPosition = lowResolutionPositions[idx]
                lowResolutionPositions[idx] =
                    ((((lowResPosition shr 28) + levelDelta) and 3 shl 28))
                        .plus(lowResPosition and 268435455)
                setUpdateType(idx, UpdateType.LOW_RESOLUTION_MOVEMENT)
                return false
            }
            2 -> {
                setUpdateType(idx, UpdateType.LOW_RESOLUTION_MOVEMENT)
                val bitpacked = buffer.gBits(5)
                val levelDelta = bitpacked shr 3
                val movementCode = bitpacked and 7
                val lowResPosition = lowResolutionPositions[idx]
                val level = (lowResPosition shr 28) + levelDelta and 3
                var x = lowResPosition shr 14 and 255
                var z = lowResPosition and 255
                if (movementCode == 0) {
                    --x
                    --z
                }

                if (movementCode == 1) {
                    --z
                }

                if (movementCode == 2) {
                    ++x
                    --z
                }

                if (movementCode == 3) {
                    --x
                }

                if (movementCode == 4) {
                    ++x
                }

                if (movementCode == 5) {
                    --x
                    ++z
                }

                if (movementCode == 6) {
                    ++z
                }

                if (movementCode == 7) {
                    ++x
                    ++z
                }
                lowResolutionPositions[idx] = (x shl 14) + z + (level shl 28)
                return false
            }
            else -> {
                setUpdateType(idx, UpdateType.LOW_RESOLUTION_MOVEMENT)
                val bitpacked = buffer.gBits(18)
                val levelDelta = bitpacked shr 16
                val xDelta = bitpacked shr 8 and 255
                val zDelta = bitpacked and 255
                val lowResPosition = lowResolutionPositions[idx]
                val level = (lowResPosition shr 28) + levelDelta and 3
                val x = (xDelta + (lowResPosition shr 14)) and 255
                val z = (zDelta + lowResPosition) and 255
                lowResolutionPositions[idx] = (x shl 14) + z + (level shl 28)
                return false
            }
        }
    }

    private fun readStationary(buffer: BitBuf): Int {
        val type = buffer.gBits(2)
        return when (type) {
            0 -> 0
            1 -> buffer.gBits(5)
            2 -> buffer.gBits(8)
            else -> buffer.gBits(11)
        }
    }

    private companion object {
        private const val CUR_CYCLE_INACTIVE = 0x1
        private const val NEXT_CYCLE_INACTIVE = 0x2

        private const val EXTENDED_SHORT = 0x4
        private const val EXTENDED_MEDIUM = 0x200

        private const val CHAT_OLD = 0x1
        private const val FACE_PATHINGENTITY = 0x2
        private const val SAY = 0x8
        private const val APPEARANCE = 0x10
        private const val FACE_ANGLE = 0x20
        private const val HITS = 0x40
        private const val SEQUENCE = 0x80
        private const val NAME_EXTRAS = 0x100
        private const val TINTING = 0x400
        private const val TEMP_MOVE_SPEED = 0x800
        private const val CHAT = 0x1000
        private const val EXACT_MOVE = 0x2000
        private const val MOVE_SPEED = 0x4000
        private const val SPOTANIM = 0x10000

        private class Player {
            var queuedMove: Boolean = false
            var coord: CoordGrid = CoordGrid.INVALID
            var extendedInfoBlocks: List<ExtendedInfo> = emptyList()
        }

        private enum class UpdateType {
            LOW_RESOLUTION_IDLE,
            HIGH_RESOLUTION_IDLE,
            LOW_RESOLUTION_TO_HIGH_RESOLUTION,
            HIGH_RESOLUTION_MOVEMENT,
            LOW_RESOLUTION_MOVEMENT,
            HIGH_RESOLUTION_TO_LOW_RESOLUTION,
        }
    }
}
