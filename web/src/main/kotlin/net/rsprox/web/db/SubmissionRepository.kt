package net.rsprox.web.db

import org.springframework.data.repository.CrudRepository

public interface SubmissionRepository : CrudRepository<Submission, Long> {
    public fun findByFileChecksum(checksum: String): Submission?
    public fun findByAccountHash(accountHash: String): List<Submission>
    public fun findByProcessed(processed: Boolean): List<Submission>
}
