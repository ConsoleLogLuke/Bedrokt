package io.lavamc.bedrokt

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nukkitx.network.util.Preconditions
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket
import com.nukkitx.protocol.bedrock.packet.LoginPacket
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import io.netty.util.AsciiString
import net.minidev.json.JSONObject
import io.lavamc.bedrokt.api.EventType
import io.lavamc.bedrokt.api.Player
import io.lavamc.bedrokt.api.PluginManager
import java.io.IOException
import java.net.URI
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.InvalidKeySpecException
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit

data class LoginStuff(
    val skinData: JSONObject,
    val extraData: JSONObject,
    val chainData: ArrayNode,
    val keyPair: KeyPair = EncryptionUtils.createKeyPair()
)

private fun forgeAuthData(pair: KeyPair, extraData: JSONObject?): SignedJWT? {
    val publicKeyBase64: String = Base64.getEncoder().encodeToString(pair.public.encoded)
    val x5u: URI = URI.create(publicKeyBase64)
    val header = JWSHeader.Builder(JWSAlgorithm.ES384).x509CertURL(x5u).build()
    val timestamp = System.currentTimeMillis()
    val nbf = Date(timestamp - TimeUnit.SECONDS.toMillis(1))
    val exp = Date(timestamp + TimeUnit.DAYS.toMillis(1))

    val claimsSet = JWTClaimsSet.Builder()
        .notBeforeTime(nbf)
        .expirationTime(exp)
        .issueTime(exp)
        .issuer("self")
        .claim("certificateAuthority", true)
        .claim("extraData", extraData)
        .claim("identityPublicKey", publicKeyBase64)
        .build()

    val jwt = SignedJWT(header, claimsSet)

    try {
        EncryptionUtils.signJwt(jwt, pair.private as ECPrivateKey)
    } catch (e: JOSEException) {
        throw RuntimeException(e)
    }

    return jwt
}

private fun forgeSkinData(pair: KeyPair, skinData: JSONObject?): JWSObject? {
    val x5u: URI = URI.create(Base64.getEncoder().encodeToString(pair.public.encoded))
    val header = JWSHeader.Builder(JWSAlgorithm.ES384).x509CertURL(x5u).build()
    val jws = JWSObject(header, Payload(skinData))

    try {
        EncryptionUtils.signJwt(jws, pair.private as ECPrivateKey)
    } catch (e: JOSEException) {
        throw RuntimeException(e)
    }

    return jws
}

private fun validateChainData(data: JsonNode): Boolean {
    var lastKey: ECPublicKey? = null
    var validChain = false

    for (node in data) {
        val jwt = JWSObject.parse(node.asText())
        if (!validChain) validChain =
            verifyJwt(jwt, EncryptionUtils.getMojangPublicKey())

        lastKey?.let { verifyJwt(jwt, it) }

        val payloadNode = jsonMapper.readTree(jwt.payload.toString())
        val ipkNode = payloadNode["identityPublicKey"]

        Preconditions.checkState(
            ipkNode != null && ipkNode.nodeType == JsonNodeType.STRING,
            "identityPublicKey node is missing in chain"
        )

        lastKey = EncryptionUtils.generateKey(ipkNode!!.asText())
    }

    return validChain
}

private fun verifyJwt(jwt: JWSObject, key: ECPublicKey): Boolean {
    return jwt.verify(DefaultJWSVerifierFactory().createJWSVerifier(jwt.header, key))
}

fun Player.login(packet: LoginPacket) {
    val protocolVersion = packet.protocolVersion
    if (protocolVersion != packetCodec.protocolVersion) {
        val status = PlayStatusPacket()

        status.status = if (protocolVersion > packetCodec.protocolVersion) PlayStatusPacket.Status.FAILED_SERVER
        else PlayStatusPacket.Status.FAILED_CLIENT
    }

    val certData = try {
        jsonMapper.readTree(packet.chainData.toByteArray())
    } catch (e: IOException) {
        throw RuntimeException("Certificate JSON can not be read.")
    }

    val certChainData = certData["chain"]
    val chainData = certChainData as ArrayNode

    if (certChainData.nodeType != JsonNodeType.ARRAY) throw RuntimeException("Certificate data is not valid")

    try {
        validateChainData(certChainData)

        val jwt = JWSObject.parse(certChainData[certChainData.size() - 1].asText())
        val payload: JsonNode = jsonMapper.readTree(jwt.payload.toBytes())

        if (payload["extraData"].nodeType != JsonNodeType.OBJECT) throw RuntimeException("AuthData was not found!")

        val extraData = jwt.payload.toJSONObject()["extraData"] as JSONObject

        gamertag = extraData.getAsString("displayName")
        uniqueId = UUID.fromString(extraData.getAsString("identity"))
        xboxId = extraData.getAsString("XUID")

        if (payload["identityPublicKey"].nodeType != JsonNodeType.STRING) {
            throw RuntimeException("Identity Public Key was not found!")
        }

        val identityPublicKey = EncryptionUtils.generateKey(payload["identityPublicKey"].textValue())
        val clientJwt = JWSObject.parse(packet.skinData.toString())

        verifyJwt(clientJwt, identityPublicKey)
        val skinData = clientJwt.payload.toJSONObject()

        internal.loginStuff = LoginStuff(skinData, extraData, chainData)
        proxyLogger.info("Player $gamertag logged in!")

        PluginManager.callEvent(EventType.PLAYER_PROXY_JOIN) { it.onPlayerProxyJoin(this) }
    } catch (e: Exception) {
        internal.realSession.disconnect("disconnectionScreen.internalError.cantConnect")
        throw RuntimeException("Unable to complete login", e)
    }
}

fun Player.finishLogin() {
    val authData =
        forgeAuthData(internal.loginStuff.keyPair, internal.loginStuff.extraData)
    val skinData =
        forgeSkinData(internal.loginStuff.keyPair, internal.loginStuff.skinData)

    internal.loginStuff.chainData.remove(internal.loginStuff.chainData.size() - 1)
    internal.loginStuff.chainData.add(authData?.serialize())

    val json: JsonNode = jsonMapper.createObjectNode().set("chain", internal.loginStuff.chainData)
    val chainData: AsciiString

    try {
        chainData = AsciiString(jsonMapper.writeValueAsBytes(json))
    } catch (e: JsonProcessingException) {
        throw RuntimeException(e)
    }

    val login = LoginPacket()

    login.chainData = chainData
    login.skinData = AsciiString.of(skinData?.serialize())

    login.protocolVersion = packetCodec.protocolVersion

    session.sendPacketImmediately(login)
}

fun Player.returnHandshake(packet: ServerToClientHandshakePacket) {
    try {
        val saltJwt = SignedJWT.parse(packet.jwt)
        val x5u: URI = saltJwt.header.x509CertURL
        val serverKey = EncryptionUtils.generateKey(x5u.toASCIIString())

        val key = EncryptionUtils.getSecretKey(
            internal.loginStuff.keyPair.private,
            serverKey,
            Base64.getDecoder().decode(saltJwt.jwtClaimsSet.getStringClaim("salt"))
        )

        session.enableEncryption(key)
    } catch (e: ParseException) {
        throw RuntimeException(e)
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException(e)
    } catch (e: InvalidKeySpecException) {
        throw RuntimeException(e)
    } catch (e: InvalidKeyException) {
        throw RuntimeException(e)
    }

    val clientToServerHandshake = ClientToServerHandshakePacket()
    session.sendPacketImmediately(clientToServerHandshake)
}
