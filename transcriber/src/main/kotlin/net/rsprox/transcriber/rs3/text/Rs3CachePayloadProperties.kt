package net.rsprox.transcriber.rs3.text

import net.rsprox.cache.api.rs3.Rs3BaseVarType
import net.rsprox.cache.api.rs3.Rs3Gameval
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.rs3.common.QuickChat
import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.shared.BaseVarType
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
    domain: Rs3VariableDomain,
) {
    group("variables") {
        for (variable in variables) group { appendVariable(variable, domain) }
    }
}

internal fun Property.appendVariable(
    variable: TypedVariable,
    domain: Rs3VariableDomain,
) {
    when (domain) {
        Rs3VariableDomain.PLAYER -> varp("varp", variable.id)
        Rs3VariableDomain.CLIENT -> varc("varc", variable.id)
        Rs3VariableDomain.CLAN -> gameval("id", Rs3Gameval.VAR_CLAN, variable.id)
        Rs3VariableDomain.PLAYER_GROUP -> gameval("id", Rs3Gameval.VAR_PLAYER_GROUP, variable.id)
    }
    int("scriptType", variable.scriptType)
    appendVariableValue(variable.scriptType, variable.value)
}

private val sharedIntTypes = ScriptVarType.entries
    .filter { it.id in 0..116 && it.baseVarType == BaseVarType.INTEGER }.associateBy { it.id }

internal fun Property.appendVariableInt(
    definitions: Rs3PacketDefinitions?,
    domain: Rs3VariableDomain,
    id: Int,
    value: Int,
) {
    val definition = definitions?.getVariableOrNull(domain, id)
    val scriptType = definition?.takeIf { it.baseType == Rs3BaseVarType.INT }?.scriptType
    appendVariableInt(scriptType, value)
}

private fun Property.appendVariableInt(scriptType: Int?, value: Int) {
    when (scriptType) {
        129 -> scriptVarType("value", ScriptVarType.DBTABLE, value)
        131 -> gameval("value", Rs3Gameval.ACHIEVEMENT, value)
        133 -> gameval("value", Rs3Gameval.STYLESHEET, value)
        135 -> gameval("value", Rs3Gameval.UI_ANIM_CURVE, value)
        136 -> gameval("value", Rs3Gameval.UI_ANIM, value)
        else -> {
            val type = sharedIntTypes[scriptType] ?: ScriptVarType.INT
            if (type == ScriptVarType.BOOLEAN && value !in 0..1) int("value", value) else scriptVarType("value", type, value)
        }
    }
}

internal fun Property.appendVariableValue(scriptType: Int, value: TypedVariable.Value) {
    when (value) {
        is TypedVariable.IntValue -> appendVariableInt(scriptType, value.value)
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
