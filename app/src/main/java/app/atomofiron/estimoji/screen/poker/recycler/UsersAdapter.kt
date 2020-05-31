package app.atomofiron.estimoji.screen.poker.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.model.User

class UsersAdapter : GeneralAdapter<UserHolder, User>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        inflater: LayoutInflater
    ): UserHolder {
        val itemView = inflater.inflate(R.layout.item_user, parent, false)
        return UserHolder(itemView)
    }

    override fun getDiffUtilCallback(old: List<User>, new: List<User>): DiffUtil.Callback? {
        return DiffUtilCallback(old, new)
    }

    private class DiffUtilCallback(val old: List<User>, val new: List<User>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = old[oldItemPosition]
            val new = new[newItemPosition]
            return old.nickname == new.nickname
        }

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = old[oldItemPosition]
            val new = new[newItemPosition]
            return new.areContentsTheSame(old)
        }
    }
}