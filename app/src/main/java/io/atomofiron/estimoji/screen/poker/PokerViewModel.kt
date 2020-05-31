package io.atomofiron.estimoji.screen.poker

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.atomofiron.estimoji.App
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.injactable.interactor.AuthInteractor
import io.atomofiron.estimoji.logD
import io.atomofiron.estimoji.util.Util
import io.atomofiron.estimoji.screen.base.BaseViewModel
import io.atomofiron.estimoji.work.WebClientWorker
import io.atomofiron.estimoji.work.KtorServerWorker
import java.util.concurrent.TimeUnit

class PokerViewModel(app: Application) : BaseViewModel<PokerRouter>(app) {
    override val router = PokerRouter()

    val nickname = MutableLiveData("Ярослав")
    val shareAddress = MutableLiveData("http://192.168.0.0:777/estimoji.io")
    val shareBitmap = MutableLiveData<Bitmap>()

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        val size = context.resources.getDimensionPixelSize(R.dimen.share_view)
        shareBitmap.value = Util.encodeAsBitmap(size, size, shareAddress.value!!)

        nickname.value = intent.getStringExtra(PokerFragment.KEY_NICKNAME)
    }

    override fun onViewDestroy() {
        super.onViewDestroy()

        shareBitmap.value?.recycle()
    }

    fun onShareClick() {
    }

    fun onCardsClick() = router.startCardsScreen()

    fun onExitClick() = router.popScreen()

    fun onSettingsClick() {
        Util.isDarkTheme = !Util.isDarkTheme
        router.reattachFragments()
    }
}