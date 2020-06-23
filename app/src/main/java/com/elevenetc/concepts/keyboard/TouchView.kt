package com.elevenetc.concepts.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TouchView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var intercept = false
    var receiver: (MotionEvent) -> Unit = {}
    val inputHeight = resources.getDimension(R.dimen.input_height)

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return if (intercept) {
            receiver(
                MotionEvent.obtain(
                    event.downTime,
                    event.eventTime,
                    event.action,
                    event.x,
                    event.y + inputHeight,
                    event.metaState
                )
            )
            true
        } else {
            super.onTouchEvent(event)
        }
    }
}