@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package wtf.lpc.bedrokt.api

import com.nukkitx.protocol.bedrock.BedrockClient
import com.nukkitx.protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.bedrock.BedrockPacketType
import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.handler.BatchHandler
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket
import com.nukkitx.protocol.bedrock.packet.TextPacket
import com.nukkitx.protocol.bedrock.packet.TransferPacket
import wtf.lpc.bedrokt.*
import java.net.InetSocketAddress
import java.util.*

open class Player(bindAddress: InetSocketAddress, realSession: BedrockServerSession) : BedrockClient(bindAddress) {
    class Internal(val realSession: BedrockServerSession) {
        lateinit var loginStuff: LoginStuff
    }

    val internal = Internal(realSession)

    val ipAddress: InetSocketAddress = internal.realSession.address

    lateinit var gamertag: String
    lateinit var uniqueId: UUID
    lateinit var xboxId: String

    fun joinServer(hostname: String, port: Int) {
        val serverAddress = InetSocketAddress(hostname, port)

        connect(serverAddress).whenComplete { session, _ ->
            session.packetCodec = packetCodec

            finishLogin()

            proxyLogger.info("Player $gamertag was sent to $hostname:$port.")

            session.addDisconnectHandler {
                PluginManager.callEvent(EventType.PLAYER_SERVER_DISCONNECT) {
                    it.onPlayerServerDisconnect(this, hostname, port)
                }
            }

            session.batchedHandler = BatchHandler { _, _, packets ->
                packets.forEach {
                    if (config.logPackets) proxyLogger.debug("Server -> Client: $it")

                    PluginManager.callEvent(EventType.SERVER_TO_CLIENT_PACKET) { player ->
                        player.onServerToClientPacket(this, it)
                    }

                    when (it.packetType) {
                        BedrockPacketType.SERVER_TO_CLIENT_HANDSHAKE -> {
                            returnHandshake(it as ServerToClientHandshakePacket)
                        }
                        BedrockPacketType.DISCONNECT -> disconnectFromProxy()
                        else -> sendPacketToClient(it)
                    }
                }
            }

            PluginManager.callEvent(EventType.PLAYER_SERVER_JOIN) { it.onPlayerServerJoin(this, hostname, port) }
        }.join()
    }

    fun disconnectFromProxy(reason: String? = null) {
        PluginManager.callEvent(EventType.PLAYER_PROXY_DISCONNECT) { it.onPlayerProxyDisconnect(this) }

        if (!session.isClosed) session.disconnect()

        if (reason == null) internal.realSession.disconnect()
        else internal.realSession.disconnect(reason)

        players.remove(bindAddress.port)
        proxyLogger.info("Player $gamertag disconnected!")
    }

    fun sendPacketToServer(packet: BedrockPacket) = session.sendPacketImmediately(packet)
    fun sendPacketToClient(packet: BedrockPacket) = internal.realSession.sendPacketImmediately(packet)

    fun sendToServer(hostname: String, port: Int) {
        val packet = TransferPacket()
        packet.address = hostname
        packet.port = port

        sendPacketToClient(packet)
    }

    fun sendMessage(message: String) {
        val packet = TextPacket()
        packet.platformChatId = ""
        packet.sourceName = ""
        packet.xuid = ""
        packet.type = TextPacket.Type.CHAT
        packet.isNeedsTranslation = false
        packet.message = message

        sendPacketToClient(packet)
    }
}
