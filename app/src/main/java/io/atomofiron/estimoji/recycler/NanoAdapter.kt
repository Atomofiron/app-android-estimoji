package io.atomofiron.estimoji.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class NanoAdapter(private val layoutId: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return NanoHolder(view)
    }

    override fun getItemCount(): Int = 100

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit

    private class NanoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}