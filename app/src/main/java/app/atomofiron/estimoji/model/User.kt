package app.atomofiron.estimoji.model

import app.atomofiron.estimoji.util.Const

data class User(
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

    val isSlipping: Boolean get() = status == STATUS_SLIPPING
    val isWaking: Boolean get() = status == STATUS_WAKING
    val isOffline: Boolean get() = status == STATUS_OFFLINE

    override fun equals(other: Any?): Boolean = when (other) {
        null -> false
        !is User -> false
        else -> other.nickname == nickname
    }

    override fun hashCode(): Int = nickname.hashCode()

    fun areContentsTheSame(other: User) = when {
        other.nickname != nickname -> false
        other.status != status -> false
        other.chose != chose -> false
        else -> true
    }

    fun getChosePlaceHolder(): String = if (chose.isEmpty()) "" else Const.CARD_PLACEHOLDER
}