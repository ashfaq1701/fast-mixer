package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.HorizontalScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bluehub.fastmixer.databinding.CustomScrollBarBinding

class ScrollBar(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private val binding: CustomScrollBarBinding
    private lateinit var mHorizontalScrollView: HorizontalScrollView
    private lateinit var mView: View

    private val mScrollBar: ScrollBar
    private val mScrollTrack: View
    private val mScrollThumb: View

    private val gestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {


            //handleScrollOfThumb()
            return true
        }
    }

    private val gestureDetector: GestureDetector

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = CustomScrollBarBinding.inflate(inflater, this, true)

        mScrollBar = this

        mScrollTrack = binding.scrollTrack
        mScrollThumb = binding.scrollThumb

        gestureDetector = GestureDetector(context, gestureListener)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    fun setHorizontalScrollView(horizontalScrollView: HorizontalScrollView) {
        this.mHorizontalScrollView = horizontalScrollView
    }

    fun setControlledView(view: View) {
        this.mView = view
        setViewWidthListener()
    }

    private fun setViewWidthListener() {
        mView.addOnLayoutChangeListener { view: View, _, _, _, _, _, _, _, _ ->

            if (mScrollTrack.width == 0) return@addOnLayoutChangeListener

            val w = view.width

            val ratio = w.toFloat() / mScrollTrack.width.toFloat()

            if (ratio == 1.0f) {
                mScrollBar.visibility = View.INVISIBLE
            } else {
                mScrollBar.visibility = View.VISIBLE

                val layoutParams = mScrollThumb.layoutParams
                layoutParams.width = (mScrollTrack.width.toFloat() / ratio).toInt()
                mScrollThumb.layoutParams = layoutParams
            }
        }
    }
}
