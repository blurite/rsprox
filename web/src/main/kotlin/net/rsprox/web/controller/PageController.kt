package net.rsprox.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
public class PageController {

    @GetMapping("/")
    public fun index(): String = "index"

    @GetMapping("/search")
    public fun search(): String = "search"


}
