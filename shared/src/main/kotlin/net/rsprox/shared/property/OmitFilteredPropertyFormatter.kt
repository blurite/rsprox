package net.rsprox.shared.property

import net.rsprox.shared.property.regular.GroupProperty

public class OmitFilteredPropertyFormatter : PropertyFormatter {
    override fun format(property: RootProperty<*>): List<String> {
        val builder = StringBuilder()
        builder.append('[').append(property.prot).append("] ")
        for (child in property.children) {
            writeChild(child, builder, 1)
        }
        return builder.lines()
    }

    private fun writeChild(
        property: ChildProperty<*>,
        builder: StringBuilder,
        indent: Int,
    ) {
        if (property is FilteredProperty<*>) {
            if (property.value == property.filterValue) {
                return
            }
        }
        if (property is GroupProperty) {
            val hasLabel = property.propertyName.isNotEmpty()
            if (hasLabel) {
                builder.appendLine()
                builder.append(INDENTATION.repeat(indent))
                builder.append(property.propertyName)
            }
            val childIndent = if (hasLabel) (indent + 1) else indent
            if (property.children.isNotEmpty()) {
                builder.appendLine().append(INDENTATION.repeat(childIndent))
                for (child in property.children) {
                    writeChild(child, builder, childIndent)
                }
            }
            return
        }
        val lastLine = builder.lineSequence().lastOrNull()
        val needsSeparator = !lastLine.isNullOrEmpty() && lastLine.last() != ' '
        if (needsSeparator) {
            builder.append(", ")
        }
        builder
            .append(property.propertyName)
            .append('=')
            .append(property.value)
        if (property.children.isNotEmpty()) {
            builder.appendLine()
            builder.append(INDENTATION.repeat(indent + 1))
            for (child in property.children) {
                writeChild(child, builder, indent + 1)
            }
        }
    }

    private companion object {
        private const val INDENTATION: String = "    "
    }
}
