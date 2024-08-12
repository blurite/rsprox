package net.rsprox.patch.native.processors

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.NativeClientType
import net.rsprox.patch.native.Client
import net.rsprox.patch.native.DuplicateReplacementBehaviour
import net.rsprox.patch.native.FailureBehaviour
import net.rsprox.patch.native.PatternStringSlice
import net.rsprox.patch.native.processors.utils.indexOf

@Suppress("DuplicatedCode")
public class PatternStringSliceProcessor(
    private val client: Client,
    private val nativeClientType: NativeClientType,
    private val slice: PatternStringSlice,
) : ClientProcessor<List<String>> {
    override fun process(): List<String> {
        val pattern = slice.pattern
        val results = slice.pattern.findAll(client.bytes.toString(Charsets.UTF_8)).toList()
        if (results.isEmpty()) {
            when (slice.failureBehaviour) {
                FailureBehaviour.ERROR -> throw IllegalStateException("Unable to find ${slice.pattern}")
                FailureBehaviour.WARN -> {
                    logger.warn { "Unable to find ${slice.pattern}" }
                }

                FailureBehaviour.SKIP -> {
                }
            }
            return emptyList()
        }
        if (slice.duplicateReplacementBehaviour == DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES) {
            return replaceOccurrences(pattern, results)
        }
        if (results.size > 1 &&
            slice.duplicateReplacementBehaviour != DuplicateReplacementBehaviour.REPLACE_FIRST_OCCURRENCE_ONLY
        ) {
            when (slice.duplicateReplacementBehaviour) {
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES -> {
                    throw IllegalStateException("${results.size} matches found for ${slice.pattern}")
                }
                DuplicateReplacementBehaviour.WARN_ON_DUPLICATES -> {
                    logger.warn { "${results.size} matches found for ${slice.pattern}" }
                }
                DuplicateReplacementBehaviour.SKIP_ON_DUPLICATES -> {
                    return emptyList()
                }
                else -> {
                    // no-op, impossible
                }
            }
        }
        return replaceOccurrences(pattern, results.take(1))
    }

    private fun replaceOccurrences(
        pattern: Regex,
        results: List<MatchResult>,
    ): List<String> {
        logger.info { "Pattern $pattern replacements:" }
        return results.map { result ->
            val index = client.indexOf(result.value.toByteArray(Charsets.UTF_8))
            if (index == -1) {
                throw IllegalStateException("Unexpected error")
            }
            val rangeWithTerminators = index..<index + (result.value.length)
            val bytes = client.bytes.sliceArray(rangeWithTerminators)
            val text = bytes.toString(Charsets.UTF_8)
            val replaced = text.replace(pattern, slice.replacement)
            logger.info {
                "'$text' -> '$replaced'"
            }
            val replacementBytes = replaced.toByteArray(Charsets.UTF_8)
            for (i in replacementBytes.indices) {
                client.bytes[rangeWithTerminators.first + i] = replacementBytes[i]
            }
            // Native client will break if strings aren't the same length
            // This appears to be due to how the string implementation under the hood works,
            // where it joins multiple smaller char arrays together to build strings.
            // If there are any unexpected terminators, there's a chance it will crash.
            // (Above explanation is a rough guess at what goes on - no guarantees)
            if (nativeClientType == NativeClientType.MAC) {
                for (i in replacementBytes.indices.last..<bytes.size - 1) {
                    client.bytes[rangeWithTerminators.first + i] = ' '.code.toByte()
                }
            }
            text
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
