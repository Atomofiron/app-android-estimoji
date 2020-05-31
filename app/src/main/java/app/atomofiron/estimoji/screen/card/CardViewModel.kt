package app.atomofiron.estimoji.screen.card

import android.app.Application
import app.atomofiron.estimoji.screen.base.BaseViewModel

class CardViewModel(app: Application) : BaseViewModel<CardRouter>(app) {
    override val router = CardRouter()

    fun onOpenedCardClick(): Boolean {
        router.popScreen()
        return true // todo preferences
    }
}