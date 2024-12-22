package net.rsprox.patch.native

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.patch.PatchResult
import net.rsprox.patch.Patcher
import net.rsprox.patch.native.processors.ClientProcessor
import net.rsprox.patch.native.processors.ConstByteSequenceProcessor
import net.rsprox.patch.native.processors.ConstStringSliceProcessor
import net.rsprox.patch.native.processors.PatternStringSliceProcessor
import net.rsprox.patch.native.processors.RsaModulusProcessor
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.isRegularFile

@Suppress("DuplicatedCode")
public class NativePatcher: Patcher<NativePatchCriteria> {
    public override fun patch(
        path: Path,
        criteria: NativePatchCriteria,
    ): PatchResult {
        if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
            throw IllegalArgumentException("Path $path does not point to a file.")
        }
        logger.debug { "Attempting to patch $path" }

        val client = Client(path.toFile().readBytes())
        val replacementModulus = criteria.rsaModulus
        val oldModulus =
            if (replacementModulus != null) {
                val processor = RsaModulusProcessor(client, replacementModulus)
                processor.process()
            } else {
                null
            }

        val processors = mutableListOf<ClientProcessor<*>>()
        val priorityProcessors = mutableListOf<Pair<Int, ClientProcessor<*>>>()
        for (constString in criteria.constStrings) {
            priorityProcessors += constString.priority to ConstStringSliceProcessor(client, constString)
        }

        for (patternString in criteria.patternStrings) {
            priorityProcessors += patternString.priority to
                PatternStringSliceProcessor(
                    client,
                    criteria.type,
                    patternString,
                )
        }

        for (sequence in criteria.bytePatternSlices) {
            priorityProcessors += sequence.priority to ConstByteSequenceProcessor(client, sequence)
        }

        val sorted = priorityProcessors.sortedByDescending { it.first }
        for ((_, processor) in sorted) {
            processors += processor
        }

        for (processor in processors) {
            processor.process()
        }

        path.toFile().writeBytes(client.bytes)
        return PatchResult.Success(
            oldModulus,
            path,
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
