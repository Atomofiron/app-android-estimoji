package app.atomofiron.estimoji.injactable.channel

import app.atomofiron.common.util.KObservable
import app.atomofiron.estimoji.model.User

object PublicChannel {
    val users = KObservable<List<User>>()
    /** value is true if connected to the server */
    val connectionStatus = KObservable<Boolean>()
    val ipJoin = KObservable<String>()
}