package net.rsprox.transcriber.rs3.text

import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.common.QuickChat
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.varc
import net.rsprox.shared.property.varp

internal fun Property.appendQuickChat(
    phrase: Int,
    chat: QuickChat,
) {
    scriptVarType("phrase", ScriptVarType.CHATPHRASE, phrase)
    string("template", chat.template)
    if (chat.parameters.isNotEmpty()) {
        group("parameters") {
            for (parameter in chat.parameters) {
                group {
                    int("command", parameter.command)
                    parameter.value?.let { value ->
                        if (parameter.command == 1 || parameter.command == 10) {
                            scriptVarType("obj", ScriptVarType.OBJ, value.toInt())
                        } else {
                            long("value", value)
                        }
                    }
                    if (parameter.arguments.isNotEmpty()) {
                        group("definitionArgs") {
                            parameter.arguments.forEachIndexed { index, value -> int("arg$index", value) }
                        }
                    }
                }
            }
        }
    }
}

internal fun Property.appendVariables(
    variables: List<TypedVariable>,
    domain: Rs3VariableDomain? = null,
) {
    group("variables") {
        for (variable in variables) group { appendVariable(variable, domain) }
    }
}

internal fun Property.appendVariable(
    variable: TypedVariable,
    domain: Rs3VariableDomain? = null,
) {
    when (domain) {
        Rs3VariableDomain.PLAYER -> varp("varp", variable.id)
        Rs3VariableDomain.CLIENT -> varc("varc", variable.id)
        else -> int("id", variable.id)
    }
    int("scriptType", variable.scriptType)
    appendVariableValue(variable.value)
}

internal fun Property.appendVariableValue(value: TypedVariable.Value) {
    when (value) {
        is TypedVariable.IntValue -> int("value", value.value)
        is TypedVariable.LongValue -> long("value", value.value)
        is TypedVariable.StringValue -> string("value", value.value)
        is TypedVariable.Coordinate ->
            group("value") {
                int("level", value.plane)
                int("x", value.x)
                int("y", value.y)
                int("z", value.z)
            }
    }
}
