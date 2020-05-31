package app.atomofiron.estimoji.screen.cards.recycler

interface OnCardClickListener<T> {
    fun onCardClick(item: T)
    fun onAddCardConfirm(item: T)
}