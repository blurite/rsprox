package net.rsprox.proxy.util

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.gdata
import net.rsprot.buffer.extensions.p8
import java.security.MessageDigest

@JvmInline
public value class UserUid private constructor(
    public val hash: ByteArray,
) {
    public constructor(
        userId: Long,
        userHash: Long,
    ) : this(
        hash(
            userId,
            userHash,
        ),
    )

    private companion object {
        private fun hash(
            userId: Long,
            userHash: Long,
        ): ByteArray {
            val buffer = Unpooled.buffer(Long.SIZE_BYTES + Long.SIZE_BYTES)
            // User id is an incrementing value; As of writing this comment, there are somewhere between
            // 300-400m users, meaning the userId value for any new accounts would be in that range
            // This value is not sensitive in any way, but it is constant.
            buffer.p8(userId)
            // User hash is an actual hash provided by Jagex, unique for a given account regardless of the world.
            // While hash on its own is not useful, there is a potential security concern in how these hashes
            // are generated. As such, we take an extra step and salt it with the user id, then hash the
            // value once more. Due to the function turning 128 bits of data to 256 bits of data,
            // the probability of collisions is extremely thin.
            buffer.p8(userHash)
            val input = ByteArray(buffer.readableBytes())
            buffer.gdata(input)
            // Take the combined byte array and hash it with a SHA-256 hashing function.
            // This effectively ensures no one will be able to reverse the original input values,
            // while still ensuring we can match multiple play sessions to a single user account.
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(input)
            return messageDigest.digest()
        }
    }
}
