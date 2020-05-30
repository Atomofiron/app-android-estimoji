package io.atomofiron.estimoji.screen.poker

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.util.Util
import io.atomofiron.estimoji.screen.base.BaseViewModel

class PokerViewModel(app: Application) : BaseViewModel<PokerRouter>(app) {
    override val router = PokerRouter()

    val nickname = MutableLiveData("Ярослав")
    val shareAddress = MutableLiveData("http://192.168.0.0:777/estimoji.io")
    val shareBitmap = MutableLiveData<Bitmap>()

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        nickname.value = intent.getStringExtra(PokerFragment.KEY_NICKNAME)
        val connection = intent.getStringExtra(PokerFragment.KEY_CONNECTION)
        // todo connection

        val size = context.resources.getDimensionPixelSize(R.dimen.share_view)
        shareBitmap.value = Util.encodeAsBitmap(size, size, shareAddress.value!!)
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