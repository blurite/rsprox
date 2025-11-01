package net.rsprox.proxy.client

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprox.proxy.attributes.BINARY_HEADER_BUILDER
import net.rsprox.proxy.attributes.WORLD_ATTRIBUTE
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.bootstrap.BootstrapFactory
import net.rsprox.proxy.channel.getPort
import net.rsprox.proxy.channel.setAutoRead
import net.rsprox.proxy.client.prot.LoginClientProtProvider
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.target.ProxyTarget
import net.rsprox.proxy.util.ChannelConnectionHandler
import net.rsprox.proxy.worlds.LocalHostAddress
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.settings.SettingSetStore
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.io.IOException
import java.net.Inet4Address
import java.net.InetSocketAddress

public class ClientLoginInitializer(
    private val bootstrapFactory: BootstrapFactory,
    private val target: ProxyTarget,
    private val rsa: RSAPrivateCrtKeyParameters,
    private val decoderLoader: DecoderLoader,
    private val binaryWriteInterval: Int,
    private val connections: ProxyConnectionContainer,
    private val filters: PropertyFilterSetStore,
    private val settings: SettingSetStore,
) : ChannelInitializer<Channel>() {
    override fun initChannel(clientChannel: Channel) {
        val localHostAddress = getLocalHostAddress(clientChannel)
        val worldListProvider = target.worldListProvider
        val worldList = worldListProvider.get()
        val world =
            worldList.getWorld(localHostAddress)
                ?: throw IllegalStateException("Connection $clientChannel is missing a linked world: $worldList")
        logger.info { "Establishing a new connection to ${world.localHostAddress} @ ${world.activity}" }
        val clientBootstrap = bootstrapFactory.createClientBootstrap()
        val serverPort = target.config.gameServerPort
        logger.info { "Connecting to ${world.host}@$serverPort -- $localHostAddress, $world" }
        val future =
            try {
                clientBootstrap.connect(world.host, serverPort).sync()
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to connect to /${world.host}:$serverPort"
                }
                clientChannel.close()
                return
            }
        val serverChannel = future.channel()
        future.addListener(
            ChannelFutureListener { connectFuture ->
                if (!connectFuture.isSuccess) {
                    clientChannel.close()
                    serverChannel.close()
                    logger.debug {
                        "Failure to connect to the server ${world.host}/$serverPort via $clientChannel"
                    }
                    return@ChannelFutureListener
                }
                logger.debug {
                    "Successfully connected to server ${world.host}/$serverPort via $clientChannel"
                }
                clientChannel.pipeline().addLast(
                    ClientGenericDecoder(NopStreamCipher, LoginClientProtProvider),
                    ClientLoginHandler(
                        serverChannel,
                        rsa,
                        binaryWriteInterval,
                        target,
                        decoderLoader,
                        connections,
                        filters,
                        settings,
                    ),
                )
                clientChannel.pipeline().addLast(ChannelConnectionHandler(serverChannel, connections))
                serverChannel.pipeline().addLast(ChannelConnectionHandler(clientChannel, connections))
                val builder = BinaryHeader.Builder()
                val name = ClientTypeDictionary[clientChannel.getPort()]
                builder.clientName(name)
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

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        // Ignore IOExceptions as those tend to spam whenever something disconnects
        // Those exceptions are not very useful for us, but errors in our handling are.
        if (cause is IOException) {
            return
        }
        logger.error(cause) {
            "Exception in netty channel $ctx"
        }
    }

    private companion object {
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
