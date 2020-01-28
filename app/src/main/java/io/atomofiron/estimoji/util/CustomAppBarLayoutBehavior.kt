package io.atomofiron.estimoji.util

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.min

class CustomAppBarLayoutBehavior(private val appBar: AppBarLayout) : AppBarLayout.Behavior() {
    private var lastHeight = 0
    private var correction = 0

    /* итерации
    * offset: -1088, lastHeight: 1260, height: 1260 -> offset: -1088
    * offset: -147, lastHeight: 1260, height: 147 -> offset: 0
    * offset: -1148, lastHeight: 147, height: 147 -> offset: -35
    * offset: -91, lastHeight: 147, height: 147 -> offset: -91
    *         ^fixed?
    * offset: -1254, lastHeight: 147, height: 147 -> offset: -141
    *         ^UNEXPECTED!!!
    * offset: -181, lastHeight: 147, height: 147 -> offset: -181
    */

    override fun setTopAndBottomOffset(offset: Int): Boolean {
        val height = appBar.height
        var newOffset = offset

        if (lastHeight != height) {
            correction = lastHeight - height
            lastHeight = height

            if (offset == -height) {
                newOffset = 0
            }
        }

        if (offset < -height) {
            newOffset += correction
        }
        return super.setTopAndBottomOffset(min(0, newOffset))
    }
}
