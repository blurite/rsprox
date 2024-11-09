package net.rsprox.web.db

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ln
import kotlin.math.pow

@Entity
@Table(name = "submissions")
public data class Submission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public var id: Long? = null,
    public var createdAt: LocalDateTime = LocalDateTime.now(),
    public var processed: Boolean = false,
    public var delayed: Boolean,
    public var accountHash: String,
    public var fileChecksum: String,
    public var fileSize: Long,
    public var revision: Int,
    public var subRevision: Int,
    public var clientType: Int,
    public var platformType: Int,
    public var worldActivity: String
) {

    public fun readableDate(): String {
        return formatter.format(createdAt)
    }

    public fun readableSize(): String {
        if (fileSize == 0L) return "0 Bytes"

        val sizes = arrayOf("Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
        val i = (ln(fileSize.toDouble()) / ln(1024.0)).toInt()

        return String.format("%.2f %s", fileSize / 1024.0.pow(i.toDouble()), sizes[i])
    }

    public companion object {
        public val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    }
}

public class SubmissionResult(submission: Submission) {
    public val id: Long = submission.id!!
    public val date: String = submission.readableDate()
    public val revision: Int = submission.revision
    public val worldActivity: String = submission.worldActivity
    public val fileSize: String = submission.readableSize()
}
