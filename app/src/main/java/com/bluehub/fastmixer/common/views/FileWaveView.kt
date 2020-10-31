package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import timber.log.Timber


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFilePath", method = "setAudioFilePath"),
    BindingMethod(type = FileWaveView::class, attribute = "totalSampleCountReader", method = "setTotalSampleCountReader")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), LifecycleOwner {
    private var registry: LifecycleRegistry = LifecycleRegistry(this)

    private lateinit var mAudioFilePath: String

    lateinit var mSamplesReader: () -> Deferred<Array<Float>>
    lateinit var mTotalSampleCountReader: () -> Deferred<Int>

    private var fileLoaded = false
    private var totalSampleCount: Int = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath = audioFilePath
        checkAndSetupAudioFileSource()
    }

    fun setSamplesReader(samplesReader: () -> Deferred<Array<Float>>) {
        mSamplesReader = samplesReader
        checkAndSetupAudioFileSource()
    }

    fun setTotalSampleCountReader(totalSampleCountReader: () -> Deferred<Int>) {
        mTotalSampleCountReader = totalSampleCountReader
        checkAndSetupAudioFileSource()
    }

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    fun checkAndSetupAudioFileSource() {
        if (::mAudioFilePath.isInitialized
            && ::mSamplesReader.isInitialized
            && ::mTotalSampleCountReader.isInitialized) {
            lifecycleScope.launch {
                select {
                    mTotalSampleCountReader().onAwait { samplesCount ->
                        totalSampleCount = samplesCount
                        Timber.d("Total samples: %s", totalSampleCount)
                        invalidate()
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(::mAudioFilePath.isInitialized) {
            canvas.drawText(mAudioFilePath, 10F, 10F, paint)
        }
    }
}
