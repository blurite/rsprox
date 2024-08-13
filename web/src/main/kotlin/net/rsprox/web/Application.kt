package net.rsprox.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
public class Application

public fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
