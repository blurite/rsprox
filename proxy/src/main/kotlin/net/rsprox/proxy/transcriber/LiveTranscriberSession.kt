package net.rsprox.proxy.transcriber

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.exceptions.DecodeError
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.unix.UnixSocketConnection
import net.rsprox.shared.StreamDirection
import net.rsprox.transcriber.Packet
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.KeyStorage
import net.rsprox.transcriber.state.SessionTracker

public class LiveTranscriberSession(
    private val session: Session,
    private val decodingSession: DecodingSession,
    private val runner: TranscriberRunner,
    private val sessionTracker: SessionTracker,
    internal val cacheProvider: CacheProvider,
    private val unixSocketConnection: UnixSocketConnection?,
) {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock: Object = Object()
    private val queue: ArrayDeque<PendingEvent> = ArrayDeque()

    @Volatile
    private var running: Boolean = true

    @Volatile private var revision: Int = -1

    private val packetList: MutableList<Packet> = mutableListOf()

    public fun getKeyStorage(): KeyStorage {
        return sessionTracker.keyStorage
    }

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
        synchronized(lock) {
            if (!running) {
                payload.release()
                return
            }
            queue.addLast(UnidentifiedPacket(direction, payload))
            lock.notifyAll()
        }
    }

    private fun decode(unidentified: UnidentifiedPacket) {
        val index = unidentified.payload.readerIndex()
        try {
            val results =
                decodingSession.decodePacket(
                    unidentified.direction,
                    unidentified.payload,
                    session,
                )
            for (result in results) {
                unixSocketConnection?.push(result.message)
                val packet = Packet(unidentified.direction, result.prot, result.message)
                packetList += packet
                if (result.prot.toString() == "SERVER_TICK_END") {
                    flushPackets()
                }
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
                    sessionTracker.onClientPacket(packet.message, packet.prot)
                    sessionTracker.beforeTranscribe(packet.message)
                    runner.onClientProt(packet.prot, packet.message, revision)
                    sessionTracker.afterTranscribe(packet.message)
                }
                StreamDirection.SERVER_TO_CLIENT -> {
                    sessionTracker.onServerPacket(packet.message, packet.prot)
                    sessionTracker.beforeTranscribe(packet.message)
                    runner.onServerPacket(packet.prot, packet.message, revision)
                    sessionTracker.afterTranscribe(packet.message)
                }
            }
        }
    }

    private fun launchThread(): Thread {
        val thread =
            Thread {
                try {
                    // Preload gameval types while allowing packets to queue during login.
                    cacheProvider.get().allGameValTypes()
                    while (true) {
                        val next =
                            synchronized(lock) {
                                while (queue.isEmpty() && running) {
                                    lock.wait()
                                }
                                queue.removeFirstOrNull()
                            } ?: break
                        when (next) {
                            is UnidentifiedPacket -> decode(next)
                            is Flush -> {
                                try {
                                    flushPackets()
                                } finally {
                                    next.afterFlush()
                                }
                            }
                        }
                    }
                    flushPackets()
                } catch (t: Throwable) {
                    logger.error(t) { "Live transcription stopped" }
                } finally {
                    synchronized(lock) {
                        running = false
                        for (pending in queue) {
                            if (pending is UnidentifiedPacket) {
                                pending.payload.release()
                            }
                        }
                        queue.clear()
                    }
                    packetList.clear()
                }
            }
        thread.start()
        return thread
    }

    public fun flush(afterFlush: () -> Unit = {}) {
        synchronized(lock) {
            if (running) {
                // The worker owns packetList and session state, including on disconnect.
                queue.addLast(Flush(afterFlush))
                lock.notifyAll()
                return
            }
        }
        afterFlush()
    }

    private fun flushPackets() {
        if (packetList.isEmpty()) return
        val packets = packetList.toList()
        // A failed transcript must not replay partially applied state on the next tick.
        packetList.clear()
        executeRunner(runner.preprocess(packets))
    }

    public fun shutdown() {
        synchronized(lock) {
            this.running = false
            lock.notifyAll()
        }
    }

    private sealed interface PendingEvent

    private class Flush(
        val afterFlush: () -> Unit,
    ) : PendingEvent

    private class UnidentifiedPacket(
        val direction: StreamDirection,
        val payload: ByteBuf,
    ) : PendingEvent

    private companion object {
        private val logger = InlineLogger()
    }
}
