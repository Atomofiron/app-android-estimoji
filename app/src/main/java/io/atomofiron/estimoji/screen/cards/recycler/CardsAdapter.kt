package io.atomofiron.estimoji.screen.cards.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.atomofiron.estimoji.R

class CardsAdapter : RecyclerView.Adapter<CardHolder>() {
    companion object {
        private const val VIEW_TYPE_DEFAULT = 0
        private const val VIEW_TYPE_ADD = 1
    }
    private val data = ArrayList<String>()
    private var onCardClickListener: OnCardClickListener<String>? = null
    var editable = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setData(data: List<String>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnCardClickListener(listener: OnCardClickListener<String>) {
        onCardClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_ADD -> inflater.inflate(R.layout.item_card_add, parent, false)
            else -> inflater.inflate(R.layout.item_card, parent, false)
        }
        return CardHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (editable && position == data.size) VIEW_TYPE_ADD else VIEW_TYPE_DEFAULT
    }

    override fun getItemCount(): Int = if (editable) data.size + 1 else data.size

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_ADD -> Unit
            VIEW_TYPE_DEFAULT -> holder.onBind(data[position])
        }
        holder.setOnCardClickListener(if (editable) null else onCardClickListener)
    }
}