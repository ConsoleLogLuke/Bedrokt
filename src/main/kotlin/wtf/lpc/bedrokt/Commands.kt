package wtf.lpc.bedrokt

import wtf.lpc.bedrokt.api.Bedrokt
import wtf.lpc.bedrokt.api.Player

abstract class Command(val name: String, val description: String) {
    abstract fun consoleExecute()
    abstract fun inGameExecute(player: Player)
}

class HelpCommand : Command("help", "Displays this help menu") {
    private fun execute(commandPrefix: String, func: (String) -> Unit) {
        func("List of Bedrokt commands:")
        commands.forEach {
            func("  $commandPrefix${it.name} - ${it.description}")
        }
    }

    override fun consoleExecute() = execute("/") { proxyLogger.info(it) }
    override fun inGameExecute(player: Player) = execute("/bedrokt ") { player.sendMessage(it) }
}

class VersionCommand : Command("version", "Displays the current Bedrokt version") {
    override fun consoleExecute() = proxyLogger.info(fullBedroktVersion)
    override fun inGameExecute(player: Player) = player.sendMessage(fullBedroktVersion)
}

class ListCommand : Command("list", "Lists all players currently connected to the proxy") {
    private fun execute(func: (String) -> Unit) {
        val players = Bedrokt.getPlayerList()

        val isWord = if (players.size == 1) "is" else "are"
        val playerWord = if (players.size == 1) "player" else "players"
        val colonOrPeriod = if (players.isEmpty()) "." else ":"

        func("There $isWord currently ${players.size} connected $playerWord$colonOrPeriod")
        if (players.isNotEmpty()) func(players.joinToString { it.gamertag })
    }

    override fun consoleExecute() = execute { proxyLogger.info(it) }
    override fun inGameExecute(player: Player) = execute { player.sendMessage(it) }
}

class StopCommand : Command("stop", "Stops the proxy") {
    override fun consoleExecute() = Bedrokt.stopProxy(0)
    override fun inGameExecute(player: Player) = Bedrokt.stopProxy(0)
}

class ReloadCommand : Command("reload", "Reloads all plugins") {
    override fun consoleExecute() = reloadPlugins()
    override fun inGameExecute(player: Player) = reloadPlugins()
}

val commands = listOf(
    HelpCommand(),
    VersionCommand(),
    ListCommand(),
    StopCommand(),
    ReloadCommand()
)

fun getCommand(name: String) = commands.firstOrNull { it.name == name.toLowerCase().trim() } ?: HelpCommand()
