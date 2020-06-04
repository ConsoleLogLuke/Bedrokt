package io.lavamc.bedrokt

import com.nukkitx.protocol.bedrock.BedrockServerSession
import io.lavamc.bedrokt.api.Player
import java.net.InetSocketAddress

val players = mutableMapOf<Int, Player>()

fun createPlayer(port: Int, realSession: BedrockServerSession): Player {
    val bindAddress = InetSocketAddress("0.0.0.0", port)

    val player = Player(bindAddress, realSession)
    player.bind().join()

    players[port] = player
    return player
}
