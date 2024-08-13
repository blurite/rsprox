package net.rsprox.web.db

import jakarta.persistence.*

@Entity
@Table(name = "int_indexes")
public data class IntIndex(
    override var id: Int = 0,
    override var type: Int,
    var value: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id", nullable = false)
    override var submission: Submission
) : BaseIndex(id, type, submission)
