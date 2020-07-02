package com.elevenetc.concepts.keyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.elevenetc.utils.kotlin.math.ElevenMath
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
        for ((index, point) in events.withIndex()) {
            //TODO: perf: O(x^2) per draw call

            val x = point.x
            val y = point.y
            if (index == 0) {
                path.moveTo(x, y)
                prevTime = point.time
            } else {

                diffTime = point.time - prevTime

                if (diffTime > 20) {
                    canvas.drawCircle(x, y, 10f, keyPaint)
                    canvas.drawCircle(x, y, 20f, keyPaint)
                    canvas.drawCircle(x, y, 30f, keyPaint)
                }

                prevTime = point.time

                path.lineTo(x, y)
            }
        }

        Log.d("angle", "---")
        Log.d("angle", "total: " + events.size)

        val step = 10
        for (i in 0 until events.size step step) {
            val p0: MovePoint? = events.getOrNull(0)
            val p1: MovePoint? = events.getOrNull(i + step / 2)
            val p2: MovePoint? = events.getOrNull(i + step)

            if (p0 != null && p1 != null && p2 != null) {
                val p0x = p0.x
                val p0y = p0.y
                val p1x = p1.x
                val p1y = p1.y
                val p2x = p2.x
                val p2y = p2.y
                val angleBetween = m.angleBetween(
                    p0x, p0y, p1x, p1y,
                    p1x, p1y, p2x, p2y
                )
                Log.d("angle", angleBetween.toString())
            }
        }

        canvas.drawPath(path, pathPaint)
    }

    val m: ElevenMath = ElevenMath.create()

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