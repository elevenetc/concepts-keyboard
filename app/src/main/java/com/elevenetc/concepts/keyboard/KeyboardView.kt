package com.elevenetc.concepts.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class KeyboardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    init {
        setBackgroundColor(Color.RED)
    }

    private val keyHorizontalCount = 10
    private val lettersLine0 = arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val keysLine0 = mutableListOf<Key>()
    private val inputBounds = RectF()

    var inputTapHandler:() -> Unit = {}

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawInput(canvas)
        drawKeyboard(canvas)
        canvas.drawCircle(touchX, touchY, 100f, keyPaint)
        canvas.drawCircle(touchX, touchY, 10f, keyPaint)
        canvas.drawCircle(touchX, touchY, 15f, keyPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {


        val action = event.action
        val x = event.x
        val y = event.y
        val handled: Boolean

        touchX = x
        touchY = y

        invalidate()

        if (inputBounds.contains(x, y)) {


            if(action == MotionEvent.ACTION_UP){
                inputTapHandler()
            }

            //return super.onTouchEvent(event)
            return true
        }

        handled = handleTouchEvent(action, x, y)

        return if (!handled) super.onTouchEvent(event)
        else handled
    }

    private fun handleTouchEvent(
        action: Int,
        x: Float,
        y: Float
    ): Boolean {
        var handled = false
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_MOVE) {


            keysLine0.forEach {
                val contains = it.bounds.contains(x, y)
                it.selected = contains

                if (contains) {
                    handled = true
                }
            }
        }
        return handled
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