package io.atomofiron.estimoji.screen.cards

import io.atomofiron.estimoji.screen.base.BaseRouter
import io.atomofiron.estimoji.screen.card.CardFragment

class CardsRouter : BaseRouter() {
    override val indexFromEnd: Int = 0 // todo fix

    fun startCardScreen(value: String) = startScreen(CardFragment.new(value))
}