package net.rsprox.transcriber.rs3.state

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.*

internal class Rs3TelemetryGrid {
    private data class Row(val id: Int, var pin: Int = -1)

    private data class Group(
        val id: Int,
        val rows: MutableList<Row> = mutableListOf(),
        val columns: MutableList<Int> = mutableListOf(),
    )

    private var groups: MutableList<Group>? = mutableListOf()

    fun groupId(index: Int): Int? = groups?.getOrNull(index)?.id

    fun rowId(group: Int, index: Int): Int? = groups?.getOrNull(group)?.rows?.getOrNull(index)?.id

    fun columnId(group: Int, index: Int): Int? = groups?.getOrNull(group)?.columns?.getOrNull(index)

    fun update(message: IncomingMessage) {
        if (message is TelemetryGridFull) {
            groups = message.groups.mapTo(mutableListOf()) { group ->
                Group(
                    group.id,
                    group.rowIds.mapIndexedTo(mutableListOf()) { index, id -> Row(id, group.rows[index].pin) },
                    group.columnIds.toMutableList(),
                )
            }
            return
        }
        val groups = groups ?: return
        val valid = when (message) {
            is TelemetryGridAddGroup -> {
                if (groups.size == 5 || groups.any { it.id == message.id }) {
                    true
                } else {
                    groups.insert(message.index, Group(message.id))
                }
            }
            is TelemetryGridRemoveGroup -> groups.removeIndex(message.group)
            is TelemetryGridAddColumn -> groups.getOrNull(message.group)?.let { group ->
                if (group.columns.size == 8 || message.id in group.columns) true
                else group.columns.insert(message.index, message.id)
            } ?: false
            is TelemetryGridRemoveColumn -> groups.getOrNull(message.group)?.columns?.removeIndex(message.column) ?: false
            is TelemetryGridMoveColumn ->
                groups.getOrNull(message.group)?.columns?.move(message.source, message.destination) ?: false
            is TelemetryGridAddRow -> groups.getOrNull(message.group)?.let { group ->
                val rows = group.rows
                val index = if (message.index == 255 || message.index == -1) rows.size else message.index
                when {
                    rows.size == 40 || rows.any { it.id == message.id } -> true
                    !rows.insert(index, Row(message.id)) -> false
                    else -> {
                        // Keep pinned rows in their slots when inserting before them.
                        for (i in index + 1 until rows.size) {
                            if (rows[i].pin != -1 && rows[i].pin < i) rows.move(i, i - 1)
                        }
                        true
                    }
                }
            } ?: false
            is TelemetryGridRemoveRow -> groups.getOrNull(message.group)?.let { group ->
                val rows = group.rows
                if (!rows.removeIndex(message.row)) {
                    false
                } else {
                    var destination = message.row
                    for (i in message.row until rows.size) {
                        if (rows[i].pin == -1) {
                            rows.move(i, destination)
                            destination = i + 1
                        }
                    }
                    true
                }
            } ?: false
            is TelemetryGridMoveRow -> groups.getOrNull(message.group)?.let { group ->
                if (!group.rows.move(message.source, message.destination)) {
                    false
                } else {
                    group.rows.forEachIndexed { index, row -> if (row.pin != -1) row.pin = index }
                    true
                }
            } ?: false
            is TelemetryGridSetRowPinned -> groups.getOrNull(message.group)?.rows?.getOrNull(message.row)?.let { row ->
                row.pin = if (message.enabled) message.row else -1
                true
            } ?: false
            else -> true
        }
        // Unknown ordering: use numeric indices until the next full grid.
        if (!valid) this.groups = null
    }

    private fun <T> MutableList<T>.insert(index: Int, value: T): Boolean {
        val position = if (index == 255 || index == -1) size else index
        if (position !in 0..size) return false
        add(position, value)
        return true
    }

    private fun <T> MutableList<T>.removeIndex(index: Int): Boolean {
        if (index !in indices) return false
        removeAt(index)
        return true
    }

    private fun <T> MutableList<T>.move(source: Int, destination: Int): Boolean {
        if (source !in indices || destination !in indices) return false
        add(destination, removeAt(source))
        return true
    }
}
