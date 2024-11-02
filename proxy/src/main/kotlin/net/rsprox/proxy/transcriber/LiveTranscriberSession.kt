package net.rsprox.proxy.transcriber

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprox.protocol.exceptions.DecodeError
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.shared.StreamDirection
import net.rsprox.transcriber.Packet
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
    private val queue: Queue<UnidentifiedPacket> = ConcurrentLinkedQueue()

    @Volatile
    private var running: Boolean = true

    @Volatile private var revision: Int = -1

    private val packetList: MutableList<Packet> = mutableListOf()

    init {
        launchThread()
    }

    public fun setRevision(revision: Int) {
        this.revision = revision
    }

    public fun pass(
        direction: StreamDirection,
        payload: ByteBuf,
    ) {
        if (!running) return
        queue.offer(UnidentifiedPacket(direction, payload))
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    private fun decode(unidentified: UnidentifiedPacket) {
        val index = unidentified.payload.readerIndex()
        try {
            val result =
                decodingSession.decodePacket(
                    unidentified.direction,
                    unidentified.payload,
                    session,
                )
            packetList += Packet(unidentified.direction, result.prot, result.message)
            if (result.prot.toString() == "SERVER_TICK_END") {
                executeRunner(runner.preprocess(packetList))
                packetList.clear()
            }
        } catch (t: Throwable) {
            unidentified.payload.readerIndex(index)
            logger.error(t) {
                "Error decoding packet: ${unidentified.payload.toByteArray().contentToString()}"
            }
            // Decode error is a special error we cannot recover from
            // If this is hit, the state becomes corrupted and the decoding cannot continue.
            if (t is DecodeError) {
                running = false
                throw t
            }
        } finally {
            unidentified.payload.release()
        }
    }

    private fun executeRunner(results: List<Packet>) {
        for (packet in results) {
            when (packet.direction) {
                StreamDirection.CLIENT_TO_SERVER -> {
                    runner.onClientProt(packet.prot, packet.message, revision)
                }
                StreamDirection.SERVER_TO_CLIENT -> {
                    runner.onServerPacket(packet.prot, packet.message, revision)
                }
            }
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

    public fun flush() {
        synchronized(lock) {
            executeRunner(runner.preprocess(packetList))
            packetList.clear()
        }
    }

    public fun shutdown() {
        this.running = false
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    private class UnidentifiedPacket(
        val direction: StreamDirection,
        val payload: ByteBuf,
    )

    private companion object {
        private val logger = InlineLogger()
    }
}
