package io.lavamc.bedrokt

import io.lavamc.bedrokt.api.Logger
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val logHistory = mutableListOf<String>()
val proxyLogger = Logger("Proxy")

fun saveLog() {
    proxyLogger.info("Saving log...")

    val colorRegex = Regex("\\033\\[[0-9]*m")
    val logString = logHistory.joinToString("\n") { it.replace(colorRegex, "") }

    val dateTime = LocalDateTime.now()
    val dateName = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val timeName = dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)

    val logDir = File("logs", dateName)
    if (!logDir.exists()) logDir.mkdirs()

    val logFile = File(logDir, "$timeName.txt")
    logFile.writeText(logString)
}
