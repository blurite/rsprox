package net.rsprox.web

import jakarta.servlet.MultipartConfigElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
public class SpringConfiguration(@Autowired private val props: ApplicationProperties) {

    @Bean
    public fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        factory.setMaxFileSize(props.maxFileDataSize)
        return factory.createMultipartConfig()
    }

}
