package net.rsprox.web.config

import jakarta.servlet.MultipartConfigElement
import net.rsprox.web.ApplicationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
public class SpringConfiguration(@Autowired private val props: ApplicationProperties) {

    @Bean
    public fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        factory.setMaxFileSize(props.maxFileDataSize)
        return factory.createMultipartConfig()
    }

}
