package io.lavamc.bedrokt.api

enum class EventType(val methodName: String) {
    PROXY_START("onProxyStart"),
    CONSOLE_MESSAGE("onConsoleMessage"),

    PLAYER_PROXY_JOIN("onPlayerProxyJoin"),
    PLAYER_SERVER_JOIN("onPlayerServerJoin"),

    PLAYER_PROXY_DISCONNECT("onPlayerProxyDisconnect"),
    PLAYER_SERVER_DISCONNECT("onPlayerServerDisconnect"),

    CLIENT_TO_SERVER_PACKET("onClientToServerPacket"),
    SERVER_TO_CLIENT_PACKET("onServerToClientPacket"),

    COMMAND_PREPROCESS("onCommandPreprocess")
}
