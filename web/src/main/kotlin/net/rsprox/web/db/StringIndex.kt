package net.rsprox.web.db

import jakarta.persistence.*

@Entity
@Table(name = "string_indexes")
public data class StringIndex(
    override var id: Int = 0,
    override var type: Int,
    var value: String,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id", nullable = false)
    override var submission: Submission
) : BaseIndex(id, type, submission)
