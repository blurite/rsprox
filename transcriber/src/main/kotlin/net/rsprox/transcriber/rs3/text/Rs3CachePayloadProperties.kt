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
import net.rsprox.shared.property.any
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
    valueType("scriptType", variable.scriptType)
    appendVariableValue(variable.scriptType, variable.value)
}

private val sharedTypes = ScriptVarType.entries.filter { it.id in 0..116 }.associateBy { it.id }

private enum class ExtraValueType(val id: Int, val gameval: Rs3Gameval? = null) {
    UNSUPPORTED(-1),
    COORDFINE(50),
    POINTLIGHT(117),
    PLAYER_GROUP(118),
    PLAYER_GROUP_STATUS(119),
    PLAYER_GROUP_INVITE_RESULT(120),
    PLAYER_GROUP_MODIFY_RESULT(121),
    PLAYER_GROUP_JOIN_OR_CREATE_RESULT(122),
    PLAYER_GROUP_AFFINITY_MODIFY_RESULT(123),
    PLAYER_GROUP_DELTA_TYPE(124),
    CLIENT_TYPE(125),
    TELEMETRY_INTERVAL(126),
    WORLD_AREA(127),
    DBTABLE(129, Rs3Gameval.DBTABLE),
    ACHIEVEMENT(131, Rs3Gameval.ACHIEVEMENT),
    STYLESHEET(133, Rs3Gameval.STYLESHEET),
    UI_ANIM_CURVE(135, Rs3Gameval.UI_ANIM_CURVE),
    UI_ANIM(136, Rs3Gameval.UI_ANIM),
    ANIM_STATE_MACHINE(137),
    CUTSCENE2D(138),
    COMPONENTARRAY(200),
    INTARRAY(201),
    LABEL(202),
    QUEUE(203),
    TIMER(204),
    WEAKQUEUE(205),
    SOFTTIMER(206),
    OBJVAR(207),
    WALKTRIGGER(208),
    VAR_INT(209),
    VAR_LONG(210),
    VAR_STRING(211),
}

private val extraTypes = ExtraValueType.entries.associateBy { it.id }

internal fun Property.valueType(name: String, id: Int) {
    val label = when (id) {
        0 -> "integer"
        17 -> "skill"
        23 -> "sprite"
        25 -> "font"
        44 -> "baseanimset"
        else -> sharedTypes[id]?.name?.lowercase() ?: extraTypes[id]?.name?.lowercase()
    }
    if (label != null) any(name, label) else int(name, id)
}

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
    val gameval = extraTypes[scriptType]?.gameval
    if (gameval != null) {
        gameval("value", gameval, value)
        return
    }
    val type = sharedTypes[scriptType]
    when {
        type == ScriptVarType.STAT -> skill("value", value)
        type == ScriptVarType.BOOLEAN && value !in 0..1 -> int("value", value)
        type?.baseVarType == BaseVarType.INTEGER -> scriptVarType("value", type, value)
        else -> int("value", value)
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
