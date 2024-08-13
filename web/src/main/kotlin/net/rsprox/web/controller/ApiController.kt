package net.rsprox.web.controller

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.web.ApplicationProperties
import net.rsprox.web.db.Submission
import net.rsprox.web.db.SubmissionRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.*

@RestController
public class ApiController(
    private val props: ApplicationProperties,
    private val repo: SubmissionRepository
) {
    private val log = InlineLogger()

    @PostMapping("/api/submit")
    public fun submit(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("delayed") delayed: Boolean = false
    ): Result {
        val result = when {
            file.isEmpty -> Result.failure(ResultMessage.FILE_EMPTY)
            file.size > props.maxFileDataSize.toBytes() -> Result.failure(ResultMessage.FILE_TOO_LARGE)
            file.contentType != CONTENT_TYPE -> Result.failure(ResultMessage.FILE_CONTENT_TYPE_INVALID)
            file.originalFilename?.substringAfterLast(
                '.',
                ""
            ) != FILE_EXTENSION -> Result.failure(ResultMessage.FILE_CONTENT_TYPE_INVALID)

            else -> processFile(file, delayed)
        }
        return result
    }

    private fun processFile(file: MultipartFile, delayed: Boolean): Result {
        val blobResult = runCatching {
            BinaryBlob.decode(
                file.bytes, DefaultPropertyFilterSetStore(
                    Path.of(""),
                    mutableListOf()
                )
            )
        }

        val blob = blobResult.getOrElse {
            log.error { "Failed to decode file: ${it.message}" }
            return Result.failure(ResultMessage.FAILURE_TO_DECODE)
        }

        val submission = Submission(
            delayed = delayed,
            accountHash = Base64.getEncoder().encodeToString(blob.header.accountHash)
        ).let { repo.save(it) }

        runCatching {
            file.transferTo(Path.of(props.uploadDir, "${submission.id}.$FILE_EXTENSION"))
        }.onFailure {
            log.error { "Failed to save file: ${it.message}" }
            return Result.failure(ResultMessage.FAILURE_TO_SAVE)
        }

        return Result.success()
    }

    public data class Result(
        val success: Boolean,
        val message: ResultMessage
    ) {
        public companion object {
            public fun success(): Result = Result(true, ResultMessage.SUCCESS)
            public fun failure(message: ResultMessage): Result = Result(false, message)
        }
    }

    public enum class ResultMessage {
        SUCCESS,
        FILE_EMPTY,
        FILE_TOO_LARGE,
        FILE_CONTENT_TYPE_INVALID,
        FAILURE_TO_DECODE,
        FAILURE_TO_SAVE
    }

    private companion object {
        private const val CONTENT_TYPE = "application/octet-stream"
        private const val FILE_EXTENSION = "bin"
    }
}
