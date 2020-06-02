package wtf.lpc.bedrokt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.nukkitx.protocol.bedrock.BedrockPacketCodec
import com.nukkitx.protocol.bedrock.v390.Bedrock_v390
import java.util.*
import kotlin.concurrent.schedule

val packetCodec: BedrockPacketCodec = Bedrock_v390.V390_CODEC
val jsonMapper: ObjectMapper = ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

fun main() {
    reloadConfig()

    startServer()
    Timer().schedule(0, 1) {}
}
