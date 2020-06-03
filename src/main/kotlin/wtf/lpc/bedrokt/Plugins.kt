package wtf.lpc.bedrokt

import wtf.lpc.bedrokt.api.EventType
import wtf.lpc.bedrokt.api.Plugin
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

val pluginDir = File("plugins")
val plugins = mutableListOf<Plugin>()

fun runArbitraryPluginCode(message: String, code: () -> Unit): Boolean {
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

fun callEvent(eventType: EventType, code: (Plugin) -> Unit) {
    plugins.forEach {
        runArbitraryPluginCode("${it.name}: An error occurred in ${eventType.methodName}.") { code(it) }
    }
}

fun reloadPlugins() {
    if (!pluginDir.exists()) pluginDir.mkdirs()

    proxyLogger.newline()
    proxyLogger.info("Reloading plugins...")

    var unloadCount = 0
    var loadCount = 0

    val reloadTime = measureTimeMillis {
        plugins.forEach {
            runArbitraryPluginCode("${it.name}: An error occurred in onUnload.") { it.onUnload() }
            plugins.remove(it)

            unloadCount++
            proxyLogger.info("Unloaded ${it.name} v${it.version}!")
        }

        for (file in pluginDir.listFiles()!!) {
            if (file.isDirectory || file.extension != "jar") continue

            val plugin = loadJarPlugin(file)
            val success = runArbitraryPluginCode(
                "${plugin.name}: An error occurred in onLoad. This plugin will not be loaded."
            ) { plugin.onLoad() }

            if (success) {
                plugins.add(plugin)

                loadCount++
                proxyLogger.info("Loaded ${plugin.name} v${plugin.version}!")
            }
        }
    } / 1000f

    val unloadWord = if (unloadCount == 1) "plugin" else "plugins"
    val loadWord = if (loadCount == 1) "plugin" else "plugins"

    proxyLogger.info("Unloaded $unloadCount $unloadWord and loaded $loadCount $loadWord in ${reloadTime}s!")
    proxyLogger.newline()
}

fun loadJarPlugin(jarFile: File): Plugin {
    val jar = JarFile(jarFile)
    val inputStream = jar.getInputStream(jar.getJarEntry("bedrokt.txt"))
    val mainClass = Scanner(inputStream).useDelimiter("\\A").next().trim()

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), {}.javaClass.classLoader)
    val pluginClass = Class.forName(mainClass, true, classLoader)

    val plugin = pluginClass.asSubclass(Plugin::class.java)
    val constructor = plugin.getConstructor()

    return constructor.newInstance()
}
