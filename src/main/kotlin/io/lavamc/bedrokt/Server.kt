package io.lavamc.bedrokt

import com.nukkitx.protocol.bedrock.*
import com.nukkitx.protocol.bedrock.handler.BatchHandler
import com.nukkitx.protocol.bedrock.packet.CommandRequestPacket
import com.nukkitx.protocol.bedrock.packet.LoginPacket
import io.lavamc.bedrokt.api.EventType
import io.lavamc.bedrokt.api.PluginManager
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

lateinit var server: Server

class Server(bindAddress: InetSocketAddress) : BedrockServer(bindAddress), BedrockServerEventHandler {
    fun start() {
        handler = this
        server.bind().join()
    }

    override fun onConnectionRequest(address: InetSocketAddress) = true

    override fun onQuery(address: InetSocketAddress): BedrockPong {
        val pong = BedrockPong()

        pong.edition = "MCPE"
        pong.gameType = "Survival"
        pong.isNintendoLimited = false

        pong.protocolVersion = packetCodec.protocolVersion
        pong.version = "1.14.0.60"

        pong.playerCount = players.size
        pong.maximumPlayerCount = if (config.maxPlayers < 1) pong.playerCount + 1 else config.maxPlayers

        pong.motd = "A Bedrokt proxy"
        pong.subMotd = "Test"

        pong.ipv4Port = 19132

        return pong
    }

    override fun onSessionCreation(session: BedrockServerSession) {
        session.packetCodec = packetCodec

        proxyLogger.info("Login attempt from ${session.address.address.hostAddress}...")

        var playerPort: Int

        do playerPort = Random.nextInt(20000, 60000)
        while (playerPort in players.keys)

        createPlayer(playerPort, session)

        session.addDisconnectHandler {
            players[playerPort]?.disconnectFromProxy()
        }

        session.batchedHandler = BatchHandler { _, _, packets ->
            packets.forEach {
                if (config.logPackets && it.packetType !in config.ignoredPackets) {
                    proxyLogger.debug("Client -> Server: $it")
                }

                PluginManager.callEvent(EventType.CLIENT_TO_SERVER_PACKET) { plugin ->
                    plugin.onClientToServerPacket(players[playerPort]!!, it)
                }

                when (it.packetType) {
                    BedrockPacketType.LOGIN -> {
                        players[playerPort]?.login(it as LoginPacket)
                    }

                    BedrockPacketType.COMMAND_REQUEST -> {
                        val commandPacket = it as CommandRequestPacket
                        val commandString = commandPacket.command.trim().removePrefix("/")

                        executeCommand(
                            commandString,
                            players[playerPort]!!
                        )
                    }

                    else -> players[playerPort]?.sendPacketToServer(it)
                }
            }
        }
    }
}

fun startServer() {
    proxyLogger.info("Starting server...")

    val localIp = InetAddress.getLocalHost().hostAddress
    val port = config.proxyPort
    val bindAddress = InetSocketAddress(localIp, port)

    val startTime = measureTimeMillis {
        server = Server(bindAddress)
        server.start()
    } / 1000f

    proxyLogger.info("Server started on $localIp:$port in ${startTime}s!")
    PluginManager.callEvent(EventType.PROXY_START) { it.onProxyStart() }
}

fun stopServer(statusCode: Int = 0) {
    proxyLogger.info("Stopping server; unloading plugins...")

    PluginManager.plugins.forEach { it.unload() }
    players.values.forEach { it.disconnectFromProxy("Proxy is shutting down!") }

    server.close()
    saveLog()

    proxyLogger.info("Goodbye!")
    exitProcess(statusCode)
}
