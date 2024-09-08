package net.rsprox.web.controller

import net.rsprox.shared.indexing.IndexedType
import net.rsprox.web.ApplicationProperties
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
public class PageController(
    private val props: ApplicationProperties,
) {

    @GetMapping("/")
    public fun index(): String = "index"

    @GetMapping("/database")
    public fun database(model: Model): String {
        model.addAttribute("bucketUrl", props.s3.url)
        model.addAttribute("types", IndexedType.entries)
        return "database"
    }

}
