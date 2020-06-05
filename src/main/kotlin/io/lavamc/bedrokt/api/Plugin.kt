@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.lavamc.bedrokt.api

import org.yaml.snakeyaml.Yaml
import java.io.File

open class Plugin : BasePlugin {
    lateinit var name: String
    lateinit var version: String
    lateinit var description: String
    lateinit var authors: Array<String>

    lateinit var logger: Logger
    lateinit var dataDir: File

    val commands = mutableListOf<Command>()
    private val yaml = Yaml()

    constructor(name: String, version: String, description: String, authors: Array<String>) {
        this.name = name
        this.version = version
        this.description = description
        this.authors = authors

        logger = Logger(name)
        dataDir = File(PluginManager.pluginsDir, name)

        if (!dataDir.exists()) dataDir.mkdirs()
    }

    constructor(name: String, version: String, description: String, author: String) {
        Plugin(name, version, description, arrayOf(author))
    }

    fun unload() = PluginManager.unloadPlugin(this)

    fun getConfig(): Map<String, Any> {
        val configString = File(dataDir, "config.yml").readText()
        return yaml.load(configString)
    }
}
