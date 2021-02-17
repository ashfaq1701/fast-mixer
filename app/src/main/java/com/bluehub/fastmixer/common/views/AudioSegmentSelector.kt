package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bluehub.fastmixer.databinding.AudioSegmentSelectorBinding

class AudioSegmentSelector(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private val binding: AudioSegmentSelectorBinding
    private val leftTrigger: View
    private val rightTrigger: View

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AudioSegmentSelectorBinding.inflate(inflater, this, true)

        leftTrigger = binding.leftTrigger
        rightTrigger = binding.rightTrigger
    }


}
