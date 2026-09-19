package net.rsprox.shared.clientscript

import net.rsprox.cache.api.type.ClientScriptDefinition
import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType

/** Transcript hints only: never change wire decoding, and reject the entire hint if any argument disagrees. */
public object ClientScriptTypes {
    private val typesByName = ScriptVarType.entries.associateBy(ScriptVarType::fullName)
    private val typesByChar = ScriptVarType.entries.associateBy(ScriptVarType::char)

    public fun infer(
        definition: ClientScriptDefinition?,
        wireTypes: CharArray,
        values: List<Any>,
    ): CharArray {
        if (definition == null || definition.arguments.size != wireTypes.size || wireTypes.size != values.size) {
            return wireTypes
        }
        val inferred = CharArray(wireTypes.size)
        for (index in inferred.indices) {
            val char =
                when (val type = definition.arguments[index].type) {
                    "intarray" -> 'W'
                    "stringarray" -> 'X'
                    else -> typesByName[type]?.char ?: return wireTypes
                }
            if (!isCompatible(char, values[index])) return wireTypes
            inferred[index] = char
        }
        return inferred
    }

    private fun isCompatible(
        char: Char,
        value: Any,
    ): Boolean {
        if (char == 'W') return value is IntArray
        if (char == 'X') return value is Array<*> && value.all { it is String }
        return when (typesByChar[char]?.baseVarType) {
            BaseVarType.INTEGER -> value is Int
            BaseVarType.LONG -> value is Long
            BaseVarType.STRING -> value is String
            null -> false
        }
    }
}
