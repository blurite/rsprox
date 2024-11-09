package net.rsprox.web.controller

import net.rsprox.shared.indexing.IndexedType
import net.rsprox.web.ApplicationProperties
import net.rsprox.web.db.SubmissionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
public class PageController(
    private val props: ApplicationProperties,
    private val repo: SubmissionRepository,
) {

    @GetMapping("/")
    public fun index(): String = "index"

    @GetMapping("/database")
    public fun database(model: Model): String {
        model.addAttribute("types", IndexedType.entries)
        model.addAttribute("totalSubmissions", repo.count())
        model.addAttribute("submissions", repo.findTop25ByOrderByIdDesc())
        return "database"
    }

    @GetMapping("/download/{id}")
    public fun download(@PathVariable("id") id: Long): String {
        val submission = repo.findByIdOrNull(id) ?: return "redirect:/database"
        return "redirect:${props.s3.url}${submission.id}.zip"
    }

}
