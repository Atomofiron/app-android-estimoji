package app.atomofiron.estimoji.screen.cards

import app.atomofiron.estimoji.screen.base.BaseRouter
import app.atomofiron.estimoji.screen.card.CardFragment

class CardsRouter : BaseRouter() {
    fun startCardScreen(value: String) = startScreen(CardFragment.new(value))
}