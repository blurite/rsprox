package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3v951.buffer.readNativeString
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.readSpotanimRemovals

/** Revision-951 native mask order and getter selectors. */
internal object PlayerExtendedInfoDecoder {
    private val order =
        intArrayOf(
            5,
            2,
            25,
            20,
            22,
            10,
            13,
            1,
            26,
            27,
            3,
            23,
            11,
            16,
            8,
            19,
            14,
            15,
            17,
            4,
            0,
            21,
            24,
            6,
        )
    private val continuationBits = intArrayOf(7, 12, 18)
    private val knownBits = (order.toList() + continuationBits.toList()).fold(0) { mask, bit -> mask or (1 shl bit) }

    fun decode(
        buffer: JagByteBuf,
        definitions: Rs3AppearanceDefinitions? = null,
    ): List<PlayerExtendedInfo> {
        var mask = buffer.g1()
        for ((byte, bit) in continuationBits.withIndex()) {
            if (mask and (1 shl bit) == 0) break
            mask = mask or (buffer.g1() shl ((byte + 1) * 8))
        }
        require(mask and knownBits.inv() == 0) { "Unknown player mask bits: 0x${mask.toUInt().toString(16)}" }
        return buildList {
            for (bit in order) {
                if (mask and (1 shl bit) == 0) continue
                add(decodeMask(buffer, bit, definitions))
            }
        }
    }

    private fun decodeMask(
        buffer: JagByteBuf,
        bit: Int,
        definitions: Rs3AppearanceDefinitions?,
    ): PlayerExtendedInfo =
        when (bit) {
            27 -> {
                val removals = buffer.readSpotanimRemovals()
                val additions =
                    List(buffer.g1()) {
                        PlayerExtendedInfo.Spotanim(
                            buffer.g1Alt1(),
                            buffer.g2Alt3(),
                            buffer.g4Alt3(),
                            buffer.g1(),
                            buffer.g3(),
                        )
                    }
                PlayerExtendedInfo.Spotanims(removals, additions)
            }
            // Note(revision): the client reads/discards these fields; their bit numbers are revision-specific.
            8 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g1Alt2(), buffer.g1Alt2(), buffer.g2()))
            24 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt3(), buffer.g4Alt1(), buffer.g1Alt3()))
            3 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt2(), buffer.g4Alt3(), buffer.g1Alt3()))
            10 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt3(), buffer.g4Alt1(), buffer.g1Alt3()))
            14 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt1(), buffer.g4Alt3(), buffer.g1Alt2()))
            17 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt2(), buffer.g4Alt2(), buffer.g1Alt1()))
            19 ->
                PlayerExtendedInfo.UnusedFields(
                    bit,
                    listOf(buffer.g1Alt1(), buffer.g2Alt1(), buffer.g2Alt1(), buffer.g2Alt2()),
                )
            0 -> PlayerExtendedInfo.Sequence(List(4) { buffer.readNullableSmart() }, buffer.g1Alt2())
            20 -> PlayerExtendedInfo.ClanMember(buffer.g1() == 1)
            1 -> PlayerExtendedInfo.FaceEntity(buffer.g3Alt2())
            23 -> PlayerVariableMaskDecoder.decode(buffer, full = false)
            5 -> PlayerHitMaskDecoder.decode(buffer, wide = false)
            13 -> decodeHeadIcons(buffer)
            22 -> PlayerExtendedInfo.SayV2(buffer.readNativeString(), buffer.g1() and 1 != 0)
            6 ->
                PlayerExtendedInfo.ExactMove(
                    deltaX1 = buffer.g1().toByte().toInt(),
                    deltaZ1 = buffer.g1Alt2().toByte().toInt(),
                    deltaX2 = buffer.g1Alt2().toByte().toInt(),
                    deltaZ2 = buffer.g1Alt1().toByte().toInt(),
                    deltaLevel1 = buffer.g1Alt1().toByte().toInt(),
                    deltaLevel2 = buffer.g1Alt3().toByte().toInt(),
                    delay1 = buffer.g2Alt3(),
                    delay2 = buffer.g2(),
                    angle = buffer.g2Alt1(),
                )
            21 ->
                PlayerExtendedInfo.Tinting(
                    hue = buffer.g1Alt3(),
                    saturation = buffer.g1Alt3(),
                    lightness = buffer.g1(),
                    weight = buffer.g1Alt2(),
                    start = buffer.g2Alt3(),
                    end = buffer.g2(),
                )
            16 -> PlayerVariableMaskDecoder.decode(buffer, full = true)
            26 -> PlayerHitMaskDecoder.decode(buffer, wide = true)
            25 -> PlayerAttachmentMaskDecoder.decode(buffer)
            11 -> PlayerExtendedInfo.PlayerStatus(buffer.g1Alt2())
            15 -> PlayerExtendedInfo.SayV1(buffer.readNativeString())
            4 -> PlayerExtendedInfo.FaceAngle(buffer.g2())
            2 -> {
                val length = buffer.g1Alt3()
                require(length <= buffer.readableBytes()) { "Truncated player appearance: $length bytes required" }
                val payload = ByteArray(length)
                for (index in payload.indices) payload[index] = buffer.g1().toByte()
                if (definitions == null) {
                    PlayerExtendedInfo.UndecodedAppearance(payload)
                } else {
                    try {
                        PlayerAppearanceDecoder.decode(payload, definitions)
                    } catch (failure: Exception) {
                        // The independent, length-bounded envelope is consumed exactly. A profile failure
                        // must be visible, but does not invalidate subsequent movement/mask framing.
                        PlayerExtendedInfo.UndecodedAppearance(
                            payload,
                            "${failure.javaClass.simpleName}: ${failure.message}",
                        )
                    }
                }
            }
            else -> error("Unregistered player mask $bit")
        }

    private fun decodeHeadIcons(buffer: JagByteBuf): PlayerExtendedInfo.HeadIcons {
        val length = buffer.g1Alt2()
        require(length <= buffer.readableBytes()) { "Truncated player head-icon envelope" }
        // The native mask skips the embedded decoder entirely for a zero-length envelope.
        if (length == 0) return PlayerExtendedInfo.HeadIcons(update = false, slots = emptyList())
        val bytes = ByteArray(length)
        for (index in bytes.indices.reversed()) bytes[index] = buffer.g1().toByte()
        val storage = Unpooled.wrappedBuffer(bytes)
        try {
            val inner = storage.toJagByteBuf()
            val presence = inner.g1()
            val slots =
                buildList {
                    for (slot in 0..7) {
                        if (presence and (1 shl slot) == 0) continue
                        add(
                            PlayerExtendedInfo.HeadIcon(
                                slot,
                                inner.g1(),
                                inner.g2().let { if (it == 65535) -1 else it },
                                inner.readSmart(),
                                inner.readSmart(),
                                inner.readSmart(),
                                inner.readSmart(),
                            ),
                        )
                    }
                }
            // The native motion reader reads only the presence-selected records, not the entire blob.
            // Unused tail bytes are legal inside this already-consumed, length-bounded envelope.
            // Keep slot-read bounds and the enclosing PLAYER_INFO consumption check strict.
            return PlayerExtendedInfo.HeadIcons(update = true, slots = slots)
        } finally {
            storage.release()
        }
    }

    private fun JagByteBuf.readSmart(): Int =
        if (buffer.getByte(buffer.readerIndex()).toInt() < 0) g4() and Int.MAX_VALUE else g2()

    private fun JagByteBuf.readNullableSmart(): Int =
        if (buffer.getByte(buffer.readerIndex()).toInt() < 0) {
            g4() and Int.MAX_VALUE
        } else {
            g2().let { if (it == 32767) -1 else it }
        }
}
