package net.rsprox.mcp.packets

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PacketLogTest {
    private fun PacketLog.add(
        prot: String,
        origin: Origin = Origin.SERVER,
        text: String = "[${prot.lowercase()}]",
    ): Long = append(login = 1, cycle = 0, origin = origin, prot = prot, text = text)

    private fun PacketPage.seqs(): List<Long> = packets.map { it.seq }

    @Test
    fun `an empty log reads as empty at cursor zero`() {
        val log = PacketLog()
        val page = log.read(PacketQuery())
        assertEquals(emptyList(), page.packets)
        assertEquals(Cursor(0), page.next)
        assertEquals(Cursor(0), page.head)
        assertEquals(0, page.dropped)
        assertFalse(page.timedOut)
    }

    @Test
    fun `sequences start at one and reading from next returns only what came later`() {
        val log = PacketLog()
        assertEquals(1, log.add("A"))
        assertEquals(2, log.add("B"))
        val first = log.read(PacketQuery())
        assertEquals(listOf(1L, 2L), first.seqs())
        assertEquals(Cursor(2), first.next)
        assertEquals(Cursor(2), log.head())

        log.add("C")
        val second = log.read(PacketQuery(after = first.next))
        assertEquals(listOf(3L), second.seqs())
        assertEquals(Cursor(3), second.next)
        assertEquals(emptyList(), log.read(PacketQuery(after = second.next)).packets)
    }

    @Test
    fun `next skips past scanned records that did not match`() {
        val log = PacketLog()
        log.add("A")
        log.add("B")
        log.add("B")
        val page = log.read(PacketQuery(prots = setOf("A")))
        assertEquals(listOf(1L), page.seqs())
        assertEquals(Cursor(3), page.next)
        assertEquals(Cursor(3), page.head)
    }

    @Test
    fun `a page cut short by the limit resumes at the next unreturned record`() {
        val log = PacketLog()
        repeat(5) { log.add("A") }
        val first = log.read(PacketQuery(limit = 2))
        assertEquals(listOf(1L, 2L), first.seqs())
        assertEquals(Cursor(2), first.next)
        assertEquals(Cursor(5), first.head)
        val rest = log.read(PacketQuery(after = first.next, limit = 10))
        assertEquals(listOf(3L, 4L, 5L), rest.seqs())
        assertEquals(Cursor(5), rest.next)
    }

    @Test
    fun `evicted records are reported as dropped and sequences are never reused`() {
        val log = PacketLog(capacity = 3)
        repeat(5) { log.add("A") }
        val all = log.read(PacketQuery())
        assertEquals(listOf(3L, 4L, 5L), all.seqs())
        assertEquals(2, all.dropped)

        val partial = log.read(PacketQuery(after = Cursor(1)))
        assertEquals(1, partial.dropped)
        assertEquals(0, log.read(PacketQuery(after = Cursor(2))).dropped)
        assertEquals(6, log.add("A"))
    }

    @Test
    fun `filters by prot, origin and text`() {
        val log = PacketLog()
        log.add("IF_BUTTON", Origin.CLIENT, "[if_button] com=558:7")
        log.add("IF_SETTEXT", Origin.SERVER, "[if_settext] com=558:13, text=\"McpProto is available\"")
        log.add("LOGIN", Origin.PROXY, "revision=241 world=1")
        log.add("IF_SETTEXT", Origin.SERVER, "[if_settext] com=162:5, text=\"hello\"")

        assertEquals(listOf(2L, 4L), log.read(PacketQuery(prots = setOf("IF_SETTEXT"))).seqs())
        assertEquals(listOf(1L, 3L), log.read(PacketQuery(prots = setOf("IF_BUTTON", "LOGIN"))).seqs())
        assertEquals(listOf(1L), log.read(PacketQuery(origin = Origin.CLIENT)).seqs())
        assertEquals(listOf(3L), log.read(PacketQuery(origin = Origin.PROXY)).seqs())
        assertEquals(listOf(2L), log.read(PacketQuery(contains = "mcpproto")).seqs())
        assertEquals(
            emptyList(),
            log.read(PacketQuery(prots = setOf("IF_SETTEXT"), origin = Origin.CLIENT)).seqs(),
        )
    }

    @Test
    fun `a waiting read returns as soon as a matching record is appended`() {
        val log = PacketLog()
        log.add("A")
        val read =
            CompletableFuture.supplyAsync {
                log.read(PacketQuery(after = Cursor(1), prots = setOf("WANTED")), waitMs = 30_000)
            }
        log.add("NOISE")
        assertFalse(read.isDone)
        log.add("WANTED")
        val page = read.get(10, TimeUnit.SECONDS)
        assertEquals(listOf(3L), page.seqs())
        assertEquals(Cursor(3), page.next)
        assertFalse(page.timedOut)
    }

    @Test
    fun `a waiting read times out when nothing matches and still advances the cursor`() {
        val log = PacketLog()
        log.add("A")
        val page = log.read(PacketQuery(prots = setOf("WANTED")), waitMs = 50)
        assertTrue(page.timedOut)
        assertEquals(emptyList(), page.packets)
        assertEquals(Cursor(1), page.next)
    }

    @Test
    fun `a read without a wait never reports a timeout`() {
        val log = PacketLog()
        assertFalse(log.read(PacketQuery(prots = setOf("WANTED")), waitMs = 0).timedOut)
    }

    @Test
    fun `a waiting read with matches already present returns at once`() {
        val log = PacketLog()
        log.add("WANTED")
        val page = log.read(PacketQuery(prots = setOf("WANTED")), waitMs = 30_000)
        assertEquals(listOf(1L), page.seqs())
        assertFalse(page.timedOut)
    }

    @Test
    fun `a cursor beyond the head reads from the head`() {
        val log = PacketLog()
        log.add("A")
        val page = log.read(PacketQuery(after = Cursor(99)))
        assertEquals(Cursor(1), page.next)
        log.add("B")
        assertEquals(listOf(2L), log.read(PacketQuery(after = page.next)).seqs())
    }
}
