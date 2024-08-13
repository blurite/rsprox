package net.rsprox.web.db

import org.springframework.data.repository.CrudRepository

public interface IndexRepository : CrudRepository<BaseIndex, Long>

public interface StringRepository : CrudRepository<StringIndex, Long> {
    public fun findByValueContainingIgnoreCase(value: String): List<StringIndex>
}

public interface IntRepository : CrudRepository<IntIndex, Long> {
    public fun findByValue(value: Int): List<IntIndex>
}
