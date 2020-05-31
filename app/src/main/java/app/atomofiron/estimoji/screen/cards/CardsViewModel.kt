package app.atomofiron.estimoji.screen.cards

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import app.atomofiron.estimoji.injactable.channel.PublicChannel
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.screen.cards.CardsFragment.Companion.KEY_ALLOW_OPEN_CARD

class CardsViewModel(app: Application) : BaseViewModel<CardsRouter>(app) {
    override val router = CardsRouter()

    private var allowOpenCard = false

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        allowOpenCard = intent.getBooleanExtra(KEY_ALLOW_OPEN_CARD, false)
    }

    val cards = MutableLiveData<String>()

    fun onCardClick(value: String) {
        if (allowOpenCard) {
            router.startCardScreen(value)
        } else {
            logD("onCardClick $value")
            PublicChannel.chose.setAndNotify(value)
            router.popScreen()
        }
    }

    fun onEditOptionClick() = Unit

    fun onConfirmOptionClick(setName: String, cards: List<String>) = Unit

    fun onAddOptionClick() = Unit
}