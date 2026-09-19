package net.rsprox.proxy.rs3.privacy

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.protocol.rs3.game.outgoing.model.unknown.RawUnknownServerPacket
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.rs3.Rs3RevisionDecoder

/** Decode only safe observation copies. The revision-950 service must use an identity payload cipher. */
internal class Rs3LivePacketDecoder(
    private val decoder: Rs3RevisionDecoder,
    private val sanitizer: Rs3PacketSanitizer,
    private val serverCipher: () -> StreamCipher?,
) {
    fun decode(
        server: Boolean,
        opcode: Int,
        payload: ByteArray,
        session: Session,
    ): IncomingMessage? {
        val prot = if (server) decoder.gameServerProtProvider[opcode] else decoder.gameClientProtProvider[opcode]
        val name = prot.toString()
        val safePayload =
            try {
                // Note(revision): Add the new revision's verified privacy policy here; otherwise GUI payloads bypass it.
                if (decoder.revision == 950) {
                    val normalized = payload.copyOf()
                    if (server) {
                        sanitizer.normalizeServerPayload(name, normalized) { checkNotNull(serverCipher()) }
                    }
                    sanitizer.sanitize(server, name, normalized) ?: return null
                } else {
                    payload
                }
            } catch (_: Exception) {
                // Never retain original bytes or a nested exception from failed sanitization.
                return unknown(server, opcode, name, byteArrayOf(), "Payload omitted: privacy sanitization failed")
            }

        val buffer = Unpooled.wrappedBuffer(safePayload)
        return try {
            if (server) {
                decoder.serverPacketDecoder.decode(opcode, buffer.toJagByteBuf(), session)
            } else {
                decoder.clientPacketDecoder.decode(opcode, buffer.toJagByteBuf(), session)
            }
        } catch (exception: Exception) {
            unknown(
                server,
                opcode,
                name,
                safePayload,
                "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}",
            )
        } finally {
            buffer.release()
        }
    }

    private fun unknown(
        server: Boolean,
        opcode: Int,
        name: String,
        payload: ByteArray,
        failure: String,
    ): IncomingMessage =
        if (server) {
            RawUnknownServerPacket(opcode, name, payload, failure)
        } else {
            RawUnknownClientPacket(opcode, name, payload, failure)
        }
}
