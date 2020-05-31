package app.atomofiron.estimoji.screen.poker

import app.atomofiron.estimoji.screen.base.BaseRouter
import app.atomofiron.estimoji.screen.cards.CardsFragment

class PokerRouter : BaseRouter() {
    fun startCardsScreen() {
        switchScreen { it is CardsFragment }
    }
}