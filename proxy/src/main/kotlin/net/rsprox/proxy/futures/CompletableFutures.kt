package net.rsprox.proxy.futures

import io.netty.util.concurrent.Future
import java.util.concurrent.CompletableFuture

public fun List<CompletableFuture<*>>.joinedFuture(): CompletableFuture<Void> {
    return CompletableFuture.allOf(*this.toTypedArray())
}

/**
 * Turns a normal Netty future object into a completable future, allowing
 * for easier use of it.
 */
public fun <V> Future<V>.asCompletableFuture(): CompletableFuture<V> {
    if (isDone) {
        return if (isSuccess) {
            CompletableFuture.completedFuture(now)
        } else {
            CompletableFuture.failedFuture(cause())
        }
    }

    val future = CompletableFuture<V>()

    addListener {
        if (isSuccess) {
            future.complete(now)
        } else {
            future.completeExceptionally(cause())
        }
    }

    return future
}
