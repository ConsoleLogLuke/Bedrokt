package io.lavamc.bedrokt

import io.lavamc.bedrokt.api.ChatColor

val lavaLogo = listOf(
    "       #@@@@@@@@@@@/       ",
    "    @@               @@    ",
    "  @@                   @@  ",
    " @  @@@@@@@@@@@@@@@@@@@  @ ",
    "@@@@@@@@@@@@@@@@@@@@@@@@@@@",
    " @@@@@@@@@@@@@@@@@@@@@@@@@ ",
    "@    @@@@@@@@@@@@@@@@@    @",
    "@@@                    @@@@",
    "@@@@@@@@@@      @@     @@@@",
    "@&&&&&&&&@      &@@   @&&&@",
    "@&&&&&&&&@      &&&&&&&&&&@",
    "@&&&&&&&&&@    @&&&&&&&&&&@",
    "/&&&&&&&&&&&&&&&&&&&&&&&&&.",
    "   &&&&&&&&&&&&&&&&&&&&&   ",
    "          &&&&&&&          "
)

val lavaLogoText = listOf(
    "",
    "",
    "",
    "${ChatColor.AQUA}${ChatColor.UNDERLINE}Bedrokt v$bedroktVersion",
    "A LavaMC project",
    "",
    "https://github.com/ConsoleLogLuke/Bedrokt",
    "",
    "Copyright (c) 2020 LavaMC. All rights reserved.",
    "",
    "This work is licensed under the terms of the MIT license.",
    "For a copy, see <https://opensource.org/licenses/MIT>.",
    "",
    "",
    ""
)

fun printCoolLogo() {
    println("\n")

    val lines = lavaLogo
        .map { "   ${ChatColor.RED}$it${ChatColor.RESET}" }
        .toMutableList()

    lavaLogoText.withIndex().forEach {
        lines[it.index] += "    ${it.value}"
    }

    lines.forEach { println(it) }
    println("\n")
}
