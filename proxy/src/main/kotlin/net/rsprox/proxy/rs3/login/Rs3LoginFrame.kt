package net.rsprox.proxy.rs3.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder

/** An exact login/handshake frame, forwarded without feeding the game-packet observer. */
internal class Rs3LoginFrame(
    buffer: ByteBuf,
) : DefaultByteBufHolder(buffer)
