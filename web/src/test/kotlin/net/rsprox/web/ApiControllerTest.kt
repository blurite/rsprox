package net.rsprox.web

import net.rsprox.web.controller.ApiController
import net.rsprox.web.db.Submission
import net.rsprox.web.db.SubmissionRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.unit.DataSize
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var applicationProperties: ApplicationProperties

    @MockBean
    private lateinit var repo: SubmissionRepository

    @Test
    fun `when file is empty should return file is empty`() {
        val mockFile = MockMultipartFile("file", "", "text/plain", "".toByteArray())

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(ApiController.ResultMessage.FILE_EMPTY.name))
    }

    @Test
    fun `when file is too large should return file is too large`() {
        Mockito.`when`(applicationProperties.maxFileDataSize).thenReturn(DataSize.ofMegabytes(1))

        val mockFile = MockMultipartFile("file", "largefile.txt", "text/plain", ByteArray(5 * 1024 * 1024))

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(ApiController.ResultMessage.FILE_TOO_LARGE.name))
    }

    @Test
    fun `when file is not rsprot blob`() {
        Mockito.`when`(applicationProperties.maxFileDataSize).thenReturn(DataSize.ofMegabytes(100))

        val png = ClassPathResource("test.png")
        var mockFile = MockMultipartFile(
            "file",
            png.filename,
            "image/png",
            png.inputStream
        )

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(ApiController.ResultMessage.FILE_CONTENT_TYPE_INVALID.name))

        // send the same file but with a different content type
        mockFile = MockMultipartFile(
            "file",
            png.filename,
            "application/octet-stream",
            png.inputStream
        )

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(ApiController.ResultMessage.FILE_CONTENT_TYPE_INVALID.name))
    }

    @Test
    fun `should upload and process file successfully`() {
        Mockito.`when`(applicationProperties.maxFileDataSize).thenReturn(DataSize.ofMegabytes(100))

        val tempDir: Path = Files.createTempDirectory("testDir")
        Mockito.`when`(applicationProperties.uploadDir).thenReturn(tempDir.toString())

        val submission = Submission(
            id = 500,
            delayed = false,
            accountHash = Base64.getEncoder().encodeToString(ByteArray(32))
        )
        Mockito.`when`(repo.save(any(Submission::class.java))).thenReturn(submission)

        val file = ClassPathResource("test.bin")
        val mockFile = MockMultipartFile(
            "file",
            file.filename,
            "application/octet-stream",
            file.inputStream
        )

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        assertTrue(Files.exists(tempDir.resolve("${submission.id}.bin")))
    }

}
