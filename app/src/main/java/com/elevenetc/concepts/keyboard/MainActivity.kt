package com.elevenetc.concepts.keyboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var keyboardManager: KeyboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set us to non-modal, so that others can receive the outside touch events.
        window.setFlags(FLAG_NOT_TOUCH_MODAL, FLAG_NOT_TOUCH_MODAL);
        //and watch for outside touch events too
        window.setFlags(FLAG_WATCH_OUTSIDE_TOUCH, FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = Ad()
        disableClipOnParents(root)



        keyboardManager = KeyboardManager(root, recyclerView, keyboardView, touchView)
    }

    fun disableClipOnParents(v: View) {
        if (v.parent == null) return
        if (v is ViewGroup) v.clipChildren = false
        if (v.parent is View) disableClipOnParents(v.parent as View)
    }

    override fun onBackPressed() {

        if (keyboardManager.opened) {
            keyboardManager.close()
        } else {
            super.onBackPressed()
        }

    }

    //    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val handled = keyboardView.zed(event)
//        return if (handled) {
//            true
//        } else {
//            super.onTouchEvent(event)
//        }
//    }

    class KeyboardManager(
        val container: View,
        val recyclerView: RecyclerView,
        val keyboardView: KeyboardView,
        val touchView: TouchView
    ) {

        var opened = false

        init {
            keyboardView.inputTapHandler = {
                if (!opened) {
                    open()
                }
            }
            touchView.receiver = {
                keyboardView.onTouchEvent(it)
            }
        }

        var prev = 0

        private fun open() {

            touchView.intercept = true
            val dimension = touchView.resources.getDimension(R.dimen.keyboard_height)


            //touchView.translationY = dimension
            //(touchView.layoutParams as ViewGroup.MarginLayoutParams).tra = dimension.toInt()
            //touchView.requestLayout()

            opened = true
            val keyboardSize = container.resources.getDimension(R.dimen.keyboard_height)
            val inputSize = container.resources.getDimension(R.dimen.input_height)
            container.animate()
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(300)
                .translationYBy((keyboardSize - inputSize) * -1)
                .withEndAction {

                }
                .start()
        }

        fun close() {

            touchView.intercept = false
            //touchView.translationY = 0f
            //(touchView.layoutParams as ViewGroup.MarginLayoutParams).editorAbsoluteY = 0
            //touchView.requestLayout()

            opened = false
            container.animate()
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(300)
                .translationY(0f)
                .withEndAction {

                }
                .start()
        }
    }

    class Ad : RecyclerView.Adapter<Ad.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            fun bind(position: Int) {
                (itemView as TextView).text = "item: $position"
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(TextView(parent.context))
        }

        override fun getItemCount(): Int {
            return 50
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(position)
        }
    }
}