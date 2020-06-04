package wtf.lpc.bedrokt

import com.nukkitx.protocol.bedrock.BedrockPacketType
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

data class Config(
    val proxyPort: Int,
    val maxPlayers: Int,
    val logPackets: Boolean,
    val ignoredPackets: List<BedrockPacketType>
)

val configFile = File("config.yml")
lateinit var config: Config

fun reloadConfig() {
    if (!configFile.exists()) {
        val resource = {}.javaClass.classLoader.getResourceAsStream("config.yml")!!
        val defaultConfig = Scanner(resource).useDelimiter("\\A").next().trim()

        configFile.writeText(defaultConfig)
    }

    val yaml: Map<String, Any> = Yaml().load(configFile.readText())

    @Suppress("UNCHECKED_CAST")
    config = Config(
        yaml["proxy-port"] as Int,
        yaml["max-players"] as Int,
        yaml["log-packets"] as Boolean,
        (yaml["ignored-packets"] as List<String>)
            .map { BedrockPacketType.valueOf(it) }
    )
}
