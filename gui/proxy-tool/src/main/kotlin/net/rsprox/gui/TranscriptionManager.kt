package net.rsprox.gui

import java.awt.*
import java.awt.event.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.*
import javax.swing.*
import javax.swing.DefaultListModel
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

private inline fun onEdt(crossinline run: () -> Unit) {
    if (SwingUtilities.isEventDispatchThread()) run() else SwingUtilities.invokeLater { run() }
}

private fun defaultForkJoinPool(): ForkJoinPool {
    val parallelism = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(2)
    val handler = Thread.UncaughtExceptionHandler { _, ex -> ex.printStackTrace() }
    val factory =
        ForkJoinPool.ForkJoinWorkerThreadFactory { pool ->
            object : ForkJoinWorkerThread(pool) {
                init {
                    name = "tasks-fj-$poolIndex"
                    isDaemon = true
                }
            }
        }
    return ForkJoinPool(parallelism, factory, handler, true)
}

public class TranscriptionManager(
    private val pool: ForkJoinPool = defaultForkJoinPool(),
) {
    private val listeners = mutableListOf<() -> Unit>()
    internal val model: DefaultListModel<TrackedTranscriptionTask> = DefaultListModel()

    public fun onChange(listener: () -> Unit) {
        listeners += listener
    }

    private fun notifyChanged() = onEdt { listeners.forEach { it.invoke() } }

    public fun activeTasks(): List<TrackedTranscriptionTask> =
        (0 until model.size()).map { model.getElementAt(it) }.filter { !it.complete }

    public fun submit(
        name: String,
        block: ProgressScope.() -> Unit,
    ) {
        enqueue(
            object : BackgroundTask(name) {
                override fun runTask() {
                    ProgressScope(this).block()
                    if (!isCanceled()) report(100, "Done")
                }
            },
        )
    }

    public fun enqueue(task: BackgroundTask) {
        val tracked = TrackedTranscriptionTask(task, pool, ::notifyChanged)
        model.addElement(tracked)
        notifyChanged()
        tracked.start()
    }

    public fun clearFinished() {
        val toRemove = mutableListOf<Int>()
        for (i in 0 until model.size()) if (model.get(i).complete) toRemove += i
        toRemove.asReversed().forEach { model.remove(it) }
        notifyChanged()
    }
}

public class ProgressScope internal constructor(
    private val task: BackgroundTask,
) {
    public fun indeterminate(note: String? = null) {
        task.indeterminate(note)
    }

    public fun report(
        percent: Int,
        note: String? = null,
    ) {
        task.report(percent, note)
    }

    public fun isCancelled(): Boolean = task.isCanceled()
}

public abstract class BackgroundTask(
    public val displayName: String,
) {
    @Volatile private var canceled = false
    private var reporter: TranscriptionProgressReporter? = null

    internal fun attachReporter(r: TranscriptionProgressReporter) {
        reporter = r
    }

    public fun requestCancel() {
        canceled = true
    }

    internal fun isCanceled(): Boolean = canceled

    internal fun indeterminate(note: String? = null) {
        reporter?.indeterminate(note)
    }

    internal fun report(
        percent: Int,
        note: String? = null,
    ) {
        reporter?.report(percent.coerceIn(0, 100), note)
    }

    public abstract fun runTask()
}

public interface TranscriptionProgressReporter {
    public fun indeterminate(note: String? = null)

    public fun report(
        percent: Int,
        note: String? = null,
    )
}

public class TrackedTranscriptionTask(
    private val task: BackgroundTask,
    private val pool: ForkJoinPool,
    private val notifyChanged: () -> Unit,
) {
    public val name: String get() = task.displayName

    @Suppress("ktlint:standard:backing-property-naming")
    @Volatile
    private var _progress = 0

    @Volatile private var _note: String = "Queued"

    @Volatile private var _indeterminate = true

    @Volatile private var _error: Throwable? = null

    @Volatile private var _startedAt: Instant? = null

    @Volatile private var _finishedAt: Instant? = null

    public val progressValue: Int get() = _progress
    public val note: String get() = _note
    public val indeterminate: Boolean get() = _indeterminate
    public val error: Throwable? get() = _error
    public val startedAt: Instant? get() = _startedAt
    public val finishedAt: Instant? get() = _finishedAt
    public val complete: Boolean get() = _finishedAt != null

    @Volatile private var future: Future<*>? = null

    public fun start() {
        _startedAt = Instant.now()
        onEdt { notifyChanged() }

        task.attachReporter(
            object : TranscriptionProgressReporter {
                override fun indeterminate(note: String?) {
                    _indeterminate = true
                    if (note != null) _note = note
                    onEdt { notifyChanged() }
                }

                override fun report(
                    percent: Int,
                    note: String?,
                ) {
                    _indeterminate = false
                    _progress = percent.coerceIn(0, 100)
                    if (note != null) _note = note
                    onEdt { notifyChanged() }
                }
            },
        )

        future =
            pool.submit {
                try {
                    task.runTask()
                } catch (t: Throwable) {
                    _error = t
                } finally {
                    _finishedAt = Instant.now()
                    onEdt { notifyChanged() }
                }
            }
    }

    public fun cancelTask() {
        task.requestCancel()
        future?.cancel(true)
        _indeterminate = false
        _note = "Canceled"
        _finishedAt = Instant.now()
        onEdt { notifyChanged() }
    }
}

public class ReadableProgressBar : JProgressBar() {
    public var overlayText: String = ""
        set(value) {
            field = value
            repaint()
        }

    init {
        isStringPainted = true
        string = ""
        border = EmptyBorder(2, 6, 2, 6)
        putClientProperty("JProgressBar.paintBorder", false)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    override fun paint(g: Graphics) {
        val old = string
        string = ""
        super.paint(g)
        string = old

        if (overlayText.isEmpty()) return
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            )
            g2.font = UIManager.getFont("Label.font") ?: font
            val fm = g2.fontMetrics
            val x = (width - fm.stringWidth(overlayText)) / 2
            val y = (height - fm.height) / 2 + fm.ascent
            g2.color = Color(0, 0, 0, 140)
            g2.drawString(overlayText, x + 1, y + 1)
            g2.color = UIManager.getColor("Label.foreground") ?: foreground
            g2.drawString(overlayText, x, y)
        } finally {
            g2.dispose()
        }
    }
}

public class TranscriptionStatusBarPanel(
    private val manager: TranscriptionManager,
) : JPanel(BorderLayout()) {
    private val progressBar = ReadableProgressBar()

    private var transcriptsDialog: TranscriptsDialog? = null

    init {
        border = EmptyBorder(6, 8, 6, 8)
        val left = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply { add(progressBar) }
        add(left, BorderLayout.WEST)

        progressBar.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (!progressBar.isVisible) return
                    openTasksDialog()
                }
            },
        )
        val im = progressBar.getInputMap(WHEN_FOCUSED)
        val am = progressBar.actionMap
        im.put(KeyStroke.getKeyStroke("ENTER"), "openTasks")
        im.put(KeyStroke.getKeyStroke("SPACE"), "openTasks")
        am.put(
            "openTasks",
            object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent?) {
                    if (progressBar.isVisible) openTasksDialog()
                }
            },
        )

        manager.onChange { onEdt { refresh() } }
        refresh()
    }

    private fun openTasksDialog() {
        val owner = SwingUtilities.getWindowAncestor(this)
        if (transcriptsDialog?.isDisplayable == true) {
            transcriptsDialog!!.toFront()
            transcriptsDialog!!.isVisible = true
            return
        }
        transcriptsDialog =
            TranscriptsDialog(owner, manager).apply {
                setLocationRelativeTo(owner)
                isVisible = true
            }
    }

    private fun refresh() {
        val hasEntries = manager.model.size() > 0
        val active = manager.activeTasks()
        val anyActive = active.isNotEmpty()
        this.isVisible = hasEntries
        progressBar.isVisible = hasEntries
        progressBar.toolTipText =
            when {
                anyActive -> "Click to view transcription progress"
                hasEntries -> "No active transcripts — click to view history"
                else -> null
            }

        if (!hasEntries) {
            return
        }

        if (!anyActive) {
            progressBar.isIndeterminate = false
            progressBar.value = 0
            progressBar.overlayText = "No active transcripts"
            progressBar.repaint()
            return
        }

        val anyIndeterminate = active.any { it.indeterminate }
        progressBar.isIndeterminate = anyIndeterminate
        if (anyIndeterminate) {
            progressBar.overlayText = "${active.size} transcribing…"
        } else {
            val avg = active.map { it.progressValue }.average().toInt()
            progressBar.value = avg
            progressBar.overlayText = "$avg% — ${active.size} transcribing…"
        }
        progressBar.repaint()

        revalidate()
        repaint()
    }
}

private data class ActionsHitAreas(
    val cancel: Rectangle?,
    val remove: Rectangle?,
)

private fun actionsHitAreas(
    table: JTable,
    viewRow: Int,
    viewCol: Int,
): ActionsHitAreas {
    val cellRect = table.getCellRect(viewRow, viewCol, true)
    val renderer = table.getCellRenderer(viewRow, viewCol)
    val value = table.getValueAt(viewRow, viewCol)
    val isSelected = table.isRowSelected(viewRow)
    val hasFocus = table.hasFocus() && table.selectionModel.leadSelectionIndex == viewRow

    val comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, viewRow, viewCol)
    if (comp !is JPanel) return ActionsHitAreas(null, null)

    comp.setBounds(0, 0, cellRect.width, cellRect.height)
    comp.doLayout()

    var cancelRect: Rectangle? = null
    var removeRect: Rectangle? = null
    for (c in comp.components) {
        if (c !is JButton || !c.isVisible) continue
        val r = Rectangle(c.bounds)
        r.translate(cellRect.x, cellRect.y)
        if (c.text.equals("Cancel", ignoreCase = true)) {
            cancelRect = r
        } else if (c.text.equals("Remove", ignoreCase = true)) {
            removeRect = r
        }
    }
    return ActionsHitAreas(cancelRect, removeRect)
}

private fun isOverAnyAction(
    table: JTable,
    viewRow: Int,
    viewCol: Int,
    p: Point,
): Boolean {
    val areas = actionsHitAreas(table, viewRow, viewCol)
    return (areas.cancel?.contains(p) == true) || (areas.remove?.contains(p) == true)
}

private class TranscriptsDialog(
    owner: Window?,
    private val manager: TranscriptionManager,
) : JDialog(owner, "Transcripts", ModalityType.MODELESS) {
    private val tableModel = TranscriptTableModel(manager)
    private val table =
        object : JTable(tableModel) {
            override fun changeSelection(
                rowIndex: Int,
                columnIndex: Int,
                toggle: Boolean,
                extend: Boolean,
            ) {
                val modelCol = convertColumnIndexToModel(columnIndex)
                if (modelCol == TranscriptTableModel.COL_ACTIONS) {
                    val mouseP = mousePosition
                    if (mouseP != null && !isOverAnyAction(this, rowIndex, columnIndex, mouseP)) {
                        return
                    }
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend)
            }
        }.apply {
            defaultCloseOperation = HIDE_ON_CLOSE
            putClientProperty("JTable.showVerticalLines", false)
            putClientProperty("JTable.showHorizontalLines", true)
            intercellSpacing = Dimension(0, 1)
            rowHeight = 28
            autoCreateRowSorter = true
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            columnModel.getColumn(TranscriptTableModel.COL_PROGRESS).cellRenderer = ProgressBarCellRenderer()
            val actionCol = columnModel.getColumn(TranscriptTableModel.COL_ACTIONS)
            actionCol.cellRenderer = ActionsRenderer()
            actionCol.cellEditor = ActionsEditor(manager, this)
            columnModel.getColumn(TranscriptTableModel.COL_NAME).apply {
                preferredWidth = 175
                minWidth = 150
            }
            columnModel.getColumn(TranscriptTableModel.COL_STATUS).apply {
                preferredWidth = 150
            }
            columnModel.getColumn(TranscriptTableModel.COL_PROGRESS).apply {
                preferredWidth = 120
                maxWidth = 160
            }
            columnModel.getColumn(TranscriptTableModel.COL_STARTED).apply {
                preferredWidth = 140
                maxWidth = 160
            }
            columnModel.getColumn(TranscriptTableModel.COL_DURATION).apply {
                preferredWidth = 80
                maxWidth = 100
            }
            columnModel.getColumn(TranscriptTableModel.COL_ACTIONS).apply {
                preferredWidth = 140
                maxWidth = 160
            }

            addMouseListener(
                object : MouseAdapter() {
                    override fun mousePressed(e: MouseEvent) {
                        maybeMenu(e)
                    }

                    override fun mouseReleased(e: MouseEvent) {
                        maybeMenu(e)
                    }

                    private fun maybeMenu(e: MouseEvent) {
                        if (!e.isPopupTrigger) return
                        val row = rowAtPoint(e.point)
                        if (row < 0) return
                        setRowSelectionInterval(row, row)
                        val tracked = tableModel.getAt(convertRowIndexToModel(row))
                        JPopupMenu()
                            .apply {
                                add(
                                    JMenuItem("Cancel").apply {
                                        isEnabled = !tracked.complete
                                        addActionListener { tracked.cancelTask() }
                                    },
                                )
                                add(
                                    JMenuItem("Remove").apply {
                                        addActionListener {
                                            val idx = manager.model.indexOf(tracked)
                                            if (idx >= 0) manager.model.remove(idx)
                                            tableModel.fireTableDataChanged()
                                        }
                                    },
                                )
                            }.show(this@apply, e.x, e.y)
                    }
                },
            )
        }

    fun centerTableColumns(
        table: JTable,
        vararg modelCols: Int,
    ) {
        val renderer =
            DefaultTableCellRenderer().apply {
                horizontalAlignment = SwingConstants.CENTER
            }
        val cm = table.columnModel
        for (mc in modelCols) {
            val vc = table.convertColumnIndexToView(mc)
            if (vc >= 0) {
                cm.getColumn(vc).cellRenderer = renderer
            }
        }
    }

    private val btnClear =
        JButton("Clear Finished").apply {
            addActionListener {
                manager.clearFinished()
                tableModel.fireTableDataChanged()
            }
        }

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        contentPane =
            JPanel(BorderLayout()).apply {
                border = EmptyBorder(10, 10, 10, 10)
                add(JScrollPane(table), BorderLayout.CENTER)
                add(JPanel(FlowLayout(FlowLayout.RIGHT)).apply { add(btnClear) }, BorderLayout.SOUTH)
            }
        minimumSize = Dimension(700, 300)
        installActionsHover(table, TranscriptTableModel.COL_ACTIONS)
        table.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    val row = table.rowAtPoint(e.point)
                    val col = table.columnAtPoint(e.point)
                    if (row < 0 || col < 0) return

                    val modelCol = table.convertColumnIndexToModel(col)
                    if (modelCol == TranscriptTableModel.COL_ACTIONS) {
                        val rect = table.getCellRect(row, col, true)
                        val editor = table.getCellEditor(row, col) as? ActionsEditor ?: return
                        val inside = editor.isClickInsideButton(e.x - rect.x, e.y - rect.y)

                        if (!inside) {
                            e.consume()
                            table.clearSelection()
                        }
                    }
                }
            },
        )
        setSize(900, 420)
        setLocationRelativeTo(owner)

        manager.onChange { tableModel.fireTableDataChanged() }
        centerTableColumns(
            table,
            TranscriptTableModel.COL_NAME,
            TranscriptTableModel.COL_STATUS,
            TranscriptTableModel.COL_STARTED,
            TranscriptTableModel.COL_DURATION,
        )

        val tick =
            Timer(500) {
                table.repaint()
            }.apply { start() }
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosed(e: WindowEvent) {
                    tick.stop()
                }

                override fun windowClosing(e: WindowEvent) {
                    tick.stop()
                }
            },
        )

        rootPane.registerKeyboardAction(
            {
                if (table.isEditing) {
                    table.cellEditor.cancelCellEditing()
                } else {
                    this.isVisible = false
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW,
        )
    }
}

private class TranscriptTableModel(
    private val manager: TranscriptionManager,
) : AbstractTableModel() {
    companion object {
        const val COL_NAME = 0
        const val COL_STATUS = 1
        const val COL_PROGRESS = 2
        const val COL_STARTED = 3
        const val COL_DURATION = 4
        const val COL_ACTIONS = 5

        private val COLUMNS = arrayOf("Name", "Status", "Progress", "Started", "Duration", "Actions")
    }

    override fun getRowCount(): Int = manager.model.size()

    override fun getColumnCount(): Int = COLUMNS.size

    override fun getColumnName(column: Int): String = COLUMNS[column]

    override fun getColumnClass(columnIndex: Int): Class<*> =
        when (columnIndex) {
            COL_PROGRESS -> Int::class.java
            else -> String::class.java
        }

    override fun isCellEditable(
        rowIndex: Int,
        columnIndex: Int,
    ): Boolean = columnIndex == COL_ACTIONS

    fun getAt(modelRow: Int): TrackedTranscriptionTask = manager.model.get(modelRow)

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun formatStartedAt(instant: Instant?): String {
        if (instant == null) return "—"
        val zoned = instant.atZone(ZoneId.systemDefault())
        val today = LocalDate.now()
        return if (zoned.toLocalDate() == today) {
            zoned.format(timeFormatter)
        } else {
            zoned.format(dateTimeFormatter)
        }
    }

    override fun getValueAt(
        rowIndex: Int,
        columnIndex: Int,
    ): Any {
        val t = manager.model.get(rowIndex)
        return when (columnIndex) {
            COL_NAME -> t.name
            COL_STATUS -> statusText(t)
            COL_PROGRESS -> t.progressValue
            COL_STARTED -> formatStartedAt(t.startedAt)
            COL_DURATION -> durationText(t)
            COL_ACTIONS -> "Actions"
            else -> ""
        }
    }

    private fun statusText(t: TrackedTranscriptionTask): String =
        when {
            t.error != null -> "Error"
            t.complete -> "Finished"
            t.indeterminate -> t.note.ifBlank { "Working…" }
            else -> "${t.progressValue}% — ${t.note}"
        }

    private fun durationText(t: TrackedTranscriptionTask): String {
        val s = t.startedAt ?: return "—"
        val end = t.finishedAt ?: Instant.now()
        val d = Duration.between(s, end)
        val ms = d.toMillis()
        return when {
            ms < 1000 -> "$ms ms"
            ms < 60_000 -> "${ms / 1000}s"
            else -> "%d:%02d".format(d.toMinutes(), d.seconds % 60)
        }
    }
}

private class ProgressBarCellRenderer :
    JProgressBar(0, 100),
    TableCellRenderer {
    init {
        isStringPainted = true
        isOpaque = false
        border = EmptyBorder(2, 6, 2, 6)
        putClientProperty("JProgressBar.paintBorder", false)
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val model = table.model as TranscriptTableModel
        val tracked = model.getAt(table.convertRowIndexToModel(row))

        isIndeterminate = tracked.indeterminate
        if (isIndeterminate) {
            string = tracked.note.ifBlank { "Working…" }
        } else {
            val pct = (value as? Int) ?: 0
            this.value = pct
            string = "$pct%"
        }
        return this
    }
}

private fun JButton.asTableAction(): JButton =
    apply {
        isFocusable = false
        margin = Insets(2, 8, 2, 8)
        putClientProperty("JButton.buttonType", "roundRect")
    }

private fun installActionsHover(
    table: JTable,
    @Suppress("SameParameterValue") actionsModelCol: Int,
) {
    val keyRow = "__hoverRow"
    val keyCol = "__hoverCol"

    fun setHover(
        r: Int,
        c: Int,
    ) {
        val oldR = (table.getClientProperty(keyRow) as? Int) ?: -1
        val oldC = (table.getClientProperty(keyCol) as? Int) ?: -1
        if (oldR == r && oldC == c) return
        table.putClientProperty(keyRow, r)
        table.putClientProperty(keyCol, c)
        if (oldR >= 0 && oldC >= 0) table.repaint(table.getCellRect(oldR, oldC, false))
        if (r >= 0 && c >= 0) table.repaint(table.getCellRect(r, c, false))
    }

    table.addMouseMotionListener(
        object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val viewRow = table.rowAtPoint(e.point)
                val viewCol = table.columnAtPoint(e.point)
                if (viewRow >= 0 && viewCol >= 0 &&
                    table.convertColumnIndexToModel(viewCol) == actionsModelCol &&
                    isOverAnyAction(table, viewRow, viewCol, e.point)
                ) {
                    table.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                } else {
                    table.cursor = Cursor.getDefaultCursor()
                }
                table.repaint()
            }
        },
    )
    table.addMouseListener(
        object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent) = setHover(-1, -1)
        },
    )
}

private fun isActionsCellHovered(
    table: JTable,
    viewRow: Int,
    viewCol: Int,
): Boolean {
    val r = (table.getClientProperty("__hoverRow") as? Int) ?: -1
    val c = (table.getClientProperty("__hoverCol") as? Int) ?: -1
    return r == viewRow && c == viewCol
}

private class ActionsRenderer :
    JPanel(FlowLayout(FlowLayout.CENTER, 6, 0)),
    TableCellRenderer {
    private val btnCancel = JButton("Cancel").asTableAction()
    private val btnRemove = JButton("Remove").asTableAction()

    init {
        isOpaque = false
        add(btnCancel)
        add(btnRemove)
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val model = table.model as TranscriptTableModel
        val tracked = model.getAt(table.convertRowIndexToModel(row))
        val done = tracked.complete

        btnCancel.isVisible = !done
        btnCancel.isEnabled = !done
        btnRemove.isVisible = true
        btnRemove.isEnabled = true
        val hovered = isActionsCellHovered(table, row, column)
        btnCancel.model.isRollover = hovered
        btnRemove.model.isRollover = hovered
        revalidate()
        return this
    }
}

private class ActionsEditor(
    private val manager: TranscriptionManager,
    private val table: JTable,
) : AbstractCellEditor(),
    TableCellEditor {
    private val panel = JPanel(FlowLayout(FlowLayout.CENTER, 6, 0)).apply { isOpaque = false }
    private val btnCancel = JButton("Cancel").asTableAction()
    private val btnRemove = JButton("Remove").asTableAction()
    private var currentRow = -1

    init {
        btnCancel.addActionListener {
            currentTask()?.let { t -> if (!t.complete) t.cancelTask() }
            stopCellEditing()
            (table.model as TranscriptTableModel).fireTableRowsUpdated(currentRow, currentRow)
        }
        btnRemove.addActionListener {
            currentTask()?.let { t ->
                val idx = manager.model.indexOf(t)
                if (idx >= 0) manager.model.remove(idx)
            }
            stopCellEditing()
            (table.model as TranscriptTableModel).fireTableDataChanged()
        }
    }

    private fun currentTask(): TrackedTranscriptionTask? {
        val model = table.model as TranscriptTableModel
        val modelRow = table.convertRowIndexToModel(currentRow)
        return if (modelRow in 0 until model.rowCount) model.getAt(modelRow) else null
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int,
    ): Component {
        currentRow = row
        val tracked = (table.model as TranscriptTableModel).getAt(table.convertRowIndexToModel(row))

        panel.removeAll()
        if (!tracked.complete) {
            panel.add(btnCancel)
        }
        panel.add(btnRemove)
        panel.revalidate()
        panel.repaint()

        return panel
    }

    override fun getCellEditorValue(): Any = "Actions"

    fun isClickInsideButton(
        x: Int,
        y: Int,
    ): Boolean {
        for (comp in panel.components) {
            if (comp.isVisible && comp.bounds.contains(x, y)) return true
        }
        return false
    }
}
