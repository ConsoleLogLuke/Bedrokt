@file:Suppress("unused")

package wtf.lpc.bedrokt.api

open class Plugin(
    val name: String,
    val version: String,
    val description: String,
    val authors: List<String>
) : BasePlugin {
    val logger = Logger(name)
}
