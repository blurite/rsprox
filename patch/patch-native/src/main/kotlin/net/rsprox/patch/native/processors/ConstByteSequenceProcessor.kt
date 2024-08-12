package net.rsprox.patch.native.processors

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.native.Client
import net.rsprox.patch.native.DuplicateReplacementBehaviour
import net.rsprox.patch.native.FailureBehaviour
import net.rsprox.patch.native.WildcardHexByteSequenceSlice
import net.rsprox.patch.native.processors.utils.indexOf
import net.rsprox.patch.native.processors.utils.write

internal class ConstByteSequenceProcessor(
    private val client: Client,
    private val slice: WildcardHexByteSequenceSlice,
) : ClientProcessor<List<ByteArray>> {
    override fun process(): List<ByteArray> {
        check(slice.old isCompatible slice.new) {
            "${slice.old} is incompatible with ${slice.new}"
        }
        val results = getPatternMatchResults()
        if (results.isEmpty()) {
            when (slice.failureBehaviour) {
                FailureBehaviour.ERROR -> throw IllegalStateException("Unable to find ${slice.old}")
                FailureBehaviour.WARN -> {
                    logger.warn { "Unable to find ${slice.old}" }
                }

                FailureBehaviour.SKIP -> {
                }
            }
            return emptyList()
        }
        if (slice.duplicateReplacementBehaviour == DuplicateReplacementBehaviour.REPLACE_ALL_OCCURRENCES) {
            return replaceOccurrences(results)
        }
        if (results.size > 1 &&
            slice.duplicateReplacementBehaviour != DuplicateReplacementBehaviour.REPLACE_FIRST_OCCURRENCE_ONLY
        ) {
            when (slice.duplicateReplacementBehaviour) {
                DuplicateReplacementBehaviour.ERROR_ON_DUPLICATES -> {
                    throw IllegalStateException("${results.size} matches found for ${slice.old}")
                }
                DuplicateReplacementBehaviour.WARN_ON_DUPLICATES -> {
                    logger.warn { "${results.size} matches found for ${slice.old}" }
                }
                DuplicateReplacementBehaviour.SKIP_ON_DUPLICATES -> {
                    return emptyList()
                }
                else -> {
                    // no-op, impossible
                }
            }
        }
        return replaceOccurrences(results.take(1))
    }

    private fun replaceOccurrences(results: List<Int>): List<ByteArray> {
        if (results.isEmpty()) {
            when (slice.failureBehaviour) {
                FailureBehaviour.ERROR -> throw IllegalStateException("Unable to find ${slice.old}")
                FailureBehaviour.WARN -> {
                    logger.warn { "Unable to find ${slice.old}" }
                }

                FailureBehaviour.SKIP -> {
                }
            }
            return emptyList()
        }
        val replacements =
            results.map { index ->
                client.write(slice.new, index)
            }
        logger.info {
            "Replaced pattern ${slice.old} ${results.size} time${if (results.size == 1) "" else "s"}"
        }
        return replacements
    }

    private fun getPatternMatchResults(): List<Int> {
        val occurrences = mutableListOf<Int>()
        var startIndex = 0
        while (true) {
            val index = client.indexOf(slice.old, startIndex)
            if (index == -1) {
                break
            }
            occurrences += index
            startIndex = index + slice.old.length
        }
        return occurrences
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
