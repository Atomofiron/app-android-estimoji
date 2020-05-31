package app.atomofiron.estimoji.screen.poker.recycler

import android.view.View
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.model.User

class UserHolder(itemView: View) : GeneralHolder<User>(itemView) {
    private val ivAvatar = itemView.findViewById<TextView>(R.id.item_user_iv_avatar)
    private val tvNickname = itemView.findViewById<TextView>(R.id.item_user_tv_nickname)
    private val tvChose = itemView.findViewById<TextView>(R.id.item_user_tv_chose)

    override fun onBind(item: User, position: Int) {
        ivAvatar.text = when {
            item.isAdmin -> "\uD83D\uDC51"
            item.isSlipping -> "\uD83D\uDE34"
            item.isWaking -> "\uD83D\uDE42"
            item.isOffline -> "\uD83D\uDE35"
            else -> "\uD83D\uDC7D"
        }
        tvNickname.text = item.nickname
        tvChose.text = item.chose
    }
}