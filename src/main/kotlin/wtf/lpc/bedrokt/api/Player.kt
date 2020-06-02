@file:Suppress("unused")

package wtf.lpc.bedrokt.api

import com.nukkitx.network.util.DisconnectReason
import com.nukkitx.protocol.bedrock.BedrockClient
import com.nukkitx.protocol.bedrock.BedrockPacketType
import com.nukkitx.protocol.bedrock.BedrockServerSession
import com.nukkitx.protocol.bedrock.handler.BatchHandler
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket
import wtf.lpc.bedrokt.*
import java.net.InetSocketAddress
import java.util.*

open class Player(
    bindAddress: InetSocketAddress,
    val realSession: BedrockServerSession
) : BedrockClient(bindAddress) {
    lateinit var loginStuff: LoginStuff

    val ipAddress: InetSocketAddress = realSession.address

    lateinit var gamertag: String
    lateinit var uniqueId: UUID
    lateinit var xboxId: String

    fun joinServer(hostname: String, port: Int) {
        val serverAddress = InetSocketAddress(hostname, port)

        connect(serverAddress).whenComplete { session, _ ->
            session.packetCodec = packetCodec

            finishLogin()

            proxyLogger.info("Player $gamertag was sent to $hostname:$port.")

            session.batchedHandler = BatchHandler { _, _, packets ->
                packets.forEach {
                    if (config.logPackets) proxyLogger.debug("Server -> Client: $it")

                    if (it.packetType == BedrockPacketType.SERVER_TO_CLIENT_HANDSHAKE) {
                        returnHandshake(it as ServerToClientHandshakePacket)
                    } else realSession.sendPacket(it)
                }
            }
        }.join()
    }

    fun disconnectFromProxy(reason: DisconnectReason? = null) {
        this.close()
        players.remove(bindAddress.port)
        proxyLogger.info("Player $gamertag disconnected${if (reason != null) " for reason $reason" else ""}!")
    }
}
