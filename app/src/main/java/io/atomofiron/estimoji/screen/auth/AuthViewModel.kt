package io.atomofiron.estimoji.screen.auth

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.base.BaseViewModel
import io.atomofiron.estimoji.util.Util

class AuthViewModel(app: Application) : BaseViewModel<AuthRouter>(app) {
    override val router = AuthRouter()

    val nickname = MutableLiveData<String>("")
    val ipJoin = MutableLiveData<String>("")

    fun onCreateClick() = router.startPokerScreen()

    fun onNicknameInput(nickname: String) {
        this.nickname.value = nickname
    }

    fun onIpInput(ip: String?) {
        // todo
        log("onIpInput ip: $ip")
        ipJoin.value = ip
    }

    fun onScanClick() = router.startScanScreen()

    fun onJoinClick() = router.startPokerScreen()

    fun onCardsClick() = router.startCardsScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.recreateActivity()
    }
}