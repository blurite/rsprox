package net.rsprox.web

import net.rsprox.web.db.Submission
import net.rsprox.web.db.SubmissionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests @Autowired constructor(
    val entityManager: TestEntityManager,
    val submissionRepository: SubmissionRepository
) {

    @Test
    fun `test insert and retrieve submission`() {
        val submission = Submission(
            processed = false,
            delayed = false,
            accountHash = "hash",
            fileChecksum = "checksum",
            fileSize = 0,
            revision = 1,
            subRevision = 1,
            clientType = 1,
            platformType = 1,
            worldActivity = "test"
        )
        entityManager.persist(submission)
        entityManager.flush()
        val found = submissionRepository.findByIdOrNull(submission.id!!)
        assertThat(found).isEqualTo(submission)
    }

}