@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.lavamc.bedrokt.api

import org.yaml.snakeyaml.Yaml
import java.io.File

open class Plugin(
    val name: String,
    val version: String,
    val description: String,
    val authors: Array<String>
) : BasePlugin {
    val logger = Logger(name)
    val dataDir = File(PluginManager.pluginsDir, name)

    var defaultConfig: String? = null

    val commands = mutableListOf<Command>()
    private val yaml = Yaml()

    init {
        if (!dataDir.exists()) dataDir.mkdirs()
    }

    fun unload() = PluginManager.unloadPlugin(this)

    fun getConfig(): Map<String, Any> {
        val configString = File(dataDir, "config.yml").readText()
        return yaml.load(configString)
    }
}
