package net.rsprox.web

import net.rsprox.web.controller.ApiController
import net.rsprox.web.db.Submission
import net.rsprox.web.db.SubmissionRepository
import net.rsprox.web.util.checksum
import net.rsprox.web.util.toBase64
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
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
    fun `when uploading duplicate bin`() {
        Mockito.`when`(applicationProperties.maxFileDataSize).thenReturn(DataSize.ofMegabytes(100))

        val tempDir: Path = Files.createTempDirectory("testDir")
        Mockito.`when`(applicationProperties.uploadDir).thenReturn(tempDir.toString())

        val file = ClassPathResource("test.bin")
        val mockFile = MockMultipartFile(
            "file",
            file.filename,
            "application/octet-stream",
            file.inputStream
        )

        val checksum = mockFile.bytes.checksum()

        val submission = Submission(
            id = 500,
            delayed = false,
            accountHash = ByteArray(32).toBase64(),
            fileChecksum = checksum
        )
        Mockito.`when`(repo.findByFileChecksum(checksum)).thenReturn(submission)

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(ApiController.ResultMessage.DUPLICATE.name))
    }

    @Test
    fun `should upload and process file successfully`() {
        Mockito.`when`(applicationProperties.maxFileDataSize).thenReturn(DataSize.ofMegabytes(100))

        val tempDir: Path = Files.createTempDirectory("testDir")
        Mockito.`when`(applicationProperties.uploadDir).thenReturn(tempDir.toString())

        val file = ClassPathResource("test.bin")
        val mockFile = MockMultipartFile(
            "file",
            file.filename,
            "application/octet-stream",
            file.inputStream
        )

        val submission = Submission(
            id = 500,
            delayed = false,
            accountHash = ByteArray(32).toBase64(),
            fileChecksum = mockFile.bytes.checksum()
        )
        Mockito.`when`(repo.save(any(Submission::class.java))).thenReturn(submission)

        mockMvc.perform(
            multipart("/api/submit")
                .file(mockFile)
                .param("delayed", "false")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        assertTrue(Files.exists(tempDir.resolve("${submission.id}.bin")))
    }

    @Test
    fun `when getting user submissions`() {
        val accountHash = ByteArray(32).toBase64()

        val submission = Submission(
            id = 500,
            delayed = false,
            accountHash = accountHash,
            fileChecksum = ByteArray(20).toBase64()
        )
        Mockito.`when`(repo.findByAccountHash(accountHash)).thenReturn(listOf(submission))

        mockMvc.perform(
            get("/api/submissions").param("accountHash", accountHash)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.submissions").isArray)
            .andExpect(jsonPath("$.submissions.length()").value(1))
            .andExpect(jsonPath("$.submissions[0].id").value(submission.id))
            .andExpect(jsonPath("$.submissions[0].removable").value(!submission.delayed))
            .andExpect(jsonPath("$.submissions[0].fileChecksum").value(submission.fileChecksum))

        mockMvc.perform(
            get("/api/submissions").param("accountHash", "invalidHash")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.submissions").isArray)
            .andExpect(jsonPath("$.submissions.length()").value(0))
    }

    @Test
    fun `when deleting processed submission`() {
        val submission = Submission(
            id = 1,
            delayed = false,
            processed = true,
            accountHash = "",
            fileChecksum = ""
        )

        Mockito.`when`(repo.findById(1)).thenReturn(Optional.of(submission))

        mockMvc.perform(
            delete("/api/submission").param("id", "1")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `when deleting unprocessed submissions`() {
        val submission = Submission(
            id = 1,
            delayed = false,
            processed = false,
            accountHash = "",
            fileChecksum = ""
        )

        Mockito.`when`(repo.findById(1)).thenReturn(Optional.of(submission))

        mockMvc.perform(
            delete("/api/submission").param("id", "1")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

}
