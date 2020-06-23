package com.elevenetc.concepts.keyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import java.util.*

class KeyboardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    init {
        setBackgroundColor(Color.RED)
    }

    private val keyHorizontalCount = 10
    private val lettersLine0 = arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val keysLine0 = mutableListOf<Key>()
    private val inputBounds = RectF()

    var inputTapHandler: () -> Unit = {}

    val keyboardHeight = resources.getDimension(R.dimen.keyboard_height)
    val inputViewHeight = resources.getDimension(R.dimen.input_height)
    val keyTextSize = resources.getDimension(R.dimen.key_text_size)
    val keyHeight = resources.getDimension(R.dimen.key_height)
    val maxKeyWidth = resources.getDimension(R.dimen.max_key_width).toInt()
    val keyPadding = resources.getDimension(R.dimen.key_padding)
    var minKeyWidth = 0
    var keyWidth = 0
    var totalWidth = 0

    var touchX = 0f
    var touchY = 0f

    val keyPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    val keySelectedPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    val keyTextPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textSize = keyTextSize
    }

    val pathPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawInput(canvas)
        drawKeyboard(canvas)
        canvas.drawCircle(touchX, touchY, 100f, keyPaint)
        canvas.drawCircle(touchX, touchY, 10f, keyPaint)
        canvas.drawCircle(touchX, touchY, 15f, keyPaint)

        val path = Path()

        var prevTime = 0L
        var diffTime = 0L
        for ((index, it) in events.withIndex()) {
            //TODO: perf: O(x^2) per draw call

            val x = it.x
            val y = it.y
            if (index == 0) {
                path.moveTo(x, y)
                prevTime = it.time
            } else {

                diffTime = it.time - prevTime

                if (diffTime > 20) {
                    canvas.drawCircle(x, y, 10f, keyPaint)
                    canvas.drawCircle(x, y, 20f, keyPaint)
                    canvas.drawCircle(x, y, 30f, keyPaint)
                }

                prevTime = it.time

                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, pathPaint)
    }



    var moving = false
    var lastKey: Key? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {


        val action = event.action
        val x = event.x
        val y = event.y
        val handled: Boolean

        touchX = x
        touchY = y

        invalidate()

        if (action == ACTION_MOVE) {
            moving = true
        } else if (action == ACTION_CANCEL || action == ACTION_UP) {

            if (!moving) {
                if (lastKey != null) {
                    println("print:" + lastKey!!.ch)
                }
            }

            moving = false
        }


        handled = handleTouchEvent(action, x, y, event.eventTime)

        if (inputBounds.contains(x, y)) {
            if (action == ACTION_UP) {
                inputTapHandler()
            }
            return true
        }

        return if (!handled) super.onTouchEvent(event)
        else handled
    }

    data class MovePoint(val x: Float, val y: Float, val time: Long)

    private val events: LinkedList<MovePoint> = LinkedList()

    private fun handleTouchEvent(
        action: Int,
        x: Float,
        y: Float,
        eventTime: Long
    ): Boolean {
        var handled = false
        if (action == ACTION_DOWN || action == ACTION_UP || action == ACTION_CANCEL || action == ACTION_MOVE) {


            keysLine0.forEach {
                val contains = it.bounds.contains(x, y)
                it.selected = contains

                if (contains) {
                    lastKey = it
                    handled = true
                }
            }


            //start tap/motion
            if (action == ACTION_DOWN) {
                lastKey = null

                events.clear()
            }

            //end tap/motion
            if (action == ACTION_UP || action == ACTION_CANCEL) {
                lastKey?.selected = false

                events.clear()
            }


            events.add(MovePoint(x, y, eventTime))

            if (events.size >= 30) {
                //events.removeFirst()
            }
        }

        if (!handled) {
            lastKey = null
        }

        return handled
    }

    class TapKeeper {
        fun onEvent(event: MotionEvent) {
            if (event.action == ACTION_DOWN) {

            } else if (event.action == ACTION_UP) {

            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            totalWidth = right
            minKeyWidth = totalWidth / keyHorizontalCount
            keyWidth = if (minKeyWidth > maxKeyWidth) {
                maxKeyWidth
            } else {
                minKeyWidth
            }

            keysLine0.clear()

            inputBounds.set(0f, 0f, totalWidth.toFloat(), inputViewHeight)

            buildKeys()
        }
    }

    private fun buildKeys() {
        var top = inputViewHeight
        var left = totalWidth / 2 - (keyWidth * 10) / 2f

        for (it in lettersLine0) {
            keysLine0.add(Key(it, RectF(left, top, left + keyWidth, top + keyHeight)))
            left += keyWidth
        }
    }

    private fun drawKeyboard(canvas: Canvas) {
        keysLine0.forEach {
            drawKey(canvas, it)
        }
    }

    private fun drawKey(
        canvas: Canvas,
        key: Key
    ) {

        if (key.selected) {
            canvas.drawRect(key.bounds, keySelectedPaint)
        } else {
            canvas.drawRect(key.bounds, keyPaint)
        }

        canvas.drawText(key.ch, key.bounds.left + 20, key.bounds.top + 20, keyTextPaint)
    }

    private fun drawInput(canvas: Canvas) {
        val inputViewHeight = resources.getDimension(R.dimen.input_height)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), inputViewHeight, Paint().apply {
            style = Paint.Style.FILL
            color = Color.GREEN
        })
    }

    class Key(val ch: String, val bounds: RectF) {
        var selected = false
    }
}