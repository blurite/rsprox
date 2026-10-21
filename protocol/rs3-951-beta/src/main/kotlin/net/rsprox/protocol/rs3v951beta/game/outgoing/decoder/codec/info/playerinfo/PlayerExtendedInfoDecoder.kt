package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3v951beta.buffer.readNativeString
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.info.readSpotanimRemovals

/** Revision-951 beta native mask order and getter selectors. */
internal object PlayerExtendedInfoDecoder {
    private val order =
        intArrayOf(
            4,
            2,
            3,
            5,
            17,
            19,
            22,
            21,
            24,
            25,
            8,
            0,
            23,
            20,
            15,
            1,
            11,
            16,
            9,
            7,
            26,
            27,
            10,
            14,
        )
    private val continuationBits = intArrayOf(6, 12, 18)
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
            26 -> {
                val removals = buffer.readSpotanimRemovals()
                val additions =
                    List(buffer.g1()) {
                        PlayerExtendedInfo.Spotanim(
                            buffer.g1Alt2(),
                            buffer.g2Alt2(),
                            buffer.g4Alt3(),
                            buffer.g1Alt1(),
                            buffer.g3Alt3(),
                        )
                    }
                PlayerExtendedInfo.Spotanims(removals, additions)
            }
            // Note(revision): the client reads/discards these fields; their bit numbers are revision-specific.
            9 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g1Alt3(), buffer.g1Alt3(), buffer.g2Alt3()))
            24 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt3(), buffer.g4Alt3(), buffer.g1Alt2()))
            8 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt2(), buffer.g4Alt3(), buffer.g1Alt1()))
            22 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt3(), buffer.g4(), buffer.g1Alt3()))
            1 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt3(), buffer.g4Alt3(), buffer.g1()))
            11 -> PlayerExtendedInfo.UnusedFields(bit, listOf(buffer.g2Alt2(), buffer.g4Alt3(), buffer.g1Alt3()))
            17 ->
                PlayerExtendedInfo.UnusedFields(
                    bit,
                    listOf(buffer.g1Alt3(), buffer.g2Alt1(), buffer.g2(), buffer.g2()),
                )
            4 -> PlayerExtendedInfo.Sequence(List(4) { buffer.readNullableSmart() }, buffer.g1Alt1())
            16 -> PlayerExtendedInfo.ClanMember(buffer.g1Alt3() == 1)
            5 -> PlayerExtendedInfo.FaceEntity(buffer.g3Alt2())
            20 -> PlayerVariableMaskDecoder.decode(buffer, full = false)
            7 -> PlayerHitMaskDecoder.decode(buffer, wide = false)
            15 -> decodeHeadIcons(buffer)
            23 -> PlayerExtendedInfo.SayV2(buffer.readNativeString(), buffer.g1() and 1 != 0)
            2 ->
                PlayerExtendedInfo.ExactMove(
                    deltaX1 = buffer.g1().toByte().toInt(),
                    deltaZ1 = buffer.g1Alt3().toByte().toInt(),
                    deltaX2 = buffer.g1Alt3().toByte().toInt(),
                    deltaZ2 = buffer.g1Alt2().toByte().toInt(),
                    deltaLevel1 = buffer.g1Alt2().toByte().toInt(),
                    deltaLevel2 = buffer.g1Alt3().toByte().toInt(),
                    delay1 = buffer.g2(),
                    delay2 = buffer.g2Alt1(),
                    angle = buffer.g2Alt2(),
                )
            19 ->
                PlayerExtendedInfo.Tinting(
                    hue = buffer.g1Alt2(),
                    saturation = buffer.g1Alt2(),
                    lightness = buffer.g1Alt3(),
                    weight = buffer.g1(),
                    start = buffer.g2(),
                    end = buffer.g2Alt3(),
                )
            21 -> PlayerVariableMaskDecoder.decode(buffer, full = true)
            25 -> PlayerHitMaskDecoder.decode(buffer, wide = true)
            27 -> PlayerAttachmentMaskDecoder.decode(buffer)
            10 -> PlayerExtendedInfo.PlayerStatus(buffer.g1Alt1())
            14 -> PlayerExtendedInfo.SayV1(buffer.readNativeString())
            0 -> PlayerExtendedInfo.FaceAngle(buffer.g2Alt3())
            3 -> {
                val length = buffer.g1Alt3()
                require(length <= buffer.readableBytes()) { "Truncated player appearance: $length bytes required" }
                val payload = ByteArray(length)
                for (index in payload.indices) payload[index] = buffer.g1Alt1().toByte()
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
        val length = buffer.g1Alt1()
        require(length <= buffer.readableBytes()) { "Truncated player head-icon envelope" }
        // The native mask skips the embedded decoder entirely for a zero-length envelope.
        if (length == 0) return PlayerExtendedInfo.HeadIcons(update = false, slots = emptyList())
        val bytes = ByteArray(length)
        for (index in bytes.indices.reversed()) bytes[index] = buffer.g1Alt1().toByte()
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
