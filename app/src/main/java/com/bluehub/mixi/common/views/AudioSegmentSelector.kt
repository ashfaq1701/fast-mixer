package com.bluehub.mixi.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.bluehub.mixi.R
import com.bluehub.mixi.databinding.AudioSegmentSelectorBinding

class AudioSegmentSelector(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private val binding: AudioSegmentSelectorBinding

    private var leftEdge: View
    private var rightEdge: View

    var leftEdgeActivated: Boolean = false
    var rightEdgeActivated: Boolean = false

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AudioSegmentSelectorBinding.inflate(inflater, this, true)

        leftEdge = binding.leftEdge
        rightEdge = binding.rightEdge
    }

    fun activateLeftEdge() {
        leftEdge.backgroundTintList = AppCompatResources.getColorStateList(context, R.color.white)
        leftEdgeActivated = true
    }

    fun disableLeftEdge() {
        leftEdge.backgroundTintList = AppCompatResources.getColorStateList(context, R.color.gray)
        leftEdgeActivated = false
    }

    fun activateRightEdge() {
        rightEdge.backgroundTintList = AppCompatResources.getColorStateList(context, R.color.white)
        rightEdgeActivated = true
    }

    fun disableRightEdge() {
        rightEdge.backgroundTintList = AppCompatResources.getColorStateList(context, R.color.gray)
        rightEdgeActivated = false
    }

    fun disableAllEdges() {
        disableLeftEdge()
        disableRightEdge()
    }
}
