package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf

internal fun JagByteBuf.readSpotanimRemovals(): List<Int> =
    buildList {
        val count = g1()
        for (index in 0 until count) {
            val id = g2().toShort().toInt()
            add(id)
            // Note: native 950 player/NPC masks end the removal list at -1, even before count.
            // The next byte is the addition count; do not consume the remaining advertised entries.
            if (id == -1) break
        }
    }
