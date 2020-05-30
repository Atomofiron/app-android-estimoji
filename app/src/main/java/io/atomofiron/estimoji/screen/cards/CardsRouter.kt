package io.atomofiron.estimoji.screen.cards

import io.atomofiron.estimoji.screen.base.BaseRouter
import io.atomofiron.estimoji.screen.card.CardFragment

class CardsRouter : BaseRouter() {
    fun startCardScreen(value: String) = startScreen(CardFragment.new(value))
}