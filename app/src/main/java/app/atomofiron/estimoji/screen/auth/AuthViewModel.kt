package app.atomofiron.estimoji.screen.auth

import android.app.Application
import androidx.work.Operation
import androidx.work.WorkManager
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.estimoji.App
import app.atomofiron.estimoji.injactable.channel.PublicChannel
import app.atomofiron.estimoji.injactable.interactor.AuthInteractor
import app.atomofiron.estimoji.log
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.model.ConnectionState
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.util.Util
import app.atomofiron.estimoji.work.KtorServerWorker
import app.atomofiron.estimoji.work.WebClientWorker

class AuthViewModel(app: Application) : BaseViewModel<AuthRouter>(app) {
    override val router = AuthRouter()
    private val interactor = AuthInteractor()

    val nickname = LateinitLiveData("")
    val password = LateinitLiveData("")
    val ipJoin = LateinitLiveData("")
    val buttonsEnabled = LateinitLiveData(true)

    private val workManager = WorkManager.getInstance(App.appContext)

    init {
        PublicChannel.ipResultScanned.addObserver(onClearedCallback) {
            ipJoin.postValue(it)

            /*if (nickname.value.isNotEmpty() && password.value.isNotEmpty()) {
                join()
            }*/
        }
        PublicChannel.connectionStatus.addObserver(onClearedCallback) {
            logD("connectionStatus ${it.javaClass.simpleName}")
            buttonsEnabled.postValue(true)
            when (it) {
                is ConnectionState.Authorized -> router.startPokerScreen(nickname.value)
                is ConnectionState.Forbidden -> router.startPokerScreen(nickname.value)
            }
        }
    }

    fun onCreateClick() {
        buttonsEnabled.value = false
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
        buttonsEnabled.value = false
        join()
    }

    private fun join() {
        logD("join...")
        interactor.join(nickname.value, password.value, ipJoin.value)
    }

    fun onCardsClick() = router.startCardsScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.reattachFragments()
    }

    override fun onViewDestroy() {
        super.onViewDestroy()

        workManager.cancelUniqueWork(WebClientWorker.NAME)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)
    }
}