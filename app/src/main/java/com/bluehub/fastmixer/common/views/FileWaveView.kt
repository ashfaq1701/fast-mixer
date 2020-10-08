package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getStringOrThrow
import com.bluehub.fastmixer.R

class AudioFileReader {

}

class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    filePath: String? = null,
    channelCount: Int? = null,
    sampleRate: Int? = null,

) : View(context, attrs) {
    private val audioFilePath: String
    private val audioFileChannelCount: Int
    private val audioFileSampleRate: Int

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FileWaveView)

        audioFilePath = filePath ?: typedArray.getStringOrThrow(R.styleable.FileWaveView_audioFilePath)
        audioFileChannelCount = channelCount ?: typedArray.getIntOrThrow(R.styleable.FileWaveView_channelCount)
        audioFileSampleRate = sampleRate ?: typedArray.getIntOrThrow(R.styleable.FileWaveView_sampleRate)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
