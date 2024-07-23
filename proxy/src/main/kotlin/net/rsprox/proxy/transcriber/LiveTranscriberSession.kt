package net.rsprox.proxy.transcriber

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.transcriber.TranscriberRunner

public class LiveTranscriberSession(
    private val session: Session,
    private val decodingSession: DecodingSession,
    private val runner: TranscriberRunner,
) {
    public fun pass(
        direction: StreamDirection,
        payload: ByteBuf,
    ) {
        val index = payload.readerIndex()
        try {
            val result = decodingSession.decodePacket(direction, payload, session)
            when (direction) {
                StreamDirection.CLIENT_TO_SERVER -> {
                    runner.onClientProt(result.prot, result.message)
                }
                StreamDirection.SERVER_TO_CLIENT -> {
                    runner.onServerPacket(result.prot, result.message)
                }
            }
        } catch (t: Throwable) {
            payload.readerIndex(index)
            logger.error(t) {
                "Error decoding packet: ${payload.toByteArray().contentToString()}"
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
