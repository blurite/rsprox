package net.rsprox.mcp.packets

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Position in one session's packet log. 0 is before the first record. Monotonic across logins and restarts. */
@JvmInline
public value class Cursor(
    public val seq: Long,
)

/** Who produced a record: the game client, the game server, or rsprox itself (lifecycle markers). */
public enum class Origin(
    public val letter: Char,
) {
    CLIENT('C'),
    SERVER('S'),
    PROXY('P'),
}

public data class PacketRecord(
    /** 1-based, contiguous, never reused. */
    val seq: Long,
    /** Login epoch within the session, or 0 before the first login. */
    val login: Int,
    /** Server tick within that login. Always 0 for [Origin.PROXY] records. */
    val cycle: Int,
    val origin: Origin,
    /** Upper-case prot name such as `IF_SETTEXT`, or a lifecycle marker such as `LOGIN`. */
    val prot: String,
    /** Wall clock at append. Wire packets are flushed once per server tick, so this is tick-granular. */
    val atMs: Long,
    val text: String,
)

public data class PacketQuery(
    val after: Cursor = Cursor(0),
    /** Upper-case prot names. Empty matches every prot. */
    val prots: Set<String> = emptySet(),
    val origin: Origin? = null,
    /** Case-insensitive substring of the record text. */
    val contains: String? = null,
    val limit: Int = DEFAULT_LIMIT,
) {
    public companion object {
        public const val DEFAULT_LIMIT: Int = 200
    }
}

public data class PacketPage(
    val packets: List<PacketRecord>,
    /** Pass as `after` next time. Everything up to here was either returned or did not match. */
    val next: Cursor,
    val head: Cursor,
    /** Records after the query's cursor that were evicted before they could be scanned. */
    val dropped: Long,
    /** True only when a wait elapsed with no match. */
    val timedOut: Boolean,
)

/**
 * Bounded append-only log. Records are contiguous, so the record with sequence `seq` sits at index
 * `seq - firstSeq` and a read after a cursor only scans the tail it has not seen.
 */
public class PacketLog(
    private val capacity: Int = 100_000,
) {
    private val lock = ReentrantLock()
    private val appended = lock.newCondition()
    private val records = ArrayDeque<PacketRecord>()
    private var firstSeq = 1L

    internal fun append(
        login: Int,
        cycle: Int,
        origin: Origin,
        prot: String,
        text: String,
    ): Long =
        lock.withLock {
            val seq = firstSeq + records.size
            records.addLast(PacketRecord(seq, login, cycle, origin, prot, System.currentTimeMillis(), text))

            if (records.size > capacity) {
                records.removeFirst()
                firstSeq++
            }

            appended.signalAll()
            seq
        }

    /**
     * With `waitMs` of 0 this returns whatever matches now. With a positive `waitMs` and nothing matching,
     * it blocks until the first match arrives or the wait elapses.
     */
    public fun read(
        query: PacketQuery,
        waitMs: Long = 0,
    ): PacketPage =
        lock.withLock {
            val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitMs)
            val matches = ArrayList<PacketRecord>()

            // A cursor past the head can only come from another log; reading from the head keeps it usable.
            var scanned = query.after.seq.coerceIn(0, headSeq())
            var dropped = 0L
            var timedOut = false

            while (true) {
                if (scanned + 1 < firstSeq) {
                    dropped += firstSeq - (scanned + 1)
                    scanned = firstSeq - 1
                }

                while (scanned < headSeq() && matches.size < query.limit) {
                    scanned++
                    val record = records[(scanned - firstSeq).toInt()]
                    if (query.matches(record)) matches += record
                }

                if (matches.isNotEmpty() || waitMs <= 0) break
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0) {
                    timedOut = true
                    break
                }

                appended.awaitNanos(remaining)
            }

            PacketPage(matches, Cursor(scanned), Cursor(headSeq()), dropped, timedOut)
        }

    public fun head(): Cursor = lock.withLock { Cursor(headSeq()) }

    private fun headSeq(): Long = firstSeq + records.size - 1

    private fun PacketQuery.matches(record: PacketRecord): Boolean =
        (prots.isEmpty() || record.prot in prots) &&
            (origin == null || record.origin == origin) &&
            (contains == null || record.text.contains(contains, ignoreCase = true))
}
