package app.atomofiron.estimoji.screen.poker

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.estimoji.App
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.injactable.channel.PublicChannel
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.util.Util
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.work.WebClientWorker
import app.atomofiron.estimoji.work.KtorServerWorker

class PokerViewModel(app: Application) : BaseViewModel<PokerRouter>(app) {
    override val router = PokerRouter()

    val nickname = LateinitLiveData<String>()
    val shareAddress = LateinitLiveData<String>()
    val showExitSnackbar = SingleLiveEvent<Unit>()
    val shareBitmap = MutableLiveData<Bitmap>()

    private val workManager = WorkManager.getInstance(App.appContext)

    init {
        PublicChannel.ipJoin.addObserver(onClearedCallback) {
            shareAddress.postValue(it)
            val size = app.applicationContext.resources.getDimensionPixelSize(R.dimen.share_view)
            shareBitmap.postValue(Util.encodeAsBitmap(size, size, it))
        }
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        nickname.value = intent.getStringExtra(PokerFragment.KEY_NICKNAME)!!
    }

    override fun onViewDestroy() {
        super.onViewDestroy()

        shareBitmap.value?.recycle()
        workManager.cancelUniqueWork(WebClientWorker.NAME)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)
    }

    fun onShareClick() {
    }

    fun onCardsClick() = router.startCardsScreen()

    fun onExitClick() = router.popScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.reattachFragments()
    }

    override fun onBackPressed(): Boolean {
        showExitSnackbar.invoke()
        return true
    }
}