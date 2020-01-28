package io.atomofiron.estimoji.util

import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout

class OnToolbarCollapsedListener(
    private val toolbar: Toolbar,
    private val onCollapsed: (() -> Unit)? = null,
    private val onExpanded: (() -> Unit)? = null
) : AppBarLayout.OnOffsetChangedListener {
    private var called = false
    private var lastOffset = 0

    override fun onOffsetChanged(appBar: AppBarLayout, offset: Int) {
        //log("offset $offset")
        val nextOffset = offset + offset - lastOffset
        val crossing = appBar.height <= -offset + toolbar.height
        val nextCrossing = appBar.height <= -nextOffset + toolbar.height
        lastOffset = offset
        when {
            offset == 0 -> {
                if (called) return
                called = true
                onExpanded?.invoke()
            }
            crossing || nextCrossing -> {
                if (called) return
                called = true
                onCollapsed?.invoke()
            }
            else -> called = false
        }
    }
}