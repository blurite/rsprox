package net.rsprox.patch.native.processors

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.native.Client
import net.rsprox.patch.native.DuplicateReplacementBehaviour
import net.rsprox.patch.native.FailureBehaviour
import net.rsprox.patch.native.PatternStringSlice
import net.rsprox.patch.native.processors.utils.indexOf

@Suppress("DuplicatedCode")
public class PatternStringSliceProcessor(
    private val client: Client,
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
            val range = index..<index + (result.value.length)
            val bytes = client.bytes.sliceArray(range)
            val text = bytes.toString(Charsets.UTF_8)
            val replaced = text.replace(pattern, slice.replacement)
            logger.info {
                "'$text' -> '$replaced'"
            }
            val replacementBytes = replaced.toByteArray(Charsets.UTF_8)
            for (i in replacementBytes.indices) {
                client.bytes[range.first + i] = replacementBytes[i]
            }
            text
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
