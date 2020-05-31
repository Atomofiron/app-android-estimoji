package app.atomofiron.estimoji.screen.base.util

import androidx.fragment.app.FragmentManager
import app.atomofiron.estimoji.log

class OneTimeBackStackListener(
    private val manager: FragmentManager,
    private var callback: (() -> Unit)?
) : FragmentManager.OnBackStackChangedListener {
    init {
        manager.addOnBackStackChangedListener(this)
    }
    override fun onBackStackChanged() {
        log("onBackStackChanged")
        callback?.invoke()
        callback = null
        manager.removeOnBackStackChangedListener(this)
    }
}