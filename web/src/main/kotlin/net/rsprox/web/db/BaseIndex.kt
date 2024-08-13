package net.rsprox.web.db

import jakarta.persistence.*

@MappedSuperclass
public abstract class BaseIndex(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public open var id: Int = 0,
    public open var type: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submission_id", nullable = false)
    public open var submission: Submission
)
