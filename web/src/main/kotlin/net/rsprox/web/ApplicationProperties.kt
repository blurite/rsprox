package net.rsprox.web

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize

@ConfigurationProperties(prefix = "app")
public class ApplicationProperties(
    maxFileSize: String,
    public val uploadDir: String
) {
    public val maxFileDataSize: DataSize = DataSize.parse(maxFileSize)
}
