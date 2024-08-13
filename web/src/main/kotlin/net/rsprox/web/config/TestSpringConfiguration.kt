package net.rsprox.web.config

import jakarta.servlet.MultipartConfigElement
import net.rsprox.web.ApplicationProperties
import net.rsprox.web.db.Submission
import net.rsprox.web.service.FileUploader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
public class TestSpringConfiguration(@Autowired private val props: ApplicationProperties) {

    @Bean
    public fun testMultipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        factory.setMaxFileSize(props.maxFileDataSize)
        return factory.createMultipartConfig()
    }

    @Bean
    public fun testFileUploader(): FileUploader {
        return object : FileUploader {
            override fun uploadFile(buf: ByteArray, submission: Submission): Boolean {
                return true
            }
        }
    }

}
