package app.atomofiron.estimoji.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import app.atomofiron.estimoji.log

class BlockFrameLayout : FrameLayout {
    companion object {
        const val DELAY = 300L
    }
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    var blocked: Boolean = false
    private var debounce = false
    var atTime = 0L
    var action = 0

    fun block() {
        log("block()")
        blocked = true
        debounce = false
    }

    fun unblock() {
        log("unblock()")
        debounce = true
        atTime = System.currentTimeMillis()
    }

    /*override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        log("action: ${MotionEvent.actionToString(ev.action)}")

        when {
            blocked && !debounce -> {
                log("---block...")
                return true
            }
            !blocked -> {
                log("---miss!")
                return super.onInterceptTouchEvent(ev)
            }
        }
        val now = System.currentTimeMillis()
        val dif = now - atTime
        if (dif < DELAY) {
            log("---debounce...")
            atTime = now
        } else {
            log("---unblock!")
            blocked = false
            debounce = false
        }
        return blocked
    }*/

    /*override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev?.action ?: 0
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            this.action = action
            val now = System.currentTimeMillis()
            if (action == MotionEvent.ACTION_DOWN) {
                val dif = now - atTime
                blocked = dif < DELAY
            }
            atTime = now
        }
        if (blocked) {
            log("onInterceptTouchEvent blocking... ${ev?.action} ${ev?.pointerCount}")
        } else {
            log("onInterceptTouchEvent ${ev?.action} ${ev?.pointerCount}")
        }
        return blocked || super.onInterceptTouchEvent(ev)
    }*/
}
/*

onInterceptTouchEvent pinterCount: 1 action: ACTION_DOWN
onInterceptTouchEvent pinterCount: 2 action: ACTION_POINTER_DOWN + ACTION_POINTER_2_DOWN
onInterceptTouchEvent pinterCount: 2 action: ACTION_MOVE
onInterceptTouchEvent pinterCount: 2 action: ACTION_MOVE
onInterceptTouchEvent pinterCount: 2 action: ACTION_POINTER_UP
onInterceptTouchEvent pinterCount: 1 action: ACTION_UP

 */