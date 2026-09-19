package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo.Unused.Kind
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.readSpotanimRemovals

/** Mask order and read selectors are taken from the revision-950 native mask proof. */
internal object PlayerExtendedInfoDecoder {
    private val order = intArrayOf(
        26, 8, 20, 3, 23, 7, 12, 24, 19, 6, 11, 22, 5, 0, 17, 21, 16, 25, 27, 9, 10, 1, 2, 13,
    )
    private val continuationBits = intArrayOf(4, 15, 18)
    private val knownBits = (order.toList() + continuationBits.toList()).fold(0) { mask, bit -> mask or (1 shl bit) }

    fun decode(buffer: JagByteBuf, definitions: Rs3AppearanceDefinitions? = null): List<PlayerExtendedInfo> {
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
    ): PlayerExtendedInfo = when (bit) {
        26 -> {
            val removals = buffer.readSpotanimRemovals()
            val additions = List(buffer.g1()) {
                PlayerExtendedInfo.Spotanim(
                    buffer.g1Alt3(), buffer.g2Alt3(), buffer.g4Alt1(), buffer.g1(), buffer.g3Alt1(),
                )
            }
            PlayerExtendedInfo.Spotanims(removals, additions)
        }
        // Note: revision 950 reads and discards these six masks; do not infer effects from old names.
        8 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_8, buffer.g1Alt3(), buffer.g1Alt1(), buffer.g2())
        20 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_20, buffer.g2Alt2(), buffer.g4Alt2(), buffer.g1Alt1())
        3 -> PlayerExtendedInfo.Sequence(List(4) { buffer.readNullableSmart() }, buffer.g1())
        23 -> PlayerExtendedInfo.ClanMember(buffer.g1Alt3() == 1)
        7 -> PlayerExtendedInfo.FaceEntity(buffer.g3Alt1())
        12 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_12, buffer.g2Alt2(), buffer.g4(), buffer.g1())
        24 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_24, buffer.g2Alt1(), buffer.g4Alt1(), buffer.g1Alt1())
        19 -> PlayerVariableMaskDecoder.decode(buffer, full = false)
        6 -> PlayerHitMaskDecoder.decode(buffer, wide = false)
        11 -> decodeHeadIcons(buffer)
        22 -> PlayerExtendedInfo.SayV2(buffer.readNativeString(), buffer.g1() and 1 != 0)
        0 -> PlayerExtendedInfo.ExactMove(
            deltaX1 = buffer.g1Alt3().toByte().toInt(),
            deltaZ1 = buffer.g1().toByte().toInt(),
            deltaX2 = buffer.g1Alt2().toByte().toInt(),
            deltaZ2 = buffer.g1Alt1().toByte().toInt(),
            deltaLevel1 = buffer.g1Alt2().toByte().toInt(),
            deltaLevel2 = buffer.g1Alt3().toByte().toInt(),
            delay1 = buffer.g2Alt1(),
            delay2 = buffer.g2(),
            angle = buffer.g2Alt1(),
        )
        21 -> PlayerExtendedInfo.Tinting(
            hue = buffer.g1(),
            saturation = buffer.g1Alt3(),
            lightness = buffer.g1(),
            weight = buffer.g1Alt3(),
            start = buffer.g2Alt3(),
            end = buffer.g2(),
        )
        // Note: revision 950 discards all four values; recheck native usage when updating revisions.
        16 -> PlayerExtendedInfo.UnusedMask16(buffer.g1(), buffer.g2Alt3(), buffer.g2Alt3(), buffer.g2Alt3())
        17 -> PlayerVariableMaskDecoder.decode(buffer, full = true)
        25 -> PlayerHitMaskDecoder.decode(buffer, wide = true)
        27 -> PlayerAttachmentMaskDecoder.decode(buffer)
        9 -> PlayerExtendedInfo.PlayerStatus(buffer.g1Alt2())
        10 -> PlayerExtendedInfo.SayV1(buffer.readNativeString())
        1 -> PlayerExtendedInfo.FaceAngle(buffer.g2())
        2 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_2, buffer.g2Alt1(), buffer.g4Alt3(), buffer.g1())
        13 -> PlayerExtendedInfo.Unused(Kind.UNUSED_MASK_13, buffer.g2Alt2(), buffer.g4Alt2(), buffer.g1Alt2())
        5 -> {
            val length = buffer.g1Alt1()
            require(length <= buffer.readableBytes()) { "Truncated player appearance: $length bytes required" }
            val payload = ByteArray(length) { (buffer.g1() - 128).toByte() }
            if (definitions == null) {
                PlayerExtendedInfo.UndecodedAppearance(payload)
            } else {
                try {
                    PlayerAppearanceDecoder.decode(payload, definitions)
                } catch (failure: Exception) {
                    // The independent, length-bounded envelope is consumed exactly. A profile failure
                    // must be visible, but does not invalidate subsequent movement/mask framing.
                    PlayerExtendedInfo.UndecodedAppearance(
                        payload, "${failure.javaClass.simpleName}: ${failure.message}",
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
        for (index in bytes.indices.reversed()) bytes[index] = (buffer.g1() - 128).toByte()
        val storage = Unpooled.wrappedBuffer(bytes)
        try {
            val inner = storage.toJagByteBuf()
            val presence = inner.g1()
            val slots = buildList {
                for (slot in 0..7) {
                    if (presence and (1 shl slot) == 0) continue
                    add(
                        PlayerExtendedInfo.HeadIcon(
                            slot, inner.g1(), inner.g2().let { if (it == 65535) -1 else it },
                            inner.readSmart(), inner.readSmart(), inner.readSmart(), inner.readSmart(),
                        ),
                    )
                }
            }
            // Native 0x002216c0 reads only the presence-selected records, not the entire blob.
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
