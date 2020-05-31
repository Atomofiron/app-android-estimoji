package app.atomofiron.estimoji.screen.cards.recycler

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.estimoji.R

class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val tvCounter = itemView.findViewById<TextView>(R.id.counter)
    private val cvCard = itemView.findViewById<CardView>(R.id.item_cv_card)

    private lateinit var item: String
    private var onCardClickListener: OnCardClickListener<String>? = null

    override fun onClick(v: View?) {
        onCardClickListener?.onCardClick(item)
    }

    fun onBind(item: String) {
        this.item = item
        cvCard.setOnClickListener(this)
        tvCounter.text = item
    }

    fun setOnCardClickListener(listener: OnCardClickListener<String>?) {
        onCardClickListener = listener
    }
}