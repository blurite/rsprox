package net.rsprox.web

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.web.db.SubmissionRepository
import net.rsprox.web.util.zip
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

@Component
public class SubmissionProcessor(
    @Autowired private val props: ApplicationProperties,
    @Autowired private val repo: SubmissionRepository
) {
    private val log = InlineLogger()

    private val s3 = S3Client.builder()
        .region(Region.of(props.s3.region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    props.s3.accessKey,
                    props.s3.secretKey
                )
            )
        )
        .build()

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

            val objectRequest = PutObjectRequest.builder()
                .bucket(props.s3.bucket)
                .key("${submission.id}.zip")
                .tagging("public=true") // Makes the object public
                .build()

            log.trace { "Uploading to S3: ${submission.id}" }

            try {
                val response = s3.putObject(objectRequest, RequestBody.fromBytes(buf.zip(submission.id.toString())))
                if (!response.sdkHttpResponse().isSuccessful) {
                    val statusCode = response.sdkHttpResponse().statusCode()
                    val statusText = response.sdkHttpResponse().statusText().orElse("Unknown error")
                    log.error { "Failed to upload to S3: $statusCode $statusText" }
                    continue
                }
            } catch (e: SdkClientException) {
                log.trace { "Failed to upload to S3: ${e.message}" }
                continue
            }

            submission.processed = true
            repo.save(submission)
            Files.deleteIfExists(path)

            log.trace { "Processed submission: ${submission.id}" }
        }
    }

}
