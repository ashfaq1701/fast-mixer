package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bluehub.fastmixer.databinding.AudioWidgetSliderBinding

class AudioWidgetSlider(context: Context, attributeSet: AttributeSet?)
    : LinearLayout(context, attributeSet) {

    private val binding: AudioWidgetSliderBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AudioWidgetSliderBinding.inflate(inflater, this, true)
    }

}
