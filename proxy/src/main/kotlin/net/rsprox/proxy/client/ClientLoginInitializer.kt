package net.rsprox.proxy.client

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprox.proxy.attributes.BINARY_HEADER_BUILDER
import net.rsprox.proxy.attributes.WORLD_ATTRIBUTE
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.channel.setAutoRead
import net.rsprox.proxy.client.prot.LoginClientProtProvider
import net.rsprox.proxy.worlds.LocalHostAddress
import net.rsprox.proxy.worlds.WorldListProvider
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetSocketAddress

public class ClientLoginInitializer(
    private val bootstrapFactory: BootstrapFactory,
    private val worldListProvider: WorldListProvider,
    private val rsa: RSAPrivateCrtKeyParameters,
    private val originalModulus: BigInteger,
) : ChannelInitializer<Channel>() {
    override fun initChannel(clientChannel: Channel) {
        val localHostAddress = getLocalHostAddress(clientChannel)
        val worldList = worldListProvider.get()
        val world =
            worldList.getWorld(localHostAddress)
                ?: throw IllegalStateException("Connection $clientChannel is missing a linked world: $worldList")
        logger.info { "Establishing a new connection to ${world.localHostAddress} @ ${world.activity}" }
        val clientBootstrap = bootstrapFactory.createClientBootstrap()
        logger.info { "Connecting to ${world.host}@$SERVER_PORT -- $localHostAddress, $world" }
        val future = clientBootstrap.connect(world.host, SERVER_PORT).sync()
        val serverChannel = future.channel()
        future.addListener(
            ChannelFutureListener { connectFuture ->
                if (!connectFuture.isSuccess) {
                    clientChannel.close()
                    serverChannel.close()
                    logger.debug {
                        "Failure to connect to the server ${world.host}/$SERVER_PORT via $clientChannel"
                    }
                    return@ChannelFutureListener
                }
                logger.debug {
                    "Successfully connected to server ${world.host}/$SERVER_PORT via $clientChannel"
                }
                clientChannel.pipeline().addLast(
                    ClientGenericDecoder(NopStreamCipher, LoginClientProtProvider),
                    ClientLoginHandler(serverChannel, rsa, originalModulus),
                )
                val builder = BinaryHeader.Builder()
                // TODO: Client name must be passed on from the patcher eventually
                builder.clientName("Deobfuscated Java Client")
                builder.headerVersion(BinaryHeader.HEADER_VERSION)
                builder.world(world)
                clientChannel.attr(WORLD_ATTRIBUTE).set(world)
                serverChannel.attr(WORLD_ATTRIBUTE).set(world)
                clientChannel.attr(BINARY_HEADER_BUILDER).set(builder)
                serverChannel.attr(BINARY_HEADER_BUILDER).set(builder)
                clientChannel.setAutoRead()
                serverChannel.setAutoRead()
            },
        )
    }

    private companion object {
        private const val SERVER_PORT: Int = 43594
        private val logger = InlineLogger()

        private fun getLocalHostAddress(channel: Channel): LocalHostAddress {
            val remoteAddress = channel.localAddress()
            if (remoteAddress !is InetSocketAddress) {
                throw IllegalArgumentException("Channel is not InetSocketAddress")
            }
            val address = remoteAddress.address
            if (address !is Inet4Address) {
                throw IllegalArgumentException("Address is not IPv4: $address")
            }
            return LocalHostAddress(address.hostAddress)
        }
    }
}
