@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.lavamc.bedrokt.api

enum class ChatColor(val code: Char, val ansiCode: Int) {
    BLACK('0', 30),
    DARK_BLUE('1', 34),
    DARK_GREEN('2', 32),
    DARK_AQUA('3', 36),
    DARK_RED('4', 31),
    DARK_PURPLE('5', 35),
    GOLD('6', 33),
    GRAY('7', 37),
    DARK_GRAY('8', 90),
    BLUE('9', 34),
    GREEN('a', 32),
    AQUA('b', 36),
    RED('c', 31),
    LIGHT_PURPLE('d', 35),
    YELLOW('e', 33),
    WHITE('f', 97),

    MAGIC('k', 5),
    BOLD('l', 1),
    STRIKETHROUGH('m', 9),
    UNDERLINE('n', 4),
    ITALIC('o', 3),

    RESET('r', 0);

    override fun toString() = "$inGameChar$code"

    companion object {
        const val inGameChar = '\u00a7'
        const val consoleChar = '\u001B'

        fun fromString(string: String, colorChar: Char): String {
            val regex = Regex("$colorChar(?=[A-Fa-fKkL-Ol-oRr0-9])")
            return string.replace(regex, inGameChar.toString())
        }
    }
}
