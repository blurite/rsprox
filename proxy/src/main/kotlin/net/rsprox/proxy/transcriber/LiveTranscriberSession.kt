package net.rsprox.proxy.transcriber

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprox.protocol.exceptions.DecodeError
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.binary.StreamDirection
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.transcriber.TranscriberRunner
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

public class LiveTranscriberSession(
    private val session: Session,
    private val decodingSession: DecodingSession,
    private val runner: TranscriberRunner,
) {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock: Object = Object()
    private val queue: Queue<Packet> = ConcurrentLinkedQueue()

    @Volatile
    private var running: Boolean = true

    init {
        launchThread()
    }

    public fun pass(
        direction: StreamDirection,
        payload: ByteBuf,
    ) {
        if (!running) return
        queue.offer(Packet(direction, payload))
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    private fun decode(packet: Packet) {
        val index = packet.payload.readerIndex()
        try {
            val result = decodingSession.decodePacket(packet.direction, packet.payload, session)
            when (packet.direction) {
                StreamDirection.CLIENT_TO_SERVER -> {
                    runner.onClientProt(result.prot, result.message)
                }
                StreamDirection.SERVER_TO_CLIENT -> {
                    runner.onServerPacket(result.prot, result.message)
                }
            }
        } catch (t: Throwable) {
            packet.payload.readerIndex(index)
            logger.error(t) {
                "Error decoding packet: ${packet.payload.toByteArray().contentToString()}"
            }
            // Decode error is a special error we cannot recover from
            // If this is hit, the state becomes corrupted and the decoding cannot continue.
            if (t is DecodeError) {
                running = false
                throw t
            }
        } finally {
            packet.payload.release()
        }
    }

    private fun launchThread(): Thread {
        val thread =
            Thread {
                while (this.running) {
                    while (queue.isNotEmpty()) {
                        val next = queue.poll()
                        decode(next)
                    }
                    synchronized(lock) {
                        lock.wait()
                    }
                }
            }
        thread.start()
        return thread
    }

    public fun shutdown() {
        this.running = false
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    private class Packet(
        val direction: StreamDirection,
        val payload: ByteBuf,
    )

    private companion object {
        private val logger = InlineLogger()
    }
}
