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
        val sliceIndices =
            client.bytes.firstSliceIndices(index + 5, 256) { byte ->
                isHex(byte.toInt().toChar())
            }
        val slice = client.bytes.sliceArray(sliceIndices)
        val oldModulus = slice.toString(Charsets.UTF_8)
        val newModulus = replacement.toByteArray(Charsets.UTF_8)
        if (newModulus.size > slice.size) {
            throw IllegalStateException("New modulus cannot be larger than the old.")
        }
        for (i in sliceIndices) {
            val newModulusIndex = i - sliceIndices.first
            // If the new modulus is shorter, terminate it with a null character. The C++ client can handle it.
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
    ): IntRange {
        var start = startIndex
        val size = this.size
        while (true) {
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
            if (length != -1 && end - start < length) {
                start = end
                continue
            }
            return start..<end
        }
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
