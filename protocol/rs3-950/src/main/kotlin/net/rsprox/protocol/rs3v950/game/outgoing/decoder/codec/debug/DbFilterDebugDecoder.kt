package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.debug

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug.Filter
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug.RowSet
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug.Value
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class DbFilterDebugDecoder : ProxyMessageDecoder<DbFilterDebug> {
    override val prot: ClientProt = GameServerProt.DBFILTER_DEBUG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): DbFilterDebug {
        val version = buffer.g1()
        val titleMarker = buffer.g1()
        val title = if (titleMarker == 0) buffer.readNativeString() else null
        return DbFilterDebug(version, titleMarker, title, readFilter(buffer, 0))
    }

    private fun readFilter(
        buffer: JagByteBuf,
        depth: Int,
    ): Filter {
        require(depth < 128) { "DB filter nesting exceeds the proxy safety limit (128)" }
        return when (val type = buffer.gSmart1or2()) {
            1 -> {
                val rows = readRows(buffer)
                val version = buffer.g1()
                val valueType = buffer.gSmart1or2()
                val value = readValue(buffer, valueType)
                Filter.CompareRows(rows, version, valueType, value, buffer.gSmart1or2())
            }
            2 -> Filter.Unary(buffer.g1(), readFilter(buffer, depth + 1))
            3, 4 -> {
                val version = buffer.g1()
                val first = readChildren(buffer, depth)
                val second = readChildren(buffer, depth)
                Filter.Compound(type, version, first, second)
            }
            5, 6 -> {
                val version = buffer.g1()
                val valueType = buffer.gSmart1or2()
                val fieldId = buffer.g4()
                val operator = buffer.gSmart1or2()
                val operandVersion = buffer.g1()
                if (type == 5) {
                    Filter.CompareField(
                        version,
                        valueType,
                        fieldId,
                        operator,
                        operandVersion,
                        readValue(buffer, valueType),
                    )
                } else {
                    Filter.CompareFieldInteger(version, valueType, fieldId, operator, operandVersion, buffer.g4())
                }
            }
            7 -> {
                val rows = readRows(buffer)
                val version = buffer.g1()
                val valueType = buffer.gSmart1or2()
                val count = buffer.gSmart1or2()
                require(count <= buffer.readableBytes()) { "Truncated DB value set" }
                Filter.ValueSet(rows, version, valueType, List(count) { readValue(buffer, valueType) })
            }
            8 -> {
                val version = buffer.g1()
                val flags = buffer.g1()
                val value = buffer.g4()
                val marker = buffer.g1()
                Filter.Text(version, flags, value, marker, if (marker == 0) buffer.readNativeString() else null)
            }
            else -> error("Unsupported DB filter wire type $type")
        }
    }

    private fun readChildren(
        buffer: JagByteBuf,
        depth: Int,
    ): List<Filter> {
        val count = buffer.gSmart1or2()
        require(count <= buffer.readableBytes() / 2) { "Truncated DB child list" }
        return List(count) { readFilter(buffer, depth + 1) }
    }

    private fun readRows(buffer: JagByteBuf): RowSet {
        val version = buffer.g1()
        val tableId = buffer.g4()
        val count = buffer.g4()
        require(count == -1 || count in 0..buffer.readableBytes() / 4) { "Invalid DB row count $count" }
        // Preserve transmitted duplicates; native set insertion is a consumer effect, not a wire transformation.
        return RowSet(version, tableId, if (count == -1) null else List(count) { buffer.g4() })
    }

    private fun readValue(
        buffer: JagByteBuf,
        type: Int,
    ): Value =
        when (type) {
            0 -> Value.IntegerValue(buffer.g4())
            1 -> Value.LongValue(buffer.g8())
            2 -> Value.StringValue(buffer.readNativeString())
            3 -> Value.Coordinate(buffer.g1(), buffer.g4(), buffer.g4(), buffer.g4())
            else -> error("Unsupported DB value type $type")
        }
}
