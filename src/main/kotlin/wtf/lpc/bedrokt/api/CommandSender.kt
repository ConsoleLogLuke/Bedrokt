package wtf.lpc.bedrokt.api

interface CommandSender {
    class ConsoleSender : CommandSender

    companion object {
        val consoleSender = ConsoleSender()
    }
}
