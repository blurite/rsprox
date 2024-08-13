package net.rsprox.web.db

import jakarta.persistence.*
import java.time.LocalDateTime

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
    public var fileChecksum: String
)
