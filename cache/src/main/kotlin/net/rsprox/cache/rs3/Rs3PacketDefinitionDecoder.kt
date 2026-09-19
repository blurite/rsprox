package net.rsprox.cache.rs3

import net.rsprox.cache.api.rs3.Rs3BaseVarType
import net.rsprox.cache.api.rs3.Rs3QuickChatCommand
import net.rsprox.cache.api.rs3.Rs3QuickChatPhrase
import net.rsprox.cache.api.rs3.Rs3VarbitDefinition
import net.rsprox.cache.api.rs3.Rs3VariableDefinition
import java.nio.ByteBuffer

/** Revision-950 definition grammars; unsupported opcodes/types fail rather than guess a wire width. */
internal object Rs3PacketDefinitionDecoder {
    fun varbit(bytes: ByteArray): Rs3VarbitDefinition {
        val input = ByteBuffer.wrap(bytes)
        var domain = -1
        var base = -1
        var start = 0
        var end = 0
        while (true) {
            when (val opcode = input.byte()) {
                0 -> {
                    require(!input.hasRemaining() && domain >= 0 && base >= 0 && start <= end && end < 64) {
                        "Invalid RS3 varbit definition"
                    }
                    return Rs3VarbitDefinition(domain, base, start, end)
                }
                1 -> {
                    domain = input.byte()
                    base = if (input.get(input.position()) < 0) input.int and Int.MAX_VALUE else input.word()
                }
                2 -> {
                    start = input.byte()
                    end = input.byte()
                }
                16 -> Unit
                else -> error("Unknown RS3 varbit opcode $opcode")
            }
        }
    }

    fun variable(bytes: ByteArray): Rs3VariableDefinition {
        val input = ByteBuffer.wrap(bytes)
        var type: Int? = null
        while (true) {
            when (val opcode = input.byte()) {
                0 -> {
                    require(!input.hasRemaining()) { "Trailing variable definition bytes" }
                    val id = checkNotNull(type) { "Variable has no script type" }
                    return Rs3VariableDefinition(id, baseType(id))
                }
                3 -> type = input.byte()
                4, 5 -> input.byte()
                110 -> input.short
                7, 8 -> Unit
                else -> error("Unsupported variable definition opcode $opcode")
            }
        }
    }

    fun phrase(bytes: ByteArray): Rs3QuickChatPhrase {
        val input = ByteBuffer.wrap(bytes)
        var template = ""
        var commands = emptyList<Rs3QuickChatCommand>()
        while (true) {
            when (val opcode = input.byte()) {
                0 -> {
                    require(!input.hasRemaining()) { "Trailing quickchat phrase bytes" }
                    return Rs3QuickChatPhrase(template, commands)
                }
                1 -> {
                    val start = input.position()
                    while (input.byte() != 0) Unit
                    template = String(bytes, start, input.position() - start - 1, CP1252)
                }
                2 -> repeat(input.byte()) { input.short }
                3, 5 ->
                    commands =
                        List(input.byte()) {
                            val id = input.word()
                            val (send, receive, arguments) = command(id)
                            Rs3QuickChatCommand(
                                id,
                                send,
                                receive,
                                List(arguments) {
                                    if (opcode == 5) input.varIntLE() else input.word()
                                },
                            )
                        }
                4 -> Unit
                else -> error("Unsupported quickchat phrase opcode $opcode")
            }
        }
    }

    // Native initializer 0x1e8b6f..0x1e8e9f: id, send width, receive width, definition-argument count.
    private fun command(id: Int): Triple<Int, Int, Int> =
        when (id) {
            0 -> Triple(2, 2, 1)
            1, 10 -> Triple(2, 2, 0)
            2 -> Triple(4, 4, 0)
            4 -> Triple(1, 1, 1)
            6, 16 -> Triple(0, 4, 2)
            7 -> Triple(0, 1, 1)
            8, 9, 14 -> Triple(0, 4, 1)
            11 -> Triple(0, 2, 2)
            12, 13, 15 -> Triple(0, 1, 0)
            else -> error("Unsupported quickchat command $id")
        }

    private fun baseType(id: Int): Rs3BaseVarType =
        when (id) {
            35, 49, 56, 71, 110, 115, 116, 118 -> Rs3BaseVarType.LONG
            36 -> Rs3BaseVarType.STRING
            50 -> Rs3BaseVarType.COORDINATE
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43,
            44, 45, 46, 47, 48, 51, 53, 54, 55, 57, 58, 59, 60, 61,
            62, 63, 64, 65, 66, 67, 68, 69, 70, 72, 73, 74, 75, 76,
            77, 78, 79, 80, 81, 83, 84, 85, 86, 87, 88, 89, 90, 91,
            92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
            106, 107, 108, 109, 111, 112, 113, 114, 117, 119, 120, 121, 122, 123,
            124, 125, 126, 127, 129, 131, 133, 135, 136, 137, 138, 202, 203, 204,
            205, 206, 207, 208, 209, 210, 211,
            -> Rs3BaseVarType.INT
            else -> error("Unsupported RS3 variable script type $id")
        }

    private fun ByteBuffer.byte(): Int = get().toInt() and 255

    private fun ByteBuffer.word(): Int = short.toInt() and 65535

    private fun ByteBuffer.varIntLE(): Int {
        var result = 0
        for (index in 0..4) {
            val next = byte()
            require(index != 4 || next <= 15) { "Quickchat definition argument exceeds 32 bits" }
            result = result or ((next and 127) shl (index * 7))
            if (next and 128 == 0) return result
        }
        error("Unterminated quickchat definition argument")
    }

    private val CP1252 = charset("windows-1252")
}
