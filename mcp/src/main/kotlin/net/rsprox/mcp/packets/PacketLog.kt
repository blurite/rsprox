package net.rsprox.mcp.packets

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Position in one session's packet log. 0 is before the first record. Monotonic across logins and restarts. */
@JvmInline
public value class Cursor(
    /** The sequence number of the last record before the position. */
    public val seq: Long,
)

/** Who produced a record: the game client, the game server, or rsprox itself (lifecycle markers). */
public enum class Origin(
    /** The letter that stands for the origin in a rendered packet line. */
    public val letter: Char,
) {
    /** A packet the game client sent to the server. */
    CLIENT('C'),

    /** A packet the game server sent to the client. */
    SERVER('S'),

    /** A lifecycle marker that rsprox added. */
    PROXY('P'),
}

/** One record of the log: a decoded packet, or a lifecycle marker that rsprox added. */
public data class PacketRecord(
    /** The position in the log: 1-based, contiguous, never reused. */
    val seq: Long,
    /** The login epoch within the session, or 0 before the first login. */
    val login: Int,
    /** The server tick within that login. Always 0 for [Origin.PROXY] records. */
    val cycle: Int,
    /** The producer of the record. */
    val origin: Origin,
    /** The upper-case prot name such as `IF_SETTEXT`, or a lifecycle marker such as `LOGIN`. */
    val prot: String,
    /** The decoded packet as text, or the detail of a lifecycle marker. */
    val text: String,
)

/** What a read of the log asks for: where to start, which records to keep and how many. */
public data class PacketQuery(
    /** The cursor to read after. */
    val after: Cursor = Cursor(0),
    /** The upper-case prot names to keep. Empty matches every prot. */
    val prots: Set<String> = emptySet(),
    /** The one origin to keep, or null to keep every origin. */
    val origin: Origin? = null,
    /** The case-insensitive substring that the record text must hold, or null to keep every text. */
    val contains: String? = null,
    /** The most records to return. */
    val limit: Int = DEFAULT_LIMIT,
    /** The upper-case prefixes to keep. A prot name must start with one of them. Empty matches every prot. */
    val protPrefixes: Set<String> = emptySet(),
) {
    public companion object {
        /** The limit of a query that names none. */
        public const val DEFAULT_LIMIT: Int = 200
    }
}

/** What a read of the log returns: the records that matched and where the read ended. */
public data class PacketPage(
    /** The records that matched, oldest first. */
    val packets: List<PacketRecord>,
    /** The cursor to pass as `after` next time. Everything up to here was either returned or did not match. */
    val next: Cursor,
    /** The cursor of the newest record in the log. */
    val head: Cursor,
    /** The number of records after the query's cursor that were evicted before they could be scanned. */
    val dropped: Long,
    /** Whether a wait elapsed with no match. */
    val timedOut: Boolean,
)

/**
 * Bounded append-only log. Records are contiguous, so the record with sequence `seq` sits at index
 * `seq - firstSeq` and a read after a cursor only scans the tail it has not seen.
 */
public class PacketLog(
    /** The most records the log keeps before it evicts the oldest. */
    private val capacity: Int = 100_000,
) {
    /** The lock that guards the records. */
    private val lock = ReentrantLock()

    /** The condition that wakes the readers that wait for a record. */
    private val appended = lock.newCondition()

    /** The records that have not been evicted, oldest first. */
    private val records = ArrayDeque<PacketRecord>()

    /** The sequence number of the oldest record that has not been evicted. */
    private var firstSeq = 1L

    /** Append a record, evict the oldest one when the log is full, and return the new sequence number. */
    internal fun append(
        login: Int,
        cycle: Int,
        origin: Origin,
        prot: String,
        text: String,
    ): Long =
        lock.withLock {
            val seq = firstSeq + records.size
            records.addLast(PacketRecord(seq, login, cycle, origin, prot, text))

            if (records.size > capacity) {
                records.removeFirst()
                firstSeq++
            }

            appended.signalAll()
            seq
        }

    /**
     * Read the records after the query's cursor that match it.
     * With `waitMs` of 0 this returns whatever matches now. With a positive `waitMs` and nothing matching,
     * it blocks until the first match arrives or the wait elapses.
     */
    public fun read(
        query: PacketQuery,
        waitMs: Long = 0,
    ): PacketPage {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitMs)
        var target = head().seq

        // A cursor past the head can only come from another log; reading from the head keeps it usable.
        val scan = Scan(query, query.after.seq.coerceIn(0, target))

        while (true) {
            scan.advance(target)

            if (!scan.isAt(target)) continue

            if (scan.matches.isNotEmpty() || waitMs <= 0) return scan.page(timedOut = false)

            target = awaitAppend(target, deadline) ?: return scan.page(timedOut = true)
        }
    }

    /** One read in progress: the records matched so far and how far the log has been scanned. */
    private inner class Scan(
        /** The query that the read answers. */
        private val query: PacketQuery,
        /** The sequence number of the last record that was scanned or evicted. */
        private var scanned: Long,
    ) {
        /** The records that matched so far, oldest first. */
        val matches = ArrayList<PacketRecord>()

        /** The number of records that were evicted before they could be scanned. */
        private var dropped = 0L

        /**
         * Scan the next slice of the records up to [target]. The records are matched outside the lock,
         * a slice at a time, so a broad query never holds up an append.
         */
        fun advance(target: Long) {
            val slice = lock.withLock { sliceAfter(scanned, target) }
            scanned = slice.first - 1
            dropped += slice.dropped

            for (record in slice.records) {
                if (isFull()) return

                scanned++

                if (query.matches(record)) matches += record
            }
        }

        /** Determine if the scan has nothing left to do up to [target]: it reached it or holds the limit. */
        fun isAt(target: Long): Boolean = isFull() || scanned >= target

        /** Build the page of the read, whose wait elapsed with no match when [timedOut]. */
        fun page(timedOut: Boolean): PacketPage = PacketPage(matches, Cursor(scanned), head(), dropped, timedOut)

        /** Determine if the scan holds as many matches as the query asks for. */
        private fun isFull(): Boolean = matches.size >= query.limit
    }

    /** Get the cursor of the newest record. */
    public fun head(): Cursor = lock.withLock { Cursor(headSeq()) }

    /** Get the sequence number of the newest record, or one below the first when the log is empty. */
    private fun headSeq(): Long = firstSeq + records.size - 1

    /**
     * Copy the next records after [scanned], up to [target] and at most [SLICE] of them.
     * Must be called with the lock held.
     */
    private fun sliceAfter(
        scanned: Long,
        target: Long,
    ): Slice {
        val first = maxOf(scanned + 1, firstSeq)
        val count = (target - first + 1).coerceIn(0, SLICE)
        val offset = (first - firstSeq).toInt()

        return Slice(first, first - (scanned + 1), List(count.toInt()) { records[offset + it] })
    }

    /** Wait for a record after [head] and return the new head, or null when the deadline passes first. */
    private fun awaitAppend(
        head: Long,
        deadline: Long,
    ): Long? =
        lock.withLock {
            while (headSeq() <= head) {
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0) return null

                appended.awaitNanos(remaining)
            }

            headSeq()
        }

    /** Determine if the record passes the prot, prot prefix, origin and text filters of the query. */
    private fun PacketQuery.matches(record: PacketRecord): Boolean {
        if (prots.isNotEmpty() && record.prot !in prots) return false

        if (protPrefixes.isNotEmpty() && protPrefixes.none(record.prot::startsWith)) return false

        if (origin != null && record.origin != origin) return false

        if (contains != null && !record.text.contains(contains, ignoreCase = true)) return false

        return true
    }

    /** A run of consecutive records, copied out of the log under its lock. */
    private class Slice(
        /** The sequence number of the first record of the slice, or of the next record when the slice is empty. */
        val first: Long,
        /** The number of records before [first] that were evicted before they could be scanned. */
        val dropped: Long,
        /** The records of the slice, oldest first. */
        val records: List<PacketRecord>,
    )

    private companion object {
        /** The most records copied under the lock at a time. */
        private const val SLICE = 1024L
    }
}
