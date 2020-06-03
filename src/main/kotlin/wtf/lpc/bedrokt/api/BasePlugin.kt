package wtf.lpc.bedrokt.api

import com.nukkitx.protocol.bedrock.BedrockPacket

interface BasePlugin {
    fun onLoad() {}
    fun onUnload() {}

    fun onProxyStart() {}

    fun onPlayerProxyJoin(player: Player) {}
    fun onPlayerServerJoin(player: Player, serverHostname: String, serverPort: Int) {}

    fun onPlayerProxyDisconnect(player: Player) {}
    fun onPlayerServerDisconnect(player: Player, serverHostname: String, serverPort: Int) {}

    fun onClientToServerPacket(player: Player, packet: BedrockPacket) {}
    fun onServerToClientPacket(player: Player, packet: BedrockPacket) {}
}
