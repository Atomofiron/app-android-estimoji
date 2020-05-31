package io.atomofiron.estimoji.screen.auth

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import app.atomofiron.common.util.LateinitLiveData
import io.atomofiron.estimoji.App
import io.atomofiron.estimoji.injactable.interactor.AuthInteractor
import io.atomofiron.estimoji.injactable.service.KtorServerService
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.logD
import io.atomofiron.estimoji.screen.base.BaseViewModel
import io.atomofiron.estimoji.util.Util
import io.atomofiron.estimoji.work.KtorServerWorker
import io.atomofiron.estimoji.work.WebClientWorker

class AuthViewModel(app: Application) : BaseViewModel<AuthRouter>(app) {
    override val router = AuthRouter()
    private val interactor = AuthInteractor()

    val nickname = LateinitLiveData("")
    val password = LateinitLiveData("")
    val ipJoin = LateinitLiveData("")

    private lateinit var workManager: WorkManager

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        workManager = WorkManager.getInstance(App.appContext)
    }

    fun onCreateClick() {
        // todo router.startPokerScreen(nickname.value, password.value)
        interactor.create(nickname.value, password.value)
        interactor.join(nickname.value, password.value, ipJoin.value)
    }

    fun onNicknameInput(nickname: String) {
        this.nickname.value = nickname
    }

    fun onPasswordInput(password: String) {
        this.password.value = password
    }

    fun onIpInput(ip: String) {
        // todo what????
        log("onIpInput ip: $ip")
        ipJoin.value = ip
    }

    fun onScanClick() = router.startScanScreen()

    fun onJoinClick() {
        // todo router.startPokerScreen(nickname.value, password.value, ipJoin.value)
        interactor.join(nickname.value, password.value, ipJoin.value)
    }

    fun onCardsClick() = router.startCardsScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.reattachFragments()
    }

    override fun onViewDestroy() {
        logD("onViewDestroy")
        super.onViewDestroy()

        workManager.cancelUniqueWork(WebClientWorker.NAME)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)
    }

    override fun onCleared() {
        super.onCleared()
        logD("onCleared")
        /* todo here
        workManager.cancelUniqueWork(WebClientWorker.NAME)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)*/
    }
}