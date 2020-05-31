package app.atomofiron.estimoji.screen.cards

sealed class CState(val cards: List<String>) {
    class Normal(cards: List<String>) : CState(cards)
    class Edit(cards: List<String>) : CState(cards)
}