package app.atomofiron.estimoji.screen.cards

import android.app.Application
import androidx.lifecycle.MutableLiveData
import app.atomofiron.estimoji.screen.base.BaseViewModel

class CardsViewModel(app: Application) : BaseViewModel<CardsRouter>(app) {
    override val router = CardsRouter()

    val cards = MutableLiveData<String>()

    fun onCardClick(value: String) = router.startCardScreen(value)

    fun onEditOptionClick() = Unit

    fun onConfirmOptionClick(setName: String, cards: List<String>) = Unit

    fun onAddOptionClick() = Unit
}