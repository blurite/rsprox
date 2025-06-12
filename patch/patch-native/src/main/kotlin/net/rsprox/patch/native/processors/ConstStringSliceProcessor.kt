package net.rsprox.patch.native.processors

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.native.Client
import net.rsprox.patch.native.ConstStringSlice
import net.rsprox.patch.native.DuplicateReplacementBehaviour
import net.rsprox.patch.native.FailureBehaviour
import net.rsprox.patch.native.processors.utils.indexOf

@Suppress("DuplicatedCode")
public class ConstStringSliceProcessor(
    private val client: Client,
    private val slice: ConstStringSlice,
) : ClientProcessor<List<String>> {
    override fun process(): List<String> {
        val results = listMatches()
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

    private fun replaceOccurrences(results: List<Slice>): List<String> {
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
        val replacementBytes = wrapInNullTerminators(slice.new.toByteArray(Charsets.UTF_8))
        val replacements =
            results.map { slice ->
                val (index, old) = slice
                for (i in replacementBytes.indices) {
                    client.bytes[index + i] = replacementBytes[i]
                }
                old
            }
        logger.info {
            "Replaced const string ${slice.old} ${results.size} time${if (results.size == 1) "" else "s"}"
        }
        return replacements
    }

    private fun listMatches(): List<Slice> {
        return slice.old.flatMap { old ->
            val searchBytes = wrapInNullTerminators(old.toByteArray(Charsets.UTF_8))
            var index = 0
            val results = mutableListOf<Slice>()
            while (true) {
                val result = client.indexOf(searchBytes, index)
                if (result == -1) {
                    break
                }
                results += Slice(result, old)
                index = result + searchBytes.size - 1
            }
            return@flatMap results
        }
    }

    private fun wrapInNullTerminators(input: ByteArray): ByteArray {
        val array = ByteArray(input.size + 2)
        input.copyInto(array, 1)
        return array
    }

    private data class Slice(
        val index: Int,
        val search: String,
    )

    private companion object {
        private val logger = InlineLogger()
    }
}
