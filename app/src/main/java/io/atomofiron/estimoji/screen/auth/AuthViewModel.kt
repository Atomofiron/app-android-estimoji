package io.atomofiron.estimoji.screen.auth

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.Operation
import androidx.work.WorkManager
import app.atomofiron.common.util.LateinitLiveData
import io.atomofiron.estimoji.App
import io.atomofiron.estimoji.injactable.interactor.AuthInteractor
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
    val buttonsEnabled = LateinitLiveData(true)

    private val workManager = WorkManager.getInstance(App.appContext)

    private fun onJoinOperationStateChange(state: Operation.State) {
        when (state) {
            is Operation.State.FAILURE -> {
                buttonsEnabled.value = true
            }
            is Operation.State.SUCCESS -> {
                logD("onJoinOperationStateChange SUCCESS")
                router.startPokerScreen(nickname.value)
                buttonsEnabled.value = true
            }
        }
    }

    fun onCreateClick() {
        buttonsEnabled.value = false
        // todo router.startPokerScreen(nickname.value, password.value)
        interactor.create(nickname.value, password.value)
        interactor.join(nickname.value, password.value, ipJoin.value, ::onJoinOperationStateChange)
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
        buttonsEnabled.value = false
        // todo router.startPokerScreen(nickname.value, password.value, ipJoin.value)
        interactor.join(nickname.value, password.value, ipJoin.value, ::onJoinOperationStateChange)
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
        workManager.cancelUniqueWork(WebClientWorker.NAME)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)
    }
}