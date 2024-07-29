package net.rsprox.shared.property

import net.rsprox.shared.property.regular.GroupProperty

public class OmitFilteredPropertyTreeFormatter : PropertyTreeFormatter {
    override fun format(property: RootProperty<*>): List<String> {
        val builder = StringBuilder()
        builder.append('[').append(property.prot).append("] ")
        var count = 0
        for (child in property.children) {
            if (child.isExcluded()) {
                continue
            }
            writeChild(child, builder, 1, if (count++ == 0) null else SEPARATOR)
        }
        return builder.lines()
    }

    private fun ChildProperty<*>.isExcluded(): Boolean {
        if (this !is FilteredProperty<*>) {
            return false
        }
        return value == filterValue
    }

    private fun writeChild(
        property: ChildProperty<*>,
        builder: StringBuilder,
        indent: Int,
        linePrefix: String?,
    ) {
        if (property is GroupProperty) {
            val hasLabel = property.propertyName.isNotEmpty()
            if (hasLabel) {
                builder.appendLine()
                builder.append(INDENTATION.repeat(indent))
                builder.append('[').append(property.propertyName).append(']')
            }
            val childIndent = if (hasLabel) (indent + 1) else indent
            if (property.children.isNotEmpty()) {
                if (!hasLabel) {
                    builder.appendLine().append(INDENTATION.repeat(childIndent))
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
                    writeChild(child, builder, childIndent, prefix)
                }
            }
            return
        }
        if (linePrefix != null) {
            builder.append(linePrefix)
        }
        builder
            .append(property.propertyName)
            .append('=')
            .append(property.value)
        if (property.children.isNotEmpty()) {
            builder.appendLine()
            builder.append(INDENTATION.repeat(indent + 1))
            var count = 0
            for (child in property.children) {
                if (child.isExcluded()) {
                    continue
                }
                writeChild(child, builder, indent + 1, if (count++ == 0) null else SEPARATOR)
            }
        }
    }

    private companion object {
        private const val INDENTATION: String = "    "
        private const val PREFIX: String = "- "
        private const val SEPARATOR: String = ", "
    }
}
