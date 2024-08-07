package net.rsprox.proxy.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelPipeline
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.attributes.BINARY_BLOB
import net.rsprox.proxy.attributes.BINARY_HEADER_BUILDER
import net.rsprox.proxy.attributes.SESSION_ENCODE_SEED
import net.rsprox.proxy.attributes.STREAM_CIPHER_PAIR
import net.rsprox.proxy.attributes.WORLD_ATTRIBUTE
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.worlds.World
import java.net.InetSocketAddress

/**
 * Replaces a channel handler with a new variant.
 */
public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler {
    return replace(T::class.java, newHandler::class.qualifiedName, newHandler)
}

public inline fun <reified T : ChannelHandler> ChannelPipeline.addBefore(newHandler: ChannelHandler) {
    addBefore(T::class.qualifiedName, newHandler::class.qualifiedName, newHandler)
}

public fun ChannelPipeline.addLastWithName(newHandler: ChannelHandler) {
    addLast(newHandler::class.qualifiedName, newHandler)
}

public inline fun <reified T : ChannelHandler> ChannelPipeline.remove(): ChannelHandler = remove(T::class.java)

public fun Channel.setAutoRead() {
    config().isAutoRead = true
}

public fun Channel.getWorld(): World {
    return attr(WORLD_ATTRIBUTE).get()
        ?: throw IllegalStateException("World not assigned to $this")
}

public fun Channel.getBinaryHeaderBuilder(): BinaryHeader.Builder {
    return attr(BINARY_HEADER_BUILDER).get()
        ?: throw IllegalStateException("Binary header builder not assigned to $this")
}

public fun Channel.getStreamCipherPair(): StreamCipherPair {
    return attr(STREAM_CIPHER_PAIR).get()
        ?: throw IllegalStateException("Stream cipher not assigned to $this")
}

public fun Channel.getClientToServerStreamCipher(): StreamCipher {
    return getStreamCipherPair().encoderCipher
}

public fun Channel.getServerToClientStreamCipher(): StreamCipher {
    return getStreamCipherPair().decodeCipher
}

public fun Channel.getAndDropEncodeSeed(): IntArray {
    return attr(SESSION_ENCODE_SEED).getAndSet(null)
        ?: throw IllegalStateException("Encode seed not attached to $this")
}

public fun Channel.getBinaryBlob(): BinaryBlob {
    return attr(BINARY_BLOB).get()
        ?: throw IllegalStateException("Binary blob not assigned to $this")
}

public fun Channel.removeBinaryBlob() {
    attr(BINARY_BLOB).set(null)
}

public fun Channel.getPort(): Int {
    return (localAddress() as InetSocketAddress).port
}
