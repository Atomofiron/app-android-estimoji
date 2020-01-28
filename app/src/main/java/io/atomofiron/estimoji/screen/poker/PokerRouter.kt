package io.atomofiron.estimoji.screen.poker

import io.atomofiron.estimoji.screen.base.BaseRouter
import io.atomofiron.estimoji.screen.cards.CardsFragment

class PokerRouter : BaseRouter() {
    fun startCardsScreen() {
        switchScreen { it is CardsFragment }
    }
}