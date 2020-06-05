@file:Suppress("unused")

package io.lavamc.bedrokt.api

import io.lavamc.bedrokt.logHistory
import io.lavamc.bedrokt.stopServer

class Logger(private val name: String) {
    private fun log(color: ChatColor, message: String, commandSender: CommandSender? = null) {
        val fullMessage = "$color-> ${ChatColor.RESET}" + name + "$color | ${ChatColor.RESET}" + message

        if (commandSender == null || commandSender is CommandSender.ConsoleSender) {
            println(fullMessage)

            PluginManager.callEvent(EventType.CONSOLE_MESSAGE) {
                it.onConsoleMessage(name, color, message)
            }
            logHistory.add(fullMessage)
        } else if (commandSender is Player) {
            commandSender.sendMessage(fullMessage)
        }
    }

    fun newline() {
        println()
        logHistory.add("")
    }

    fun info(message: String, commandSender: CommandSender? = null) = log(ChatColor.AQUA, message, commandSender)
    fun warn(message: String, commandSender: CommandSender? = null) = log(ChatColor.YELLOW, message, commandSender)
    fun error(message: String, commandSender: CommandSender? = null) = log(ChatColor.RED, message, commandSender)
    fun debug(message: String, commandSender: CommandSender? = null) = log(ChatColor.DARK_GRAY, message, commandSender)

    fun fatal(message: String) {
        log(ChatColor.RED, message)
        stopServer(1)
    }
}
