package app.atomofiron.common.util

import androidx.fragment.app.FragmentManager

class OneTimeBackStackListener(
    private val manager: FragmentManager,
    private var callback: (() -> Unit)?
) : FragmentManager.OnBackStackChangedListener {
    init {
        manager.addOnBackStackChangedListener(this)
    }
    override fun onBackStackChanged() {
        callback?.invoke()
        callback = null
        manager.removeOnBackStackChangedListener(this)
    }
}

fun FragmentManager.setOneTimeBackStackListener(callback: (() -> Unit)?) {
    OneTimeBackStackListener(this, callback)
}