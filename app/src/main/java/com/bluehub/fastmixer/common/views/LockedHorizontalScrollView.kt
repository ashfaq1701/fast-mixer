package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class LockedHorizontalScrollView(
    context: Context,
    attrs: AttributeSet? = null) : HorizontalScrollView(context, attrs) {

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}
