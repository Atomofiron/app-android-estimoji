package app.atomofiron.estimoji.screen.auth

import app.atomofiron.estimoji.screen.base.BaseRouter
import app.atomofiron.estimoji.screen.cards.CardsFragment
import app.atomofiron.estimoji.screen.poker.PokerFragment
import app.atomofiron.estimoji.screen.scan.ScanFragment

class AuthRouter : BaseRouter() {

    fun startPokerScreen(nickname: String) {
        val pokerFragment = PokerFragment.create(nickname)
        startScreen(CardsFragment(), pokerFragment) {
            switchScreen(addToBackStack = false) { it is PokerFragment }
        }
    }

    fun startScanScreen() = startScreen(ScanFragment())

    fun startCardsScreen() = startScreen(CardsFragment.create(allowOpenCard = true))
}