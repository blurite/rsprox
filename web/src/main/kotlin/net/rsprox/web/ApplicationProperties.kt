package net.rsprox.web

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize

@ConfigurationProperties(prefix = "app")
public class ApplicationProperties(
    maxFileSize: String,
    public val uploadDir: String,
    public val s3: S3Properties,
) {
    public val maxFileDataSize: DataSize = DataSize.parse(maxFileSize)
}

public data class S3Properties(
    public val region: String,
    public val accessKey: String,
    public val secretKey: String,
    public val bucket: String,
    public val url: String
)
