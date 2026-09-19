package net.rsprox.proxy.rs3.binary

import io.netty.buffer.ByteBuf
import net.rsprox.proxy.binary.BinaryHeader

/** Validates the recording-only preamble; native packet decoders never receive these records. */
internal class Rs3RecordingInitialization(
    private val header: BinaryHeader,
) {
    private var lobbyReady = false
    private var gameStarted = false
    var gameReady = false
        private set
    private var expectedBlock = 0
    private var currentWorld = false
    private var currentBlock = -1
    private var blockBytes = ByteArray(0)
    private var received = 0
    private var variableBytes = 0
    private var variablesComplete = false
    private var ownIndex = -1

    fun initialization(buffer: ByteBuf) {
        require(!gameReady && buffer.readableBytes() >= 12) { "Unexpected/truncated RS3 initialization" }
        require(buffer.readUnsignedByte().toInt() == Rs3RecordingEncoding.VERSION)
        val phase = buffer.readUnsignedByte().toInt()
        require(phase in 0..1)
        val world = phase == 1
        val block = buffer.readUnsignedShort()
        val length = buffer.readInt()
        val offset = buffer.readInt()
        require(length in 1..Rs3RecordingEncoding.MAX_INITIALIZATION)
        if (currentBlock == -1) {
            require(offset == 0)
            if (world && !gameStarted) {
                require(lobbyReady && block == 0) { "Game initialization before lobby baseline" }
                gameStarted = true
                expectedBlock = 0
            }
            require(world == gameStarted && block == expectedBlock && !variablesComplete)
            require(world || !lobbyReady)
            require(if (block == 0) length in 25..255 else length <= 65535)
            if (block > 0) {
                variableBytes += length
                require(variableBytes <= Rs3RecordingEncoding.MAX_INITIALIZATION)
            }
            currentWorld = world
            currentBlock = block
            blockBytes = ByteArray(length)
            received = 0
        }
        require(world == currentWorld && block == currentBlock && length == blockBytes.size && offset == received)
        val count = buffer.readableBytes()
        require(count > 0 && count <= length - received) { "Invalid RS3 initialization chunk" }
        buffer.readBytes(blockBytes, received, count)
        received += count
        if (received != length) return
        if (block == 0) {
            require(blockBytes[0] == 0.toByte()) { "Recording contains unsanitized login data" }
            if (world) {
                ownIndex = ((blockBytes[7].toInt() and 255) shl 8) or (blockBytes[8].toInt() and 255)
                require(ownIndex == header.localPlayerIndex)
            } else {
                lobbyReady = true
            }
        } else {
            require(blockBytes[0].toInt() in 0..1)
            variablesComplete = blockBytes[0] == 1.toByte()
        }
        expectedBlock++
        currentBlock = -1
        blockBytes = ByteArray(0)
    }

    data class Transfer(
        val world: Int,
        val playerIndex: Int,
        val copiedLobby: Boolean,
    )

    fun transfer(
        buffer: ByteBuf,
        epochMillis: Long,
    ): Transfer {
        require(gameStarted && !gameReady && currentBlock == -1 && variablesComplete)
        require(buffer.readableBytes() == 32) { "Invalid LOBBY_TRANSFER size" }
        require(buffer.readUnsignedByte().toInt() == Rs3RecordingEncoding.VERSION)
        val world = buffer.readUnsignedShort()
        val port = buffer.readUnsignedShort()
        buffer.readUnsignedShort() // Lobby endpoint ID; not a game-world ID.
        val player = buffer.readUnsignedShort()
        val gameTime = buffer.readLong()
        val lobbyDuration = buffer.readLong()
        val copied = buffer.readUnsignedByte().toInt()
        val blocks = buffer.readUnsignedShort()
        val bytes = buffer.readInt()
        require(world == header.worldId && player == ownIndex && port in 1..65535 && copied in 0..1)
        require(blocks == expectedBlock - 1 && bytes == variableBytes)
        require(lobbyDuration >= 0 && gameTime == epochMillis && gameTime - lobbyDuration == header.timestamp)
        gameReady = true
        return Transfer(world, player, copied == 1)
    }

    fun packet() {
        require(lobbyReady && currentBlock == -1 && (!gameStarted || gameReady)) {
            "Native packet outside a completed RS3 login phase"
        }
    }

    fun finish() {
        require(gameReady && currentBlock == -1) { "Recording is missing its completed game-login initialization" }
    }
}
