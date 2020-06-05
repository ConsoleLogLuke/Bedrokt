package io.lavamc.bedrokt

import io.lavamc.bedrokt.api.*

abstract class InternalCommand(val name: String, val description: String) {
    abstract fun consoleExecute()
    abstract fun inGameExecute(player: Player)
}

class HelpCommand : InternalCommand("help", "Displays this help menu") {
    private fun execute(commandPrefix: String, func: (String) -> Unit) {
        func("List of Bedrokt commands:")
        commands.forEach {
            func("  $commandPrefix${it.name} - ${it.description}")
        }
    }

    override fun consoleExecute() = execute("/") { proxyLogger.info(it) }
    override fun inGameExecute(player: Player) = execute("/bedrokt ") { proxyLogger.info(it, player) }
}

class VersionCommand : InternalCommand("version", "Displays the current Bedrokt version") {
    override fun consoleExecute() = proxyLogger.info(fullBedroktVersion)
    override fun inGameExecute(player: Player) = proxyLogger.info(
        fullBedroktVersion, player)
}

class ListCommand : InternalCommand("list", "Lists all players currently connected to the proxy") {
    private fun execute(func: (String) -> Unit) {
        val players = Bedrokt.getPlayerList()

        val isWord = if (players.size == 1) "is" else "are"
        val playerWord = if (players.size == 1) "player" else "players"
        val colonOrPeriod = if (players.isEmpty()) "." else ":"

        func("There $isWord currently ${players.size} connected $playerWord$colonOrPeriod")
        if (players.isNotEmpty()) func(players.joinToString { it.gamertag })
    }

    override fun consoleExecute() = execute { proxyLogger.info(it) }
    override fun inGameExecute(player: Player) = execute { proxyLogger.info(it, player) }
}

class StopCommand : InternalCommand("stop", "Stops the proxy") {
    override fun consoleExecute() = Bedrokt.stopProxy(0)

    override fun inGameExecute(player: Player) = proxyLogger.error(
        "Currently the proxy can only be stopped from the console! This will be changed in the future.", player
    )
}

class ReloadCommand : InternalCommand("reload", "Reloads all plugins") {
    override fun consoleExecute() = PluginManager.reloadPlugins()

    override fun inGameExecute(player: Player) = proxyLogger.error(
        "Currently plugins can only be reloaded from the console! This will be changed in the future.", player
    )
}

val commands = listOf(
    HelpCommand(),
    VersionCommand(),
    ListCommand(),
    StopCommand(),
    ReloadCommand()
)

fun getCommand(name: String) = commands.firstOrNull { it.name == name } ?: HelpCommand()

fun getPluginCommand(name: String): Pair<Plugin, Command>? {
    var plugin: Plugin? = null
    var command: Command? = null

    PluginManager.plugins.forEach { pl ->
        pl.commands.forEach {
            if (name == it.name || name in it.aliases) {
                plugin = pl
                command = it
            }
        }
    }

    return if (command != null) Pair(plugin!!, command!!)
    else null
}

fun executeCommand(string: String, sender: CommandSender) {
    PluginManager.callEvent(EventType.COMMAND_PREPROCESS) { it.onCommandPreprocess(sender, string) }

    if (string.startsWith("bedrokt ")) {
        val actualCommand = string.removePrefix("bedrokt ")
        val command = getCommand(actualCommand)

        if (sender is CommandSender.ConsoleSender) command.consoleExecute()
        else if (sender is Player) command.inGameExecute(sender)
    } else {
        val split = string.split(" ")
        if (split.isEmpty()) return

        val actualCommand = split[0].toLowerCase().trim()
        val command = getPluginCommand(actualCommand) ?: return

        val args = split.drop(1).toTypedArray()
        command.second.checkAndRun(command.first.logger, sender, args, actualCommand)
    }
}
