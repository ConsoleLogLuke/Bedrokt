@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package wtf.lpc.bedrokt.api

abstract class Command(
    val name: String,
    val description: String,
    val usage: String,
    val aliases: Array<String>,
    val argCount: IntRange = 0..Int.MAX_VALUE
) {
    abstract fun runCommand(sender: CommandSender, args: Array<String>, alias: String): Boolean

    fun checkAndRun(logger: Logger, sender: CommandSender, args: Array<String>, alias: String) {
        fun incorrectUsage() {
            val fullUsage = usage
                .replace("/<command>", "/$name")
                .replace("/<alias>", "/$alias")

            logger.error("Usage: $fullUsage")
        }

        if (args.size !in argCount || !runCommand(sender, args, alias)) {
            incorrectUsage()
            return
        }
    }
}
