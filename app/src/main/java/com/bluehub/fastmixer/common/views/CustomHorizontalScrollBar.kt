package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.HorizontalScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bluehub.fastmixer.databinding.CustomScrollBarBinding
import timber.log.Timber

class CustomHorizontalScrollBar(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private val binding: CustomScrollBarBinding
    private lateinit var mHorizontalScrollView: HorizontalScrollView
    private lateinit var mView: View

    private val mScrollBar: CustomHorizontalScrollBar
    private val mScrollTrack: View
    private val mScrollThumb: View

    private val gestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            handleScrollOfThumb(e2, distanceX)
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
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
        return gestureDetector.onTouchEvent(event)
    }

    fun setHorizontalScrollView(horizontalScrollView: HorizontalScrollView) {
        this.mHorizontalScrollView = horizontalScrollView
    }

    fun setControlledView(view: View) {
        this.mView = view
        setViewWidthListener()
    }

    private fun handleScrollOfThumb(event: MotionEvent?, distanceX: Float) {
        event?: return

        if (event.x >= mScrollThumb.left && event.x <= mScrollThumb.left + mScrollThumb.width) {

            var newLeft = mScrollThumb.left - distanceX.toInt()
            var newRight = mScrollThumb.right - distanceX.toInt()

            if (newRight <= mScrollBar.width && newLeft >= 0) {
                repositionScrollThumb(newLeft, newRight)
            }
            // Thumb is not at left nor right, but distanced asked to traverse by move is more
            else if (mScrollThumb.left != 0 && mScrollThumb.right < mScrollBar.width - 1) {

                // Distance from right end of bar
                val distanceToRight = mScrollBar.width - mScrollThumb.right - 1

                // Get the closest distance to move the thumb to
                val newDist = if (mScrollThumb.left < distanceToRight) {
                    mScrollThumb.left
                } else {
                    -distanceToRight
                }

                newLeft = mScrollThumb.left - newDist
                newRight = mScrollThumb.right - newDist

                repositionScrollThumb(newLeft, newRight)
            }
        }
    }

    private fun repositionScrollThumb(newLeft: Int, newRight: Int) {
        //mScrollThumb.post {
            Timber.d("REPOSITION SCROLL THUMB, $newLeft, $newRight")
            mScrollThumb.layout(newLeft, mScrollThumb.top, newRight, mScrollThumb.bottom)
        //}
        performScrollOnScrollView()
    }

    private fun performScrollOnScrollView() {
        val ratio = mView.width.toFloat() / mScrollBar.width.toFloat()
        val posToScroll = (ratio * mScrollThumb.left).toInt()
        mHorizontalScrollView.post {
            mHorizontalScrollView.scrollTo(posToScroll, mHorizontalScrollView.top)
        }
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

                val newWidth = (mScrollTrack.width.toFloat() / ratio).toInt()

                layoutParams.width = newWidth

                if (mScrollThumb.width != newWidth) {
                    mScrollThumb.layoutParams = layoutParams
                }
            }
        }
    }
}
