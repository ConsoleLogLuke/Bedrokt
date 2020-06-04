@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.lavamc.bedrokt.api

enum class ChatColor(val code: Char) {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),

    MAGIC('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),

    RESET('r');

    override fun toString() = "\u00a7$code"

    companion object {
        fun fromString(string: String, colorChar: Char): String {
            val regex = Regex("$colorChar(?=[A-Fa-fKkL-Ol-oRr0-9])")
            return string.replace(regex, "\u00A7")
        }
    }
}
