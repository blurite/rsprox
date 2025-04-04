package net.rsprox.shared.property

import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.ListProperty

public class OmitFilteredPropertyTreeFormatter(
    public val propertyFormatterCollection: PropertyFormatterCollection,
) : PropertyTreeFormatter {
    override fun format(property: RootProperty): List<String> {
        val lines = ArrayList<String>()
        val builder = StringBuilder()
        builder.append('[').append(property.prot.lowercase()).append("] ")
        var count = 0
        for (child in property.children) {
            if (child.isExcluded()) {
                continue
            }
            writeChild(child, builder, lines, 1, if (count++ == 0) null else SEPARATOR)
        }
        lines.add(builder.toString())
        return lines
    }

    public fun writeChild(
        property: ChildProperty<*>,
        builder: StringBuilder,
        lines: MutableList<String>,
        indent: Int,
        linePrefix: String?,
    ) {
        if (property is GroupProperty) {
            val hasLabel = property.propertyName.isNotEmpty()
            if (hasLabel) {
                lines.add(builder.toString())
                builder.clear()
                builder.append(INDENTATION.repeat(indent))
                builder.append('[').append(property.propertyName.lowercase()).append(']')
            }
            val childIndent = if (hasLabel) (indent + 1) else indent
            if (property.children.isNotEmpty()) {
                if (!hasLabel) {
                    lines.add(builder.toString())
                    builder.clear()
                    builder.append(INDENTATION.repeat(childIndent))
                }
                var count = 0
                for (child in property.children) {
                    if (child.isExcluded()) {
                        continue
                    }
                    val prefix =
                        if (count++ == 0) {
                            if (hasLabel) {
                                " "
                            } else {
                                PREFIX
                            }
                        } else {
                            SEPARATOR
                        }
                    writeChild(child, builder, lines, childIndent, prefix)
                }
            }
            return
        }
        if (property is ListProperty) {
            if (linePrefix != null) {
                builder.append(linePrefix)
            }
            builder
                .append(property.propertyName)
                .append('=')
                .append('[')
            var count = 0
            for (child in property.children) {
                if (child.isExcluded()) {
                    continue
                }
                val prefix =
                    if (count++ == 0) {
                        ""
                    } else {
                        SEPARATOR
                    }
                writeChild(child, builder, lines, indent, prefix)
            }
            builder.append(']')
            return
        }
        if (linePrefix != null) {
            builder.append(linePrefix)
        }
        val formatter = propertyFormatterCollection.getTypedFormatter(property.javaClass)
        val value = formatter?.format(property) ?: property.value
        if (property.propertyName.isNotEmpty()) {
            builder
                .append(property.propertyName)
                .append('=')
        }
        builder.append(value)
        if (property.children.isNotEmpty()) {
            lines.add(builder.toString())
            builder.clear()
            builder.append(INDENTATION.repeat(indent + 1))
            var count = 0
            for (child in property.children) {
                if (child.isExcluded()) {
                    continue
                }
                writeChild(child, builder, lines, indent + 1, if (count++ == 0) null else SEPARATOR)
            }
        }
    }

    private companion object {
        private const val INDENTATION: String = "    "
        private const val PREFIX: String = "- "
        private const val SEPARATOR: String = ", "
    }
}
