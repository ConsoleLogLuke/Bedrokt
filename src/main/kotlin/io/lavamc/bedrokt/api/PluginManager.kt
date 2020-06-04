@file:Suppress("MemberVisibilityCanBePrivate")

package io.lavamc.bedrokt.api

import io.lavamc.bedrokt.proxyLogger
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

class PluginManager {
    companion object {
        val pluginsDir = File("plugins")
        val plugins = mutableListOf<Plugin>()

        private fun runArbitraryPluginCode(message: String, code: () -> Unit): Boolean {
            return try {
                code()
                true
            } catch (e: Throwable) {
                e.printStackTrace()

                proxyLogger.error(message)
                proxyLogger.error("The error above was likely caused by a plugin and not Bedrokt.")

                false
            }
        }

        private fun loadJarPlugin(jarFile: File): Plugin {
            val jar = JarFile(jarFile)
            val inputStream = jar.getInputStream(jar.getJarEntry("bedrokt.txt"))
            val mainClass = Scanner(inputStream).useDelimiter("\\A").next().trim()

            val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), {}.javaClass.classLoader)
            val pluginClass = Class.forName(mainClass, true, classLoader)

            val plugin = pluginClass.asSubclass(Plugin::class.java)
            val constructor = plugin.getConstructor()

            return constructor.newInstance()
        }

        fun loadPlugin(file: File): Boolean {
            val plugin = when (file.extension) {
                "jar" -> loadJarPlugin(file)
                else -> loadJarPlugin(file)
            }

            val success = runArbitraryPluginCode(
                "${plugin.name}: An error occurred in onLoad. This plugin will not be loaded."
            ) { plugin.onLoad() }

            if (success) {
                plugins.add(plugin)
                proxyLogger.info("Loaded ${plugin.name} v${plugin.version}!")
            }

            return success
        }

        fun unloadPlugin(plugin: Plugin) {
            runArbitraryPluginCode("${plugin.name}: An error occurred in onUnload.") { plugin.onUnload() }
            plugins.remove(plugin)
            proxyLogger.info("Unloaded ${plugin.name} v${plugin.version}!")
        }

        fun reloadPlugins() {
            if (!pluginsDir.exists()) pluginsDir.mkdirs()
            proxyLogger.info("Reloading plugins...")

            var unloadCount = 0
            var loadCount = 0

            val reloadTime = measureTimeMillis {
                plugins.forEach {
                    unloadPlugin(it)
                    unloadCount++
                }

                for (file in pluginsDir.listFiles()!!) {
                    if (file.isDirectory || file.extension !in listOf("jar")) continue

                    if (loadPlugin(file)) loadCount++
                }
            } / 1000f

            val unloadWord = if (unloadCount == 1) "plugin" else "plugins"
            val loadWord = if (loadCount == 1) "plugin" else "plugins"

            proxyLogger.info("Unloaded $unloadCount $unloadWord and loaded $loadCount $loadWord in ${reloadTime}s!")
        }

        fun callEvent(eventType: EventType, code: (Plugin) -> Unit) {
            plugins.forEach {
                runArbitraryPluginCode("${it.name}: An error occurred in ${eventType.methodName}.") {
                    code(it)
                }
            }
        }
    }
}
