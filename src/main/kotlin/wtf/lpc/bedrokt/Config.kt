package wtf.lpc.bedrokt

import org.yaml.snakeyaml.Yaml
import java.io.File

data class Config(val proxyPort: Int, val maxPlayers: Int, val logPackets: Boolean)

val configFile = File("config.yml")
lateinit var config: Config

fun reloadConfig() {
    if (!configFile.exists()) {
        val resource = {}.javaClass.classLoader.getResource("config.yml")!!
        val defaultConfig = File(resource.file).readText()

        configFile.writeText(defaultConfig)
    }

    val yaml: Map<String, Any> = Yaml().load(configFile.readText())

    config = Config(
        yaml["proxy-port"] as Int,
        yaml["max-players"] as Int,
        yaml["log-packets"] as Boolean
    )
}
