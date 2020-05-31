package io.atomofiron.estimoji.model

class User(
    val nickname: String,
    val isAdmin: Boolean,
    val status: String,
    val chose: String
) {
    companion object {
        const val STATUS_WAKING = "waking"
        const val STATUS_SLIPPING = "slipping"
        const val STATUS_OFFLINE = "offline"
    }
}