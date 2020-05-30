package io.atomofiron.estimoji.screen.auth

import io.atomofiron.estimoji.screen.base.BaseRouter
import io.atomofiron.estimoji.screen.cards.CardsFragment
import io.atomofiron.estimoji.screen.poker.PokerFragment
import io.atomofiron.estimoji.screen.scan.ScanFragment

class AuthRouter : BaseRouter() {

    fun startPokerScreen(nickname: String, connection: String = "") {
        val pokerFragment = PokerFragment.create(nickname, connection)
        startScreen(CardsFragment(), pokerFragment) {
            switchScreen(addToBackStack = false) { it is PokerFragment }
        }
    }

    fun startScanScreen() = startScreen(ScanFragment())

    fun startCardsScreen() = startScreen(CardsFragment())
}