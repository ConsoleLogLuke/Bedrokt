@file:Suppress("unused")

package wtf.lpc.bedrokt.api

import wtf.lpc.bedrokt.players
import wtf.lpc.bedrokt.stopServer
import java.util.*

class Bedrokt {
    companion object {
        fun getPlayerList() = players.values

        fun broadcastMessage(message: String) = players.values.forEach { it.sendMessage(message) }

        fun getPlayerByGamertag(gamertag: String) = players.values.firstOrNull { it.gamertag == gamertag }
        fun getPlayerByUniqueId(uniqueId: UUID) = players.values.firstOrNull { it.uniqueId == uniqueId }
        fun getPlayerByXboxId(xboxId: String) = players.values.firstOrNull { it.xboxId == xboxId }

        fun stopProxy(statusCode: Int = 0) = stopServer(statusCode)
    }
}
