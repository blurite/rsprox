package net.rsprox.proxy.client

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprot.crypto.rsa.decipherRsa
import net.rsprot.crypto.xtea.xteaDecrypt
import net.rsprox.proxy.attributes.SESSION_ENCODE_SEED
import net.rsprox.proxy.attributes.STREAM_CIPHER_PAIR
import net.rsprox.proxy.channel.addLastWithName
import net.rsprox.proxy.channel.getBinaryHeaderBuilder
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.channel.getWorld
import net.rsprox.proxy.channel.remove
import net.rsprox.proxy.channel.replace
import net.rsprox.proxy.client.prot.LoginClientProt
import net.rsprox.proxy.client.util.HostPlatformStats
import net.rsprox.proxy.client.util.LoginXteaBlock
import net.rsprox.proxy.config.getConnection
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.js5.Js5MasterIndexArchive
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.rsa.Rsa
import net.rsprox.proxy.rsa.rsa
import net.rsprox.proxy.server.ServerGameLoginDecoder
import net.rsprox.proxy.server.ServerGenericDecoder
import net.rsprox.proxy.server.ServerJs5LoginHandler
import net.rsprox.proxy.server.ServerRelayHandler
import net.rsprox.proxy.server.prot.LoginServerProtId
import net.rsprox.proxy.server.prot.LoginServerProtProvider
import net.rsprox.proxy.target.ProxyTarget
import net.rsprox.proxy.util.ChannelConnectionHandler
import net.rsprox.proxy.util.xteaEncrypt
import net.rsprox.proxy.worlds.WorldFlag
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.SettingSetStore
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters

public class ClientLoginHandler(
    private val serverChannel: Channel,
    private val rsa: RSAPrivateCrtKeyParameters,
    private val binaryWriteInterval: Int,
    private val target: ProxyTarget,
    private val decoderLoader: DecoderLoader,
    private val connections: ProxyConnectionContainer,
    private val filters: PropertyFilterSetStore,
    private val settings: SettingSetStore,
) : SimpleChannelInboundHandler<ClientPacket<LoginClientProt>>() {
    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        when (msg.prot) {
            LoginClientProt.INIT_GAME_CONNECTION -> {
                logger.debug {
                    "Init game connection"
                }
                switchServerToGameLoginDecoding(ctx)
            }
            LoginClientProt.INIT_JS5REMOTE_CONNECTION -> {
                logger.debug {
                    "Init JS5 remote connection"
                }
                switchClientToRelay(ctx)
                switchServerToJs5LoginDecoding(ctx)
            }
            LoginClientProt.GAMELOGIN -> {
                logger.debug {
                    "Game login received, re-encrypting RSA"
                }
                handleLogin(ctx, msg)
            }
            LoginClientProt.GAMERECONNECT -> {
                logger.debug {
                    "Game reconnect received, re-encrypting RSA"
                }
                handleLogin(ctx, msg)
            }
            LoginClientProt.POW_REPLY -> {
                logger.debug {
                    "Proof of Work reply received"
                }
            }
            LoginClientProt.UNKNOWN -> {
                logger.debug {
                    "Unknown login prot received"
                }
            }
            LoginClientProt.REMAINING_BETA_ARCHIVE_CRCS -> {
                logger.debug {
                    "Remaining beta archive CRCs received"
                }
                switchClientToRelay(ctx)
            }
            LoginClientProt.SSL_WEB_CONNECTION -> {
                logger.debug { "SSL Web connection received, switching to relay" }
                switchClientToRelay(ctx)
            }
        }
        try {
            serverChannel.writeAndFlush(msg.encode(ctx.alloc()))
        } finally {
            msg.payload.release()
        }
    }

    private fun handleLogin(
        ctx: ChannelHandlerContext,
        msg: ClientPacket<LoginClientProt>,
    ) {
        val builder = ctx.channel().getBinaryHeaderBuilder()
        val buffer = msg.payload.toJagByteBuf()
        val version = buffer.g4()
        if (version != target.revisionNum()) {
            throw IllegalStateException("Invalid revision for target ${target.config.name}: $version")
        }
        val subVersion = buffer.g4()
        val clientType = buffer.g1()
        val platformType = buffer.g1()
        buffer.g1()

        builder.revision(version)
        builder.subRevision(subVersion)
        builder.clientType(clientType)
        builder.platformType(platformType)
        val world = ctx.channel().getWorld()
        val port = ctx.channel().getPort()
        val masterIndex = Js5MasterIndexArchive.getJs5MasterIndex(port, world)
        if (masterIndex == null) {
            // If we can't find a JS5 master index associated to a world,
            // the proxy was likely restarted while the client was kept at login screen.
            // In order to get around it, we just tell the client that the server requested a reload
            // This just reboots the client and forces it to open a JS5 connection to the server,
            // which gets us the JS5 file that we need for our log.
            val forcedReloadBuffer = Unpooled.buffer()
            forcedReloadBuffer.p1(LoginServerProtId.OUT_OF_DATE_RELOAD)
            ctx.channel().writeAndFlush(forcedReloadBuffer)
            serverChannel.close()
            return
        }
        builder.js5MasterIndex(masterIndex)

        // The header^ will just naively be copied over
        val headerSize = 4 + 4 + 1 + 1 + 1

        val originalRsaSize = buffer.g2()
        if (!buffer.isReadable(originalRsaSize)) {
            invalidRsa(ctx)
        }
        val rsaSlice = buffer.buffer.readSlice(originalRsaSize)
        val xteaBlock = buffer.buffer.copy()
        val decryptedRsaBuffer =
            try {
                rsaSlice.rsa(rsa).toJagByteBuf()
            } catch (t: Throwable) {
                invalidRsa(ctx)
            }
        val rsaStart = decryptedRsaBuffer.readerIndex()
        val rsaCheck = decryptedRsaBuffer.g1()
        if (rsaCheck != 1) {
            invalidRsa(ctx)
        }
        val encodeSeed =
            IntArray(4) {
                decryptedRsaBuffer.g4()
            }
        val encodeSeedCopy = encodeSeed.copyOf()
        val decodeSeed =
            IntArray(encodeSeed.size) {
                encodeSeed[it] + 50
            }
        val xteaBuffer = xteaBlock.xteaDecrypt(encodeSeed).toJagByteBuf()

        val loginXteaBlock = decodeLoginXteaBlock(xteaBuffer)
        logger.debug {
            "Original login xtea block: $loginXteaBlock"
        }
        val loginXteaBlockBuf = Unpooled.buffer()
        encodeLoginXteaBlock(loginXteaBlock, loginXteaBlockBuf.toJagByteBuf())
        val encryptedXteaBuf = loginXteaBlockBuf.xteaEncrypt(encodeSeed)

        // Encoding cipher is for server -> client
        val encodingCipher = IsaacRandom(encodeSeed)
        // Decoding seed is for client -> server
        val decodingCipher = IsaacRandom(decodeSeed)
        val pair = StreamCipherPair(encodingCipher, decodingCipher)
        ctx.channel().attr(SESSION_ENCODE_SEED).set(encodeSeedCopy)
        ctx.channel().attr(STREAM_CIPHER_PAIR).set(pair)
        serverChannel.attr(STREAM_CIPHER_PAIR).set(pair)
        val encoded = ctx.alloc().buffer(msg.payload.readableBytes())
        encoded.writeBytes(msg.payload, msg.start, headerSize)
        decryptedRsaBuffer.readerIndex(rsaStart)
        val connection = getConnection(port)
        val modulus = connection.modulus
        val encrypted =
            decryptedRsaBuffer.buffer.decipherRsa(
                Rsa.PUBLIC_EXPONENT,
                modulus,
                decryptedRsaBuffer.readableBytes(),
            )
        encoded.p2(encrypted.readableBytes())
        encoded.writeBytes(encrypted)
        try {
            encoded.writeBytes(encryptedXteaBuf)
        } finally {
            encryptedXteaBuf.release()
        }
        // Swap out the original login packet with the new one
        msg.replacePayload(encoded)
        if (!world.hasFlag(WorldFlag.BETA_WORLD)) {
            // Relay packets for now, server will swap over to decoding once it's time
            switchClientToRelay(ctx)
        }
    }

    private fun decodeLoginXteaBlock(buffer: JagByteBuf): LoginXteaBlock {
        val username = buffer.gjstr()
        val packedClientSettings = buffer.g1()
        val width = buffer.g2()
        val height = buffer.g2()
        val uuid =
            ByteArray(24) {
                buffer.g1().toByte()
            }
        val siteSettings = buffer.gjstr()
        val affiliate = buffer.g4()
        val deepLinkCount = buffer.g1()
        val deepLinks =
            if (deepLinkCount == 0) {
                emptyList()
            } else {
                List(deepLinkCount) {
                    buffer.g4()
                }
            }
        val hostPlatformStats = decodeHostPlatformStats(buffer)
        val secondClientType = buffer.g1()
        val reflectionCheckerConst = buffer.g4()
        val crc = buffer.buffer.readBytes(buffer.readableBytes())
        return LoginXteaBlock(
            username,
            packedClientSettings,
            width,
            height,
            uuid,
            siteSettings,
            affiliate,
            deepLinks,
            hostPlatformStats,
            secondClientType,
            reflectionCheckerConst,
            crc,
        )
    }

    private fun encodeLoginXteaBlock(
        block: LoginXteaBlock,
        buffer: JagByteBuf,
    ) {
        buffer.pjstr(block.username)
        buffer.p1(block.packedClientSettings)
        buffer.p2(block.width)
        buffer.p2(block.height)
        for (num in block.uuid) {
            buffer.p1(num.toInt())
        }
        buffer.pjstr(block.siteSettings)
        buffer.p4(block.affiliate)
        buffer.p1(block.deepLinks.size)
        for (link in block.deepLinks) {
            buffer.p4(link)
        }
        val hostBuf = Unpooled.buffer()
        encodeHostPlatformStats(block.hostPlatformStats, hostBuf.toJagByteBuf())
        try {
            buffer.pdata(hostBuf)
        } finally {
            hostBuf.release()
        }
        buffer.p1(block.secondClientType)
        buffer.p4(block.reflectionCheckerConst)
        try {
            buffer.pdata(block.crc)
        } finally {
            block.crc.release()
        }
    }

    private fun decodeHostPlatformStats(buffer: JagByteBuf): HostPlatformStats {
        val version = buffer.g1()
        val osType = buffer.g1()
        val os64Bit = buffer.g1()
        val osVersion = buffer.g2()
        val javaVendor = buffer.g1()
        val javaVersionMajor = buffer.g1()
        val javaVersionMinor = buffer.g1()
        val javaVersionPatch = buffer.g1()
        val applet = buffer.g1()
        val javaMaxMemoryMb = buffer.g2()
        val javaAvailableProcessors = buffer.g1()
        val systemMemory = buffer.g3()
        val systemSpeed = buffer.g2()
        val gpuDxName = buffer.gjstr2()
        val gpuGlName = buffer.gjstr2()
        val gpuDxVersion = buffer.gjstr2()
        val gpuGlVersion = buffer.gjstr2()
        val gpuDriverMonth = buffer.g1()
        val gpuDriverYear = buffer.g2()
        val cpuManufacturer = buffer.gjstr2()
        val cpuBrand = buffer.gjstr2()
        val cpuCount1 = buffer.g1()
        val cpuCount2 = buffer.g1()
        val cpuFeatures =
            IntArray(3) {
                buffer.g4()
            }
        val cpuSignature = buffer.g4()
        val clientName = buffer.gjstr2()
        val deviceName = buffer.gjstr2()
        return HostPlatformStats(
            version,
            osType,
            os64Bit,
            osVersion,
            javaVendor,
            javaVersionMajor,
            javaVersionMinor,
            javaVersionPatch,
            applet,
            javaMaxMemoryMb,
            javaAvailableProcessors,
            systemMemory,
            systemSpeed,
            gpuDxName,
            gpuGlName,
            gpuDxVersion,
            gpuGlVersion,
            gpuDriverMonth,
            gpuDriverYear,
            cpuManufacturer,
            cpuBrand,
            cpuCount1,
            cpuCount2,
            cpuFeatures,
            cpuSignature,
            clientName,
            deviceName,
        )
    }

    private fun encodeHostPlatformStats(
        stats: HostPlatformStats,
        buffer: JagByteBuf,
    ) {
        buffer.p1(stats.version)
        buffer.p1(stats.osType)
        buffer.p1(stats.os64Bit)
        buffer.p2(stats.osVersion)
        buffer.p1(stats.javaVendor)
        buffer.p1(stats.javaVersionMajor)
        buffer.p1(stats.javaVersionMinor)
        buffer.p1(stats.javaVersionPatch)
        buffer.p1(stats.applet)
        buffer.p2(stats.javaMaxMemoryMb)
        buffer.p1(stats.javaAvailableProcessors)
        buffer.p3(stats.systemMemory)
        buffer.p2(stats.systemSpeed)
        buffer.pjstr2(stats.gpuDxName)
        buffer.pjstr2(stats.gpuGlName)
        buffer.pjstr2(stats.gpuDxVersion)
        buffer.pjstr2(stats.gpuGlVersion)
        buffer.p1(stats.gpuDriverMonth)
        buffer.p2(stats.gpuDriverYear)
        buffer.pjstr2(stats.cpuManufacturer)
        buffer.pjstr2(stats.cpuBrand)
        buffer.p1(stats.cpuCount1)
        buffer.p1(stats.cpuCount2)
        for (ft in stats.cpuFeatures) {
            buffer.p4(ft)
        }
        buffer.p4(stats.cpuSignature)
        buffer.pjstr2(stats.clientName)
        buffer.pjstr2(stats.deviceName)
    }

    private fun invalidRsa(ctx: ChannelHandlerContext): Nothing {
        // In the case of RSA failure, it implies the client was patched with a different RSA key
        // than what the proxy has loaded up. This can happen if someone deletes the cached proxy
        // key in their user.home/.rsprox directory.
        // In this case, we just drop the connection to the server and write a custom login response
        // to the client indicating what went wrong.
        serverChannel.close()
        val customResponseBuffer = Unpooled.buffer()
        customResponseBuffer.p1(LoginServerProtId.DISALLOWED_BY_SCRIPT)
        val index = customResponseBuffer.writerIndex()
        customResponseBuffer.p2(0)
        customResponseBuffer.pjstr("RSA out of date!")
        customResponseBuffer.pjstr("Re-open the client via the proxy.")
        customResponseBuffer.pjstr("Connection to the server has been killed.")
        val end = customResponseBuffer.writerIndex()
        val length = end - index - 2
        customResponseBuffer.writerIndex(index)
        customResponseBuffer.p2(length)
        customResponseBuffer.writerIndex(end)
        ctx.channel().writeAndFlush(customResponseBuffer).await()
        throw IllegalStateException("Invalid RSA")
    }

    private fun switchClientToRelay(ctx: ChannelHandlerContext) {
        val clientPipeline = ctx.channel().pipeline()
        clientPipeline.remove<ClientGenericDecoder<*>>()
        clientPipeline.replace<ClientLoginHandler>(ClientRelayHandler(serverChannel))
    }

    private fun switchServerToJs5LoginDecoding(ctx: ChannelHandlerContext) {
        val pipeline = serverChannel.pipeline()
        pipeline.addLastWithName(ServerGenericDecoder(NopStreamCipher, LoginServerProtProvider))
        pipeline.addLastWithName(ServerJs5LoginHandler(ctx.channel()))
        pipeline.addLastWithName(ChannelConnectionHandler(serverChannel))
    }

    private fun switchServerToGameLoginDecoding(ctx: ChannelHandlerContext) {
        val pipeline = serverChannel.pipeline()
        pipeline.addLastWithName(
            ServerGameLoginDecoder(
                ctx.channel(),
                binaryWriteInterval,
                target,
                decoderLoader,
                connections,
                filters,
                settings,
            ),
        )
        pipeline.addLastWithName(ServerRelayHandler(ctx.channel()))
        pipeline.addLastWithName(ChannelConnectionHandler(serverChannel))
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
