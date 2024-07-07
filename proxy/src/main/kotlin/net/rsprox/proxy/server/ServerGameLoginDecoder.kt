package net.rsprox.proxy.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.proxy.attributes.BINARY_BLOB
import net.rsprox.proxy.attributes.BINARY_HEADER_BUILDER
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryStream
import net.rsprox.proxy.channel.getBinaryHeaderBuilder
import net.rsprox.proxy.channel.getServerToClientStreamCipher
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.util.UserUid

public class ServerGameLoginDecoder(
    private val clientChannel: Channel,
) : ByteToMessageDecoder() {
    private enum class State {
        AWAITING_GAME_CONNECTION_REPLY,
        AWAITING_SESSION_ID,
        AWAITING_LOGIN_RESPONSE,
        LOGIN_OK_READ_DATA_LENGTH,
        LOGIN_OK_READ_DATA,
        POW_READ_LENGTH,
        POW_READ,
        DISALLOWED_READ_LENGTH,
        DISALLOWED_READ,
        TOKENS_READ_LENGTH,
        TOKENS_READ,
        RECONNECT_OK_READ_DATA,
    }

    private var state: State = State.AWAITING_GAME_CONNECTION_REPLY
    private var stateValue: Int = -1

    override fun decode(
        ctx: ChannelHandlerContext,
        inputNettyBuf: ByteBuf,
        out: MutableList<Any>,
    ) {
        val input = inputNettyBuf.toJagByteBuf()
        if (state == State.AWAITING_GAME_CONNECTION_REPLY) {
            val response = input.g1()
            val future =
                writeToClient {
                    p1(response)
                }
            if (response != 0) {
                future.addListener(ChannelFutureListener.CLOSE)
                ctx.close()
                return
            }
            state = State.AWAITING_SESSION_ID
        }
        if (state == State.AWAITING_SESSION_ID) {
            if (!input.isReadable(8)) {
                return
            }
            val sessionId = input.g8()
            writeToClient {
                p8(sessionId)
            }
            this.stateValue = -1
            state = State.AWAITING_LOGIN_RESPONSE
        }
        if (state == State.AWAITING_LOGIN_RESPONSE) {
            if (this.stateValue == -1) {
                if (!input.isReadable) {
                    return
                }
                val responseCode = input.g1()
                writeToClient {
                    p1(responseCode)
                }
                this.stateValue = responseCode
            }
            when (this.stateValue) {
                61 -> {
                    if (!input.isReadable) {
                        return
                    }
                    val value = input.g1()
                    writeToClient {
                        p1(value)
                    }
                    this.stateValue = -1
                    return
                }
                2 -> {
                    // Beta does have different handling in the client, but it doesn't impact this section
                    // That entire procedure just flows through the JS5 channel, eventually jumps back to login
                    state = State.LOGIN_OK_READ_DATA_LENGTH
                }
                15 -> {
                    state = State.RECONNECT_OK_READ_DATA
                }
                64 -> {
                    state = State.TOKENS_READ_LENGTH
                }
                23 -> {
                    return closeSession(ctx)
                }
                29 -> {
                    state = State.DISALLOWED_READ_LENGTH
                }
                69 -> {
                    state = State.POW_READ_LENGTH
                }
                else -> {
                    // login error
                    return closeSession(ctx)
                }
            }
        }
        if (state == State.TOKENS_READ_LENGTH) {
            if (!input.isReadable) return
            stateValue = input.g1()
            writeToClient {
                p1(stateValue)
            }
            state = State.TOKENS_READ
        }
        if (state == State.TOKENS_READ) {
            if (!input.isReadable(stateValue)) return
            val data = ByteArray(stateValue)
            input.gdata(data)
            writeToClient {
                pdata(data)
            }
            state = State.AWAITING_LOGIN_RESPONSE
            stateValue = -1
        }
        if (state == State.DISALLOWED_READ_LENGTH) {
            if (!input.isReadable(2)) return
            stateValue = input.g2()
            writeToClient {
                p2(stateValue)
            }
            state = State.DISALLOWED_READ
        }
        if (state == State.DISALLOWED_READ) {
            if (!input.isReadable(stateValue)) return
            val mes1 = input.gjstr()
            val mes2 = input.gjstr()
            val mes3 = input.gjstr()
            writeToClient {
                pjstr(mes1)
                pjstr(mes2)
                pjstr(mes3)
            }
            return closeSession(ctx)
        }
        if (state == State.POW_READ_LENGTH) {
            if (!input.isReadable(2)) return
            stateValue = input.g2()
            writeToClient {
                p2(stateValue)
            }
            state = State.POW_READ
        }
        if (state == State.POW_READ) {
            if (!input.isReadable(stateValue)) return
            val challenge = ByteArray(stateValue)
            input.gdata(challenge)
            writeToClient {
                pdata(challenge)
            }
            state = State.AWAITING_LOGIN_RESPONSE
            stateValue = -1
        }
        if (state == State.LOGIN_OK_READ_DATA_LENGTH) {
            if (!input.isReadable) return
            stateValue = input.g1()
            writeToClient {
                p1(stateValue)
            }
            if (stateValue != 37) {
                return closeSession(ctx)
            }
            state = State.LOGIN_OK_READ_DATA
        }
        if (state == State.LOGIN_OK_READ_DATA) {
            if (!input.isReadable(stateValue)) {
                return
            }
            val authenticator = input.g1()
            val encryptedAuthenticatorCode = input.g4()
            if (authenticator == 1) {
                val cipher = ctx.channel().getServerToClientStreamCipher()
                // Increment the cipher by 4 values, as each of the auth code bytes is encrypted
                for (i in 0..<4) {
                    cipher.nextInt()
                }
            }
            val staffModLevel = input.g1()
            val playerMod = input.gboolean()
            val localPlayerIndex = input.g2()
            val members = input.gboolean()
            val accountHash = input.g8()
            val userId = input.g8()
            val userHash = input.g8()
            val userUid = UserUid(userId, userHash)
            val builder = ctx.channel().getBinaryHeaderBuilder()
            builder.twoFactorCodeUsed(authenticator == 1)
            builder.localPlayerIndex(localPlayerIndex)
            builder.accountHash(userUid.hash)
            val timestamp = System.currentTimeMillis()
            builder.timestamp(timestamp)
            val nanoTimestamp = System.nanoTime()
            val header = builder.build()
            val stream = BinaryStream(Unpooled.buffer(1_000_000), nanoTimestamp)
            val blob = BinaryBlob(header, stream)
            val serverChannel = ctx.channel()
            // Remove the binary header builder, nothing should be trying to update it from here on out
            serverChannel.attr(BINARY_HEADER_BUILDER).set(null)
            clientChannel.attr(BINARY_HEADER_BUILDER).set(null)
            // Set the binary blob in place, this will periodically flush & save on disk
            serverChannel.attr(BINARY_BLOB).set(blob)
            clientChannel.attr(BINARY_BLOB).set(blob)
            writeToClient {
                p1(authenticator)
                p4(encryptedAuthenticatorCode)
                p1(staffModLevel)
                pboolean(playerMod)
                p2(localPlayerIndex)
                pboolean(members)
                p8(accountHash)
                p8(userId)
                p8(userHash)
            }
            val pipeline = ctx.pipeline()
            // Just stick to relaying the messages now
            pipeline.remove<ServerGameLoginDecoder>()
        }
    }

    private fun closeSession(ctx: ChannelHandlerContext) {
        closeChannel(ctx.channel())
        closeChannel(clientChannel)
    }

    private fun closeChannel(channel: Channel) {
        if (channel.eventLoop().inEventLoop()) {
            channel.close()
        } else {
            channel.eventLoop().submit {
                channel.closeFuture()
            }
        }
    }

    private inline fun writeToClient(function: JagByteBuf.() -> Unit): ChannelFuture {
        val buffer = clientChannel.alloc().buffer().toJagByteBuf()
        function(buffer)
        return clientChannel.writeAndFlush(buffer.buffer)
    }
}
