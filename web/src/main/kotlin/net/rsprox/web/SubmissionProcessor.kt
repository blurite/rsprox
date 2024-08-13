package net.rsprox.web

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.web.db.SubmissionRepository
import net.rsprox.web.service.FileUploader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

@Component
public class SubmissionProcessor(
    @Autowired private val props: ApplicationProperties,
    @Autowired private val repo: SubmissionRepository,
    @Autowired private val fileUploader: FileUploader
) {
    private val log = InlineLogger()

    @Scheduled(fixedRate = 60_000)
    public fun processSubmissions() {
        val instant = LocalDateTime.now()
        val list = repo.findByProcessed(false)
        for (submission in list) {
            log.trace { "Processing submission: ${submission.id}" }

            // skip delayed submissions
            if (submission.delayed && submission.createdAt.plusDays(1).isBefore(instant)) {
                log.trace { "Skipping delayed submission: ${submission.id}" }
                continue
            }

            val path = Path.of(props.uploadDir, "${submission.id}.bin")
            if (!Files.exists(path)) {
                log.trace { "Skipping missing file: ${submission.id}" }
                continue
            }

            val buf = Files.readAllBytes(path)

            // TODO process the indexes

            if (!fileUploader.uploadFile(buf, submission)) {
                log.trace { "Failed to upload ${submission.id}" }
                continue
            }

            submission.processed = true
            repo.save(submission)
            Files.deleteIfExists(path)

            log.trace { "Processed submission: ${submission.id}" }
        }
    }

}
