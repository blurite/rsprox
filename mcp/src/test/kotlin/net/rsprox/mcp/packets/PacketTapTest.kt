package net.rsprox.mcp.packets

import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.mcp.session.LoginInfo
import net.rsprox.mcp.session.Session
import net.rsprox.mcp.session.SessionId
import net.rsprox.mcp.session.target
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection.CLIENT_TO_SERVER
import net.rsprox.shared.StreamDirection.SERVER_TO_CLIENT
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.com
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.string
import kotlin.test.Test
import kotlin.test.assertEquals

/** Drives the session monitor of one launch the way the proxy does and reads what the session shows. */
class PacketTapTest {
    private val session = Session(SessionId("s1"), target(1, "My Server"))
    private val tap = PacketTap(session.packets, session.logins, TapSettingSetStore)

    private fun header(world: Int): BinaryHeader =
        BinaryHeader(
            headerVersion = 1,
            revision = 241,
            subRevision = 1,
            clientType = 1,
            platformType = 1,
            timestamp = 0,
            worldId = world,
            worldFlags = 0,
            worldLocation = 0,
            worldHost = "127.0.0.1",
            worldActivity = "",
            localPlayerIndex = 1,
            accountHash = ByteArray(0),
            clientName = "RuneLite",
            js5MasterIndex = ByteArray(0),
        )

    /** A login as the proxy reports it: a monitor bound to the recording, then the login itself. */
    private fun login(world: Int): SessionMonitor<BinaryHeader> {
        val header = header(world)

        return tap.forSession(header).also { it.onLogin(header) }
    }

    /** A cache with no names in it, which is enough to format ids as numbers. */
    private fun SessionMonitor<BinaryHeader>.withCache(): SessionMonitor<BinaryHeader> {
        val cache =
            object : Cache {
                override fun getNpcType(id: Int): NpcType? = null

                override fun listNpcTypes(): Collection<NpcType> = emptyList()

                override fun getVarBitType(id: Int): VarBitType? = null

                override fun listVarBitTypes(): Collection<VarBitType> = emptyList()

                override fun getGameValType(
                    gameVal: GameVal,
                    id: Int,
                ): GameValType? = null

                override fun listGameValTypes(gameVal: GameVal): Collection<GameValType> = emptyList()

                override fun allGameValTypes(): Map<GameVal, Map<Int, GameValType>> = emptyMap()
            }

        onCacheUpdate { cache }

        return this
    }

    private fun packet(
        prot: String,
        build: RootProperty.() -> Unit = {},
    ): RootProperty {
        val root =
            object : RootProperty {
                override val prot: String = prot
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }

        return root.apply(build)
    }

    private fun records(): List<String> =
        session.packets.read(PacketQuery()).packets.map {
            "L${it.login} T${it.cycle} ${it.origin.letter} ${it.prot} ${it.text}"
        }

    @Test
    fun `a login is marked in the log and shows in the snapshot`() {
        login(world = 302)

        assertEquals(listOf("L1 T0 P LOGIN revision=241 world=302"), records())
        assertEquals(
            LoginInfo(epoch = 1, revision = 241, world = 302, name = null, online = true, transcribing = false),
            session.snapshot().login,
        )
    }

    @Test
    fun `each packet takes its origin from the last direction the proxy announced`() {
        val monitor = login(world = 302).withCache()

        monitor.onPacketDirection(CLIENT_TO_SERVER)
        monitor.onTranscribe(48, packet("if_button") { com(558, 7) })
        monitor.onPacketDirection(SERVER_TO_CLIENT)
        monitor.onTranscribe(49, packet("if_settext") { string("text", "McpProto is available") })
        monitor.onTranscribe(49, packet("server_tick_end"))
        monitor.onPacketDirection(CLIENT_TO_SERVER)
        monitor.onTranscribe(50, packet("resume_p_stringdialog") { string("string", "McpProto") })

        assertEquals(
            listOf(
                "L1 T48 C IF_BUTTON [if_button] com=558:7",
                "L1 T49 S IF_SETTEXT [if_settext] text=\"McpProto is available\"",
                "L1 T49 S SERVER_TICK_END [server_tick_end] ",
                "L1 T50 C RESUME_P_STRINGDIALOG [resume_p_stringdialog] string=\"McpProto\"",
            ),
            records().drop(1),
        )
    }

    @Test
    fun `a packet with nested properties is recorded as several lines`() {
        val monitor = login(world = 302).withCache()

        monitor.onPacketDirection(SERVER_TO_CLIENT)
        monitor.onTranscribe(
            7,
            packet("player_info") {
                group("localplayer") {
                    int("index", 1)
                }
            },
        )

        assertEquals("L1 T7 S PLAYER_INFO [player_info] \n    [localplayer] index=1", records().last())
    }

    @Test
    fun `the login counts as transcribing from its first packet on`() {
        val monitor = login(world = 302)
        assertEquals(false, session.snapshot().login?.transcribing)

        monitor.onPacketDirection(SERVER_TO_CLIENT)

        assertEquals(true, session.snapshot().login?.transcribing)
    }

    @Test
    fun `a second login gets the next epoch and the first can no longer change what the session shows`() {
        val first = login(world = 302)
        first.onNameUpdate("First")
        first.onPacketDirection(SERVER_TO_CLIENT)

        val second = login(world = 303)
        second.onPacketDirection(CLIENT_TO_SERVER)
        second.onTranscribe(3, packet("no_timeout"))
        first.onTranscribe(90, packet("server_tick_end"))
        first.onLogout(header(302))
        first.onNameUpdate("Late")

        assertEquals(
            listOf(
                "L1 T0 P LOGIN revision=241 world=302",
                "L2 T0 P LOGIN revision=241 world=303",
                "L2 T3 C NO_TIMEOUT [no_timeout] ",
                "L1 T90 S SERVER_TICK_END [server_tick_end] ",
                "L1 T0 P LOGOUT world=302",
            ),
            records(),
        )
        assertEquals(
            LoginInfo(epoch = 2, revision = 241, world = 303, name = null, online = true, transcribing = true),
            session.snapshot().login,
        )
    }

    @Test
    fun `a name update and a logout show in the snapshot`() {
        val monitor = login(world = 302)

        monitor.onNameUpdate("McpProto")
        assertEquals("McpProto", session.snapshot().login?.name)

        monitor.onLogout(header(302))

        assertEquals(false, session.snapshot().login?.online)
        assertEquals("L1 T0 P LOGOUT world=302", records().last())
    }

    @Test
    fun `a packet that cannot be formatted is recorded as such and the packets after it still arrive`() {
        val monitor = login(world = 302)

        monitor.onPacketDirection(CLIENT_TO_SERVER)
        monitor.onTranscribe(48, packet("if_button") { com(558, 7) })
        monitor.onTranscribe(49, packet("no_timeout"))

        assertEquals(
            listOf(
                "L1 T48 C IF_BUTTON [if_button] <unformattable: java.lang.IllegalStateException: Cache unavailable>",
                "L1 T49 C NO_TIMEOUT [no_timeout] ",
            ),
            records().drop(1),
        )
    }

    @Test
    fun `the monitor of a launch records nothing before a login binds it`() {
        tap.onPacketDirection(SERVER_TO_CLIENT)
        tap.onTranscribe(1, packet("server_tick_end"))
        tap.onNameUpdate("McpProto")

        assertEquals(emptyList(), records())
        assertEquals(null, session.snapshot().login)
    }
}
