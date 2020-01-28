package io.atomofiron.estimoji.screen.card

import android.app.Application
import io.atomofiron.estimoji.screen.base.BaseViewModel

class CardViewModel(app: Application) : BaseViewModel<CardRouter>(app) {
    override val router = CardRouter()

    fun onOpenedCardClick(): Boolean {
        router.closeScreen()
        return true // todo preferences
    }
}