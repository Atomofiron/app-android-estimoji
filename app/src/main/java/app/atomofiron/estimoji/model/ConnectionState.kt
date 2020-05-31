package app.atomofiron.estimoji.model

sealed class ConnectionState {
    object Authorized : ConnectionState()
    object Forbidden : ConnectionState()
}