package net.rsprox.cache.store

import io.netty.buffer.Unpooled
import net.rsprox.cache.Js5MasterIndex
import org.openrs2.cache.DiskStore
import org.openrs2.cache.Store
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalReplayDiskCacheStoreTest {
    @Test
    fun `opens disk cache and compares its master index`() {
        val cacheDirectory = Files.createTempDirectory("rsprox-local-replay-cache")
        val expected = byteArrayOf(1, 2, 3, 4)
        DiskStore.create(cacheDirectory).use { store ->
            store.write(Store.ARCHIVESET, Store.ARCHIVESET, Unpooled.wrappedBuffer(expected))
        }

        LocalReplayDiskCacheStore(cacheDirectory).use { store ->
            store.open()
            assertEquals(ReplayCacheMatch.MATCH, store.match(Js5MasterIndex(240, expected)))
            assertEquals(ReplayCacheMatch.MISMATCH, store.match(Js5MasterIndex(240, byteArrayOf(9))))
        }

        val invalidDirectory = Files.createTempDirectory("rsprox-invalid-replay-cache")
        assertFailsWith<IllegalArgumentException> {
            LocalReplayDiskCacheStore(invalidDirectory).open()
        }
    }
}
