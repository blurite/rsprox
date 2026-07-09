package net.rsprox.gui

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatSeparator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.components.LaunchBar
import java.awt.Component
import java.awt.Cursor
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.text.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

public class HomePanel(
    private val launchBar: LaunchBar,
    private val onReplay: () -> Unit,
    private val onReplayDump: (Path) -> Unit,
    private val onOpenLogs: () -> Unit,
    private val onReportBug: () -> Unit,
) : JPanel() {
    private var recentDumpRowsPanel: JPanel? = null

    init {
        layout = MigLayout("ins 0, gap 0, fill", "[fill, grow]", "[fill, grow]")
        border = BorderFactory.createEmptyBorder()

        val content =
            JPanel(
                MigLayout(
                    "ins 38 0 42 0, gap 18, fill",
                    "push[870!, fill]push",
                    "[][][]",
                ),
            ).apply {
                add(createHeader(), "growx, wrap")
                add(createTopActionsRow(), "growx, wrap")
                add(createBodyColumns(), "growx, top")
            }

        add(
            JScrollPane(content).apply {
                border = null
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBar.unitIncrement = 16
            },
            "grow",
        )
    }

    public fun refreshRecentDumps() {
        val rowsPanel = recentDumpRowsPanel ?: return
        populateRecentDumps(rowsPanel)
    }

    private fun createHeader(): JPanel {
        return JPanel(MigLayout("ins 0 0 12 0, gap 4, fillx", "[center]", "[][]")).apply {
            isOpaque = false
            add(
                JPanel(MigLayout("ins 0, gap 6", "[][]", "[]")).apply {
                    isOpaque = false
                    add(
                        FlatLabel().apply {
                            text = "Welcome to"
                            font = font.deriveFont(24f)
                        },
                    )
                    add(
                        FlatLabel().apply {
                            text = "RSProx"
                            useStyleClass(STYLE_ACCENT_LABEL)
                            font = font.deriveFont(24f)
                        },
                    )
                },
                "wrap",
            )
            add(
                FlatLabel().apply {
                    text = "Start a capture, replay a dump, or reopen recent binary logs."
                    useStyleClass(STYLE_MUTED_LABEL)
                    horizontalAlignment = FlatLabel.CENTER
                },
                "growx",
            )
        }
    }

    private fun createQuickLaunchPanel(): JPanel {
        return createActionPanel(
            title = "Quick Launch",
            subtitle = "Start a session with the selected settings.",
            action = { launchBar.launchConfiguration() },
        )
    }

    private fun createSessionDatabasePanel(): JPanel {
        return createActionPanel(
            title = "Replay & Dumps",
            subtitle = "Open the replay tool or browse binary logs.",
            action = { onReplay() },
        )
    }

    private fun createTopActionsRow(): JPanel {
        return JPanel(MigLayout("ins 0, gap 18, fillx", "[500!, fill][352!, fill]", "[]")).apply {
            isOpaque = false
            add(createQuickLaunchPanel(), "w 500!, growx")
            add(createSessionDatabasePanel(), "w 352!, growx")
        }
    }

    private fun createBodyColumns(): JPanel {
        return JPanel(MigLayout("ins 0, gap 18, fillx", "[500!, fill][352!, fill]", "[]")).apply {
            isOpaque = false
            add(
                JPanel(MigLayout("ins 0, gapy 18, fillx", "[fill, grow]", "[][]")).apply {
                    isOpaque = false
                    minimumSize = Dimension(0, 0)
                    add(createLaunchSection(), "growx, top, wrap")
                    add(createUpdateLogSection(), "growx, top")
                },
                "w 500!, growx, top",
            )
            add(
                JPanel(MigLayout("ins 0, gapy 18, fillx", "[fill, grow]", "[][]")).apply {
                    isOpaque = false
                    minimumSize = Dimension(0, 0)
                    add(createRecentDumpsSection(), "growx, top, wrap")
                    add(createQuickLinksSection(), "growx, top")
                },
                "w 352!, growx, top",
            )
        }
    }

    private fun createLaunchSection(): JPanel {
        return createPanel("New Session", "Configure and launch a capture.").apply {
            add(launchBar, "growx, gaptop 18, wrap")
        }
    }

    private fun createRecentDumpsSection(): JPanel {
        return createPanel("Recent Dumps", "Quick replay from history.").apply {
            val rowsPanel =
                JPanel(MigLayout("ins 0, gapy 0, fillx", "[fill, grow]", "[]")).apply {
                    isOpaque = false
                    minimumSize = Dimension(0, 0)
                }
            recentDumpRowsPanel = rowsPanel
            add(rowsPanel, "growx, gaptop 12")
            populateRecentDumps(rowsPanel)
        }
    }

    private fun createUpdateLogSection(): JPanel {
        return createPanel("Update Log", "Latest RSProx updates.").apply {
            val releasesPanel =
                JPanel(MigLayout("ins 0, gapy 4, fillx", "[fill, grow]", "[]")).apply {
                    isOpaque = false
                    minimumSize = Dimension(0, 0)
                    add(createStatusRow("Loading releases..."), "growx")
                }
            add(
                releasesPanel,
                "growx, gaptop 12",
            )
            loadReleaseRows(releasesPanel)
        }
    }

    private fun createQuickLinksSection(): JPanel {
        return createPanel("Quick Links", "Common local destinations.").apply {
            add(createLinkRow("Binary Logs", "Open capture output folder.", onOpenLogs), "growx, gaptop 12, wrap")
            add(createLinkRow("Report a Bug", "Open RSProx issues.", onReportBug), "growx")
        }
    }

    private fun createPanel(
        title: String,
        subtitle: String,
        titleSizeDelta: Float = 4f,
        action: (() -> Unit)? = null,
    ): JPanel {
        return JPanel(MigLayout("ins 18, gap 5, fillx, wrap 1", "[fill, grow]", "[][]")).apply {
            minimumSize = Dimension(0, 0)
            useStyleClass(STYLE_HOME_CARD)
            add(
                FlatLabel().apply {
                    text = title
                    useStyleClass(STYLE_ACCENT_LABEL)
                    font = font.deriveFont(font.size2D + titleSizeDelta)
                    horizontalAlignment = SwingConstants.LEFT
                },
                "growx, wrap",
            )
            add(
                FlatLabel().apply {
                    text = subtitle
                    useStyleClass(STYLE_MUTED_LABEL)
                    horizontalAlignment = SwingConstants.LEFT
                },
                "growx, wrap",
            )
            action?.let { installHoverAction(this, STYLE_HOME_CARD, STYLE_HOME_CARD_HOVER, it) }
        }
    }

    private fun createActionPanel(
        title: String,
        subtitle: String,
        action: () -> Unit,
    ): JPanel {
        return createPanel(title, subtitle, titleSizeDelta = 2f, action = action)
    }

    private fun createRecentDumpRow(dump: RecentDump): JPanel {
        return createInteractiveRow(
            title = dump.path.fileName?.toString() ?: dump.path.toString(),
            subtitle = "${dump.modifiedText}  |  ${dump.parentText}",
            trailing = null,
            action = { onReplayDump(dump.path) },
        ).apply {
            toolTipText = dump.path.toString()
        }
    }

    private fun populateRecentDumps(rowsPanel: JPanel) {
        rowsPanel.removeAll()
        val dumps = recentDumps()
        if (dumps.isEmpty()) {
            rowsPanel.add(
                FlatLabel().apply {
                    text = "No replay dumps opened yet."
                    useStyleClass(STYLE_MUTED_LABEL)
                },
                "gaptop 2",
            )
        } else {
            dumps.forEachIndexed { index, dump ->
                if (index > 0) {
                    rowsPanel.add(FlatSeparator(), "growx, wrap")
                }
                rowsPanel.add(createRecentDumpRow(dump), "growx, wrap")
            }
        }
        rowsPanel.revalidate()
        rowsPanel.repaint()
    }

    private fun createUpdateRow(
        title: String,
        body: String,
        tag: String,
        action: (() -> Unit)?,
    ): JPanel {
        return createInteractiveRow(
            title = title,
            subtitle = body,
            trailing = tag,
            action = action,
        )
    }

    private fun createLinkRow(
        title: String,
        subtitle: String,
        action: () -> Unit,
    ): JPanel {
        return createInteractiveRow(
            title = title,
            subtitle = subtitle,
            trailing = null,
            action = action,
        )
    }

    private fun createInteractiveRow(
        title: String,
        subtitle: String,
        trailing: String?,
        action: (() -> Unit)?,
    ): JPanel {
        return JPanel(MigLayout("ins 7 4 7 4, gap 8, fillx", "[fill, grow][]", "[][]")).apply {
            minimumSize = Dimension(0, 0)
            useStyleClass(STYLE_HOME_ROW)
            add(
                FlatLabel().apply {
                    text = title
                    toolTipText = title
                    horizontalAlignment = SwingConstants.LEFT
                },
                if (trailing == null) "spanx 2, growx, wrap" else "growx",
            )
            if (trailing != null) {
                add(
                    FlatLabel().apply {
                        text = trailing
                        useStyleClass(STYLE_ACCENT_LABEL)
                        horizontalAlignment = SwingConstants.RIGHT
                    },
                    "wrap",
                )
            }
            add(
                FlatLabel().apply {
                    text = subtitle
                    useStyleClass(STYLE_MUTED_LABEL)
                    toolTipText = subtitle
                    horizontalAlignment = SwingConstants.LEFT
                },
                "spanx 2, growx",
            )
            action?.let { installHoverAction(this, STYLE_HOME_ROW, STYLE_HOME_ROW_HOVER, it) }
        }
    }

    private fun createStatusRow(text: String): JPanel {
        return JPanel(MigLayout("ins 8 0 8 0, fillx", "[fill, grow]", "[]")).apply {
            isOpaque = false
            add(
                FlatLabel().apply {
                    this.text = text
                    useStyleClass(STYLE_MUTED_LABEL)
                },
                "growx",
            )
        }
    }

    private fun installHoverAction(
        panel: JPanel,
        normalStyleClass: String,
        hoverStyleClass: String,
        action: () -> Unit,
    ) {
        val listener =
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    action()
                }

                override fun mouseEntered(e: MouseEvent) {
                    panel.useStyleClass(hoverStyleClass)
                    panel.repaint()
                }

                override fun mouseExited(e: MouseEvent) {
                    val point = SwingUtilities.convertPoint(e.component, e.point, panel)
                    if (!panel.contains(point)) {
                        panel.useStyleClass(normalStyleClass)
                        panel.repaint()
                    }
                }
            }
        panel.useStyleClass(normalStyleClass)
        installRecursiveHover(panel, panel, listener)
    }

    private fun installRecursiveHover(
        root: JPanel,
        component: Component,
        listener: MouseAdapter,
    ) {
        component.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        component.addMouseListener(listener)
        if (component is JPanel) {
            component.isOpaque = component === root
            component.components.forEach { child ->
                installRecursiveHover(root, child, listener)
            }
        }
    }

    private fun Component.useStyleClass(styleClass: String) {
        if (this is JPanel || this is FlatLabel) {
            (this as javax.swing.JComponent).putClientProperty(FlatClientProperties.STYLE_CLASS, styleClass)
        }
    }

    private fun loadReleaseRows(rowsPanel: JPanel) {
        Thread {
            val result = runCatching { fetchReleases() }
            SwingUtilities.invokeLater {
                rowsPanel.removeAll()
                result
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() }
                    ?.forEachIndexed { index, release ->
                        if (index > 0) {
                            rowsPanel.add(FlatSeparator(), "growx, wrap")
                        }
                        rowsPanel.add(
                            createUpdateRow(
                                title = release.title,
                                body = release.summary,
                                tag = release.publishedText,
                                action = { openUrl(release.url) },
                            ),
                            "growx, wrap",
                        )
                    }
                    ?: rowsPanel.add(createStatusRow("Unable to load releases right now."), "growx")
                rowsPanel.revalidate()
                rowsPanel.repaint()
            }
        }.apply {
            name = "rsprox-release-fetch"
            isDaemon = true
            start()
        }
    }

    private fun fetchReleases(): List<ReleaseSummary> {
        val request =
            HttpRequest
                .newBuilder(RELEASES_URI)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "RSProx")
                .GET()
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            error("GitHub releases request failed with HTTP ${response.statusCode()}")
        }
        val releases = jsonMapper.readTree(response.body())
        if (!releases.isArray) {
            return emptyList()
        }
        return releases.mapNotNull(::parseRelease)
    }

    private fun parseRelease(node: JsonNode): ReleaseSummary? {
        val url = node.path("html_url").asText(null) ?: return null
        val tag = node.path("tag_name").asText("")
        val name = node.path("name").asText("").ifBlank { tag.ifBlank { "Release" } }
        val body = node.path("body").asText("")
        val publishedAt = node.path("published_at").asText("")
        return ReleaseSummary(
            title = name,
            summary = summarizeReleaseBody(body),
            publishedText = formatReleaseDate(publishedAt),
            url = URI(url),
        )
    }

    private fun summarizeReleaseBody(body: String): String {
        return body
            .lineSequence()
            .map { it.trim().trimStart('-', '*').trim() }
            .firstOrNull { it.isNotEmpty() }
            ?.take(140)
            ?: "View release notes."
    }

    private fun formatReleaseDate(value: String): String {
        return runCatching {
            RELEASE_DATE_FORMAT.format(Instant.parse(value))
        }.getOrDefault("release")
    }

    private fun openUrl(uri: URI) {
        if (!Desktop.isDesktopSupported()) {
            return
        }
        runCatching { Desktop.getDesktop().browse(uri) }
    }

    private fun recentDumps(): List<RecentDump> {
        return RecentReplayDumps
            .list(3)
            .map { path ->
                RecentDump(
                    path = path,
                    modified = Files.getLastModifiedTime(path).toMillis(),
                )
            }
    }

    private data class RecentDump(
        val path: Path,
        val modified: Long,
    ) {
        val modifiedText: String =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(modified))
        val parentText: String =
            path.parent
                ?.fileName
                ?.toString()
                ?.takeLast(28)
                ?: path.parent?.toString().orEmpty()
    }

    private data class ReleaseSummary(
        val title: String,
        val summary: String,
        val publishedText: String,
        val url: URI,
    )

    private companion object {
        private val RELEASES_URI: URI = URI("https://api.github.com/repos/blurite/rsprox/releases?per_page=3")
        private val RELEASE_DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())
        private val httpClient: HttpClient = HttpClient.newHttpClient()
        private val jsonMapper = jacksonObjectMapper()
        private const val STYLE_HOME_CARD = "rsproxHomeCard"
        private const val STYLE_HOME_CARD_HOVER = "rsproxHomeCardHover"
        private const val STYLE_HOME_ROW = "rsproxHomeRow"
        private const val STYLE_HOME_ROW_HOVER = "rsproxHomeRowHover"
        private const val STYLE_ACCENT_LABEL = "rsproxAccent"
        private const val STYLE_MUTED_LABEL = "rsproxMuted"
    }
}
