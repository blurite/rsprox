package net.rsprox.patch.cpp

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.PatchResult
import net.rsprox.patch.Patcher
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.isRegularFile

public class NativePatcher : Patcher {
    override fun patch(
        path: Path,
        rsa: String,
        javConfigUrl: String,
        worldListUrl: String,
        port: Int,
    ): PatchResult {
        if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
            throw IllegalArgumentException("Path $path does not point to a file.")
        }
        logger.debug { "Attempting to patch $path" }
        val bytes = path.toFile().readBytes()
        val result = patchModulus(bytes, rsa)
        patchLocalhost(bytes)
        patchJavConfig(bytes, javConfigUrl)
        patchWorldList(bytes, worldListUrl)
        patchJs5Port(bytes, port)
        patchGamePort(bytes, port)
        logger.debug { "Successfully patched $path" }
        path.toFile().writeBytes(bytes)
        return PatchResult.Success(
            result,
            path,
        )
    }

    private fun patchJs5Port(
        bytes: ByteArray,
        port: Int,
    ) {
        bytes.patchPort(0xB9, port)
        logger.debug { "Replaced JS5 port 43594 with $port" }
    }

    private fun patchGamePort(
        bytes: ByteArray,
        port: Int,
    ) {
        bytes.patchPort(0xB8, port)
        logger.debug { "Replaced game port 43594 with $port" }
    }

    private fun ByteArray.patchPort(
        opcode: Int,
        port: Int,
    ) {
        val input = byteArrayOf(opcode.toByte(), 0x4A, 0xAA.toByte(), 0x00, 0x00)
        val port1 = (port and 0xFF).toByte()
        val port2 = (port ushr 8).toByte()
        val output = byteArrayOf(opcode.toByte(), port1, port2, 0x00, 0x00)
        replaceBytes(input, output)
    }

    private fun ByteArray.replaceBytes(
        input: ByteArray,
        output: ByteArray,
    ) {
        val index = indexOf(input)
        check(index != -1) {
            "Unable to find byte sequence: ${input.contentToString()}"
        }
        overwrite(index, output)
    }

    private fun patchLocalhost(bytes: ByteArray) {
        // Rather than only accept the localhost below
        val searchInput = "/127.0.0.1"
        // We need to accept all localhost addresses
        val replacement = "/127."

        replaceText(bytes, searchInput, replacement)
        logger.debug { "Replaced localhost from $searchInput to $replacement" }
    }

    private fun patchJavConfig(
        bytes: ByteArray,
        replacement: String,
    ) {
        val searchInput = "http://oldschool.runescape.com/jav_config.ws?m=0"
        replaceText(bytes, searchInput, replacement)
        logger.debug { "Replaced jav_config.ws from $searchInput to $replacement" }
    }

    private fun patchWorldList(
        bytes: ByteArray,
        replacement: String,
    ) {
        val searchInput = "https://oldschool.runescape.com/slr.ws?order=LPWM"
        replaceText(bytes, searchInput, replacement)
        logger.debug { "Replaced worldlist from $searchInput to $replacement" }
    }

    private fun replaceText(
        bytes: ByteArray,
        input: String,
        replacement: String,
    ) {
        require(replacement.length <= input.length) {
            "Replacement string cannot be longer than the input"
        }
        val searchBytes = input.toByteArray(Charsets.UTF_8)
        val replacementBytes = replacement.toByteArray(Charsets.UTF_8)
        val index = bytes.indexOf(searchBytes)
        if (index == -1) {
            throw IllegalArgumentException("Unable to locate input $input")
        }
        for (i in replacementBytes.indices) {
            bytes[index + i] = replacementBytes[i]
        }
        // Terminate the string if it is smaller than the other
        if (replacementBytes.size < searchBytes.size) {
            bytes[index + replacementBytes.size] = 0
        }
    }

    private fun patchModulus(
        bytes: ByteArray,
        replacement: String,
    ): String {
        val exponent = "10001".toByteArray(Charsets.UTF_8)
        val index = bytes.indexOf(exponent)
        if (index == -1) {
            throw IllegalStateException("Unable to locate exponent 10001")
        }
        val sliceIndices =
            bytes.firstSliceIndices(index + 5) { byte ->
                isHex(byte.toInt().toChar())
            }
        val slice = bytes.sliceArray(sliceIndices)
        val oldModulus = slice.toString(Charsets.UTF_8)
        val newModulus = replacement.toByteArray(Charsets.UTF_8)
        if (newModulus.size > slice.size) {
            throw IllegalStateException("New modulus cannot be larger than the old.")
        }
        for (i in sliceIndices) {
            val newModulusIndex = i - sliceIndices.first
            // If the new modulus is shorter, terminate it with a null character. The C++ client can handle it.
            if (newModulusIndex >= newModulus.size) {
                bytes[i] = 0
                continue
            }
            bytes[i] = newModulus[newModulusIndex]
        }
        logger.debug { "Patched RSA modulus" }
        logger.debug { "Old modulus: $oldModulus" }
        logger.debug { "New modulus: $replacement" }
        return oldModulus
    }

    private fun ByteArray.overwrite(
        index: Int,
        replacement: ByteArray,
    ) {
        for (i in replacement.indices) {
            this[i + index] = replacement[i]
        }
    }

    private fun ByteArray.firstSliceIndices(
        startIndex: Int,
        condition: (Byte) -> Boolean,
    ): IntRange {
        var start = startIndex
        val size = this.size
        // First locate the starting index where a byte is being accepted
        while (start < size) {
            val byte = this[start]
            if (condition(byte)) {
                break
            }
            start++
        }
        var end = start + 1
        // Now find the end index where a byte is not being accepted
        while (end < size) {
            val byte = this[end]
            if (!condition(byte)) {
                break
            }
            end++
        }
        return start..<end
    }

    private fun ByteArray.indexOf(
        search: ByteArray,
        startIndex: Int = 0,
    ): Int {
        require(search.isNotEmpty()) {
            "Bytes to search are empty"
        }
        require(startIndex >= 0) {
            "Start index is negative"
        }
        var matchOffset = 0
        var start = startIndex
        var offset = startIndex
        val size = size
        while (offset < size) {
            if (this[offset] == search[matchOffset]) {
                if (matchOffset++ == 0) {
                    start = offset
                }
                if (matchOffset == search.size) {
                    return start
                }
            } else {
                matchOffset = 0
            }
            offset++
        }
        return -1
    }

    private fun isHex(char: Char): Boolean {
        return char in lowercaseHexStringCharRange ||
            char in uppercaseHexStringCharRange ||
            char in hexDigitsCharRange
    }

    private companion object {
        private val lowercaseHexStringCharRange = 'a'..'f'
        private val uppercaseHexStringCharRange = 'A'..'F'
        private val hexDigitsCharRange = '0'..'9'
        private val logger = InlineLogger()
    }
}
