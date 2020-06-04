@file:Suppress("unused")

package io.lavamc.bedrokt.api

import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor
import io.lavamc.bedrokt.logHistory
import io.lavamc.bedrokt.stopServer

class Logger(private val name: String) {
    private fun log(color: Color, message: String, commandSender: CommandSender? = null) {
        val fullMessage = Kolor.foreground("-> ", color) + name + Kolor.foreground(" | ", color) + message

        if (commandSender == null || commandSender is CommandSender.ConsoleSender) {
            println(fullMessage)

            PluginManager.callEvent(EventType.CONSOLE_MESSAGE) {
                it.onConsoleMessage(
                    name,
                    color,
                    message
                )
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

    fun info(message: String, commandSender: CommandSender? = null) = log(Color.CYAN, message, commandSender)
    fun warn(message: String, commandSender: CommandSender? = null) = log(Color.YELLOW, message, commandSender)
    fun error(message: String, commandSender: CommandSender? = null) = log(Color.RED, message, commandSender)
    fun debug(message: String, commandSender: CommandSender? = null) = log(Color.DARK_GRAY, message, commandSender)

    fun fatal(message: String) {
        log(Color.RED, message)
        stopServer(1)
    }
}
