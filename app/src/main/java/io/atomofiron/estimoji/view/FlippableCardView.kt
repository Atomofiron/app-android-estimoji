package io.atomofiron.estimoji.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.atomofiron.estimoji.view.FlippableCardView.Direction.VERTICAL
import io.atomofiron.estimoji.view.FlippableCardView.Direction.HORIZONTAL
import kotlin.math.*

class FlippableCardView : FrameLayout {
    companion object {
        private const val MAX_DURATION = 10000L
        private const val ANIM_START = 0f
        private const val ANIM_END = 1f
        private const val DIF_TO_SCALE_RATIO = 0.01f
        private const val ZERO = 0f
        private const val DEFAULT_SCALE = 1f
        private const val MIN_SCALE = -1f
        private const val MAX_SCALE = 3f
        private const val UP = -1f
        private const val DOWN = 1f

        fun crossingZero(first: Float, second: Float): Boolean = sign(first) != sign(second)
        fun crossingTwo(first: Float, second: Float): Boolean = sign(first - 2) != sign(second - 2)
    }
    private enum class Direction {
        VERTICAL, HORIZONTAL
    }
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    private val cardView: View get() = getChildAt(0)
    private val valueView: View get() = (cardView as ViewGroup).getChildAt(0)

    // return true, если событие "поглощено"
    var onOpenedCardClickListener: (() -> Boolean)? = null

    private var motionAnimator: ValueAnimator = ValueAnimator.ofFloat()
    private var lastX = -1f
    private var lastY = -1f
    private val targetAbsDif = 30f
    private var difForAnimation = targetAbsDif // != 0f
    private var orientation = VERTICAL
    private var direction = 0f // -1f, 0f, 1f
    private var moveDifX = ZERO
    private var moveDifY = ZERO
    private var eventTap = true
    private val moveDif: Float get() = when (orientation) {
        VERTICAL -> moveDifY
        else -> moveDifX
    }

    /* по завершении переворачивания карты cardScale сбрасывается к 1f */
    private var cardScale: Float
        get() = when (orientation) {
            VERTICAL -> cardScaleY
            else -> cardScaleX
        }
        set(value) = when (orientation) {
            VERTICAL -> cardScaleY = value
            else -> cardScaleX = value
        }
    /* необходима возможность переворачивать карту в любую сторону.
    * обычное состояние 1f, перевернутое вперёд -1f, назад 3f.
    * для применения сскейла к вьюхе конвертирование к -1f..1f.
    */
    private var cardScaleX = DEFAULT_SCALE
        set(value) {
            if (value != DEFAULT_SCALE && (crossingZero(field, value) || crossingTwo(field, value))) {
                toggleValueVisibility()
            }
            field = when {
                value < -1f -> -1f
                value > 3f -> 3f
                else -> value
            }
            val scale = when {
                value < 1f -> value
                else -> -value + 2
            }
            cardView.scaleX = abs(scale)
        }
    private var cardScaleY = DEFAULT_SCALE
        set(value) {
            if (value != DEFAULT_SCALE && (crossingZero(field, value) || crossingTwo(field, value))) {
                toggleValueVisibility()
            }
            field = when {
                value < -1f -> -1f
                value > 3f -> 3f
                else -> value
            }
            val scale = when {
                value < 1f -> value
                else -> -value + 2
            }
            cardView.scaleY = abs(scale)
            //cardView.rotationX = (scale - 1f) * 90
        }

    // todo consider display metrics

    fun closeCard() {
        if (valueView.visibility == View.VISIBLE) {
            toggleValueVisibility()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        cardView.pivotX = cardView.width.toFloat() / 2
        cardView.pivotY = cardView.height.toFloat() / 2
    }

    // todo попробовать заменить onOpenedCardClickListener на performClick()
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val difX = x - lastX
        val difY = y - lastY
        lastX = x
        lastY = y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown()
            MotionEvent.ACTION_MOVE -> onActionMove(difX, difY)
            MotionEvent.ACTION_UP -> onActionUp()
        }

        return super.onTouchEvent(event)
    }

    private fun onActionDown() {
        if (difForAnimation == 0f) {
            difForAnimation = targetAbsDif
        }
        moveDifX = ZERO
        moveDifY = ZERO
        eventTap = true
        motionAnimator.cancel()
        // при повторном тапе имитируется отмена действия
        difForAnimation = targetAbsDif * sign(-difForAnimation)
    }

    private fun onActionMove(difX: Float, difY: Float) {
        moveDifX = difX
        moveDifY = difY
        eventTap = eventTap && moveDif == 0f

        orientation = when {
            direction != 0f -> orientation
            difX == 0f && difY == 0f -> orientation
            abs(difX) > abs(difY) -> HORIZONTAL
            else -> VERTICAL
        }
        if (direction == 0f) {
            direction = sign(moveDif)
        }
        val scale = getScaleByDif(cardScale, moveDif)
        val downToUp = direction > 0f && scale < DEFAULT_SCALE
        val upToDown = direction < 0f && scale > DEFAULT_SCALE
        val crossingStartState = downToUp || upToDown
        // после начала жеста блокируем пересечение начального положения
        cardScale = if (crossingStartState) DEFAULT_SCALE else scale

        if (moveDif != 0f) {
            difForAnimation = moveDif
        }
    }

    private fun onActionUp() {
        val cardScale = cardScale
        val isEnd = (cardScale == 3f || cardScale == 1f || cardScale == -1f)
        val topOnOpened = eventTap && isEnd && valueView.visibility == View.VISIBLE
        if (!eventTap && moveDif == 0f) {
            /* при замирании жеста доворачиваем карту до ближайшего состояния,
            * где -1f_(-)_0f_(+)_1f_(-)_2f_(+)_3f */
            val cardDirection = if (cardScale > 0f && cardScale < 1f || cardScale > 2f) DOWN else UP
            difForAnimation = targetAbsDif * cardDirection
        }
        when {
            !eventTap && (cardScale == 3f || cardScale == 1f || cardScale == -1f) -> {
                // карта перевёрнута до конца вручную
                resetCard()
            }
            // при клике ничего не делаем, если событие "поглощено"
            topOnOpened && onOpenedCardClickListener?.invoke() == true -> Unit
            else -> {
                val sign = sign(difForAnimation)
                val scaleTo = when {                   // -1___0___1___2___3
                    cardScale >= 1f && sign > 0f -> 3f //          -->
                    cardScale > 1f -> 1f               //             <--
                    sign > 0f -> 1f                    //     -->
                    else -> -1f                        //    <--
                }
                startMotionAnim(difForAnimation, scaleTo)
                // исключает кейс поперечного слайда сразу после клика, когда direction == 0f
                direction = sign(difForAnimation)
            }
        }
    }

    private fun startMotionAnim(dif: Float, scaleTo: Float) {
        motionAnimator.cancel()
        /* animateValue(ValueAnimator.java:1547)
        * mUpdateListeners.get(i) -> IndexOutOfBoundsException: Index: 1, Size: 1 */
        motionAnimator = ValueAnimator.ofFloat(ANIM_START, ANIM_END)
        motionAnimator.addUpdateListener(ValueAnimatorListener(dif, scaleTo))
        motionAnimator.duration = MAX_DURATION
        motionAnimator.start()
    }

    private fun toggleValueVisibility() {
        valueView.visibility = when (valueView.visibility) {
            View.VISIBLE -> View.INVISIBLE
            else -> View.VISIBLE
        }
        when (orientation) {
            // отражение значения при переворотах как на настоящей карте
            VERTICAL -> valueView.scaleY = -valueView.scaleY
            else -> valueView.scaleX = -valueView.scaleX
        }
    }

    private fun resetCard() {
        cardScale = DEFAULT_SCALE
        direction = ZERO
    }

    private fun getScaleByDif(scale: Float, dif: Float): Float {
        var resultSale = scale
        resultSale += getScaleDifByDif(scale, dif)
        return resultSale
    }

    private fun getScaleDifByDif(scale: Float, dif: Float): Float {
        var scaleDif = dif * DIF_TO_SCALE_RATIO
        scaleDif *= abs(cos(scale * Math.PI / 2)).toFloat()
        scaleDif += sign(dif) * DIF_TO_SCALE_RATIO
        return scaleDif
    }

    private inner class ValueAnimatorListener(
        private var dif: Float,
        private val scaleTo: Float
    ) : ValueAnimator.AnimatorUpdateListener {
        private val fcv: FlippableCardView get() = this@FlippableCardView

        override fun onAnimationUpdate(animator: ValueAnimator) {
            val currentScale = fcv.cardScale
            if (currentScale == scaleTo) {
                animator.cancel()
                resetCard()
            } else {
                if (abs(dif) < targetAbsDif) {
                    // увеличиваем скорость до минимальной
                    dif += sign(dif)
                }
                val scale = when (animator.animatedValue as Float) {
                    ANIM_END -> scaleTo
                    else -> getScaleByDif(currentScale, dif)
                }
                /* если движение по направлению к исходному состоянию,
                * нужно определить его пересечение и остановить движение
                * current -> scaleTo -> scale или scale -> scaleTo -> current
                */
                val crossingScaleTo = sign(scaleTo - currentScale) != sign(scaleTo - scale)
                fcv.cardScale = when (crossingScaleTo) {
                    true -> scaleTo
                    else -> scale
                }
            }
        }
    }
}
