package app.atomofiron.estimoji.injactable.channel

import app.atomofiron.common.util.KObservable
import app.atomofiron.estimoji.model.ConnectionState
import app.atomofiron.estimoji.model.User

object PublicChannel {
    val users = KObservable<List<User>>()
    /** value is true if connected to the server */
    val connectionStatus = KObservable<ConnectionState>(single = true)
    val chose = KObservable<String>(single = true)
    val ipJoin = KObservable<String>()

    val appPaused = KObservable(false, single = true)
    val ipResultScanned = KObservable<String>(single = true)
}