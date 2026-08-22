package net.rsprox.patch.native.processors

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.native.Client
import net.rsprox.patch.native.processors.utils.indexOf

@Suppress("DuplicatedCode")
internal class RsaModulusProcessor(
    private val client: Client,
    private val replacement: String,
) : ClientProcessor<String> {
    override fun process(): String {
        val exponent = "10001".toByteArray(Charsets.UTF_8)
        val index = client.indexOf(exponent)
        if (index == -1) {
            throw IllegalStateException("Unable to locate exponent 10001")
        }
        val expectedLength = if (replacement.length > 256) replacement.length else 256

        // 1. Look before exponent (OSRS native client & RS3 rs2client.exe)
        val searchBeforeStart = maxOf(0, index - expectedLength - 64)
        var sliceIndices = client.bytes.firstSliceIndices(searchBeforeStart, expectedLength) { byte ->
            isHex(byte.toInt().toChar())
        }

        // 2. If not found before exponent, search after exponent (RS3 launcher)
        if (sliceIndices == null || sliceIndices.first >= index) {
            val searchAfterStart = index + exponent.size
            sliceIndices = client.bytes.firstSliceIndices(searchAfterStart, expectedLength) { byte ->
                isHex(byte.toInt().toChar())
            }
        }

        if (sliceIndices == null) {
            throw IllegalStateException("Unable to locate RSA modulus of length $expectedLength near exponent 10001")
        }

        val slice = client.bytes.sliceArray(sliceIndices)
        val oldModulus = slice.toString(Charsets.UTF_8)
        val newModulus = replacement.toByteArray(Charsets.UTF_8)
        if (newModulus.size > slice.size) {
            throw IllegalStateException("New modulus cannot be larger than the old.")
        }
        for (i in sliceIndices) {
            val newModulusIndex = i - sliceIndices.first
            if (newModulusIndex >= newModulus.size) {
                client.bytes[i] = 0
                continue
            }
            client.bytes[i] = newModulus[newModulusIndex]
        }
        logger.debug { "Patched RSA modulus" }
        logger.debug { "Old modulus: $oldModulus" }
        logger.debug { "New modulus: $replacement" }
        return oldModulus
    }

    private fun ByteArray.firstSliceIndices(
        startIndex: Int,
        length: Int = -1,
        condition: (Byte) -> Boolean,
    ): IntRange? {
        var start = startIndex
        val size = this.size
        while (start < size) {
            while (start < size && !condition(this[start])) {
                start++
            }
            if (start >= size) break
            var end = start + 1
            while (end < size && condition(this[end])) {
                end++
            }
            if (length != -1 && (end - start) != length) {
                start = end
                continue
            }
            return start..<end
        }
        return null
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
