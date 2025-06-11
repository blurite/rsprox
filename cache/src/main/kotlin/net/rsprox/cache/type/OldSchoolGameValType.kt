package net.rsprox.cache.type

import net.rsprot.buffer.JagByteBuf
import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import java.nio.charset.StandardCharsets

public class OldSchoolGameValType(
    override val gameVal: GameVal,
    override val id: Int,
) : GameValType {
    private val childDictionary: MutableMap<Int, String> = mutableMapOf()
    private var value: String? = null

    override fun getParentOrNull(): String? {
        return value
    }

    override fun getChildOrNull(childId: Int): String? {
        return childDictionary[childId and 0xFFFF]
    }

    public fun decode(
        @Suppress("UNUSED_PARAMETER") revision: Int,
        buffer: JagByteBuf,
    ) {
        when (gameVal) {
            GameVal.IF_TYPE -> {
                this.value = buffer.gjstr()

                var componentId = 0
                while (true) {
                    val check = buffer.g1()
                    if (check == 0xFF) {
                        // If there's nothing more to read, break early
                        if (!buffer.isReadable) break

                        // Otherwise try to see if we can read more, past the 255 limit.
                        buffer.buffer.markReaderIndex()
                        if (buffer.g2() == 0) {
                            break
                        }
                        buffer.buffer.resetReaderIndex()
                    }

                    val componentName = buffer.gjstr()
                    childDictionary[componentId++] = componentName
                }
            }
            GameVal.TABLE_TYPE -> {
                buffer.g1()
                this.value = buffer.gjstr()

                var columnId = 0
                while (true) {
                    val check = buffer.g1()
                    if (check == 0) {
                        break
                    }

                    val columnName = buffer.gjstr()
                    childDictionary[columnId++] = columnName
                }
            }
            else -> {
                val array = ByteArray(buffer.readableBytes())
                buffer.buffer.readBytes(array)
                value = String(array, StandardCharsets.UTF_8)
            }
        }
    }

    public companion object {
        public fun get(
            revision: Int,
            gameVal: GameVal,
            id: Int,
            buffer: JagByteBuf,
        ): OldSchoolGameValType {
            if (revision < 230) {
                throw IllegalStateException("Revisions below 230 don't support game vals: $revision")
            }
            val type = OldSchoolGameValType(gameVal, id)
            type.decode(revision, buffer)
            return type
        }
    }
}
