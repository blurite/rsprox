package net.rsprox.web.controller

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.shared.indexing.IndexedType
import net.rsprox.web.ApplicationProperties
import net.rsprox.web.db.IntRepository
import net.rsprox.web.db.StringRepository
import net.rsprox.web.db.Submission
import net.rsprox.web.db.SubmissionRepository
import net.rsprox.web.util.checksum
import net.rsprox.web.util.toBase64
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@RestController
public class ApiController(
    private val props: ApplicationProperties,
    private val repo: SubmissionRepository,
    private val intRepo: IntRepository,
    private val stringRepo: StringRepository
) {
    private val log = InlineLogger()
    private val filters = DefaultPropertyFilterSetStore.load(Path.of(""))
    private val settings = DefaultSettingSetStore.load(Path.of(""))

    @DeleteMapping("/api/submission")
    public fun deleteSubmission(
        @RequestParam("id") id: Long
    ): Map<String, Any> {
        val submission = repo.findByIdOrNull(id) ?: return mapOf("success" to false)
        // dont allow to delete processed submissions
        if (submission.processed) {
            return mapOf("success" to false)
        }
        repo.delete(submission)
        return mapOf("success" to true)
    }

    @GetMapping("/api/search")
    public fun search(
        @RequestParam("type") type: Int,
        @RequestParam("query") query: String
    ): Set<Submission> {
        val submissions: Set<Submission> = when (type) {
            IndexedType.MESSAGE_GAME.id, IndexedType.TEXT.id -> {
                stringRepo.findByValueContainingIgnoreCase(query).map { it.submission }.toSet()
            }

            else -> {
                intRepo.findByValue(query.toInt()).map { it.submission }.toSet()
            }
        }
        return submissions
    }

    @GetMapping("/api/submissions")
    public fun getSubmissions(
        @RequestParam("accountHash") accountHash: String
    ): Map<String, Any> {
        return mapOf(
            "submissions" to repo.findByAccountHash(accountHash).map {
                mapOf(
                    "id" to it.id,
                    "date" to it.createdAt,
                    "removable" to !it.processed,
                    "fileChecksum" to it.fileChecksum
                )
            }
        )
    }

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
        val checksum = file.bytes.checksum()

        repo.findByFileChecksum(checksum)?.let {
            return Result.failure(ResultMessage.DUPLICATE)
        }

        val blobResult = runCatching {
            BinaryBlob.decode(
                file.bytes,
                filters,
                settings
            )
        }

        val blob = blobResult.getOrElse {
            log.error { "Failed to decode file: ${it.message}" }
            return Result.failure(ResultMessage.FAILURE_TO_DECODE)
        }

        val submission = Submission(
            delayed = delayed,
            accountHash = blob.header.accountHash.toBase64(),
            fileChecksum = checksum,
            fileSize = file.size,
            revision = blob.header.revision,
            subRevision = blob.header.subRevision,
            clientType = blob.header.clientType,
            platformType = blob.header.platformType,
            worldActivity = blob.header.worldActivity
        ).let { repo.save(it) }

        runCatching {
            val p = Path.of(props.uploadDir, "${submission.id}.tmp")
            file.transferTo(p)
            try {
                Files.move(p, Path.of(props.uploadDir, "${submission.id}.bin"), StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(p, Path.of(props.uploadDir, "${submission.id}.bin"))
            }
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
        FAILURE_TO_SAVE,
        DUPLICATE,
    }

    private companion object {
        private const val CONTENT_TYPE = "application/octet-stream"
        private const val FILE_EXTENSION = "bin"
    }
}
