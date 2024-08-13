package net.rsprox.web.service

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.web.ApplicationProperties
import net.rsprox.web.db.Submission
import net.rsprox.web.util.zip
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

public class S3FileUploader(
    private val props: ApplicationProperties
) : FileUploader {

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

    override fun uploadFile(buf: ByteArray, submission: Submission): Boolean {
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
                return false
            }
        } catch (e: SdkClientException) {
            log.trace { "Failed to upload to S3: ${e.message}" }
            return false
        }

        return true
    }

}
