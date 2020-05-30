package io.atomofiron.estimoji.screen.auth

import android.app.Application
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.util.LateinitLiveData
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.base.BaseViewModel
import io.atomofiron.estimoji.util.Util

class AuthViewModel(app: Application) : BaseViewModel<AuthRouter>(app) {
    override val router = AuthRouter()

    val nickname = LateinitLiveData("")
    val ipJoin = LateinitLiveData("")

    fun onCreateClick() = router.startPokerScreen(nickname.value)

    fun onNicknameInput(nickname: String) {
        this.nickname.value = nickname
    }

    fun onIpInput(ip: String) {
        // todo
        log("onIpInput ip: $ip")
        ipJoin.value = ip
    }

    fun onScanClick() = router.startScanScreen()

    fun onJoinClick() = router.startPokerScreen(nickname.value, ipJoin.value)

    fun onCardsClick() = router.startCardsScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.reattachFragments()
    }
}