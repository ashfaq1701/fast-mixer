package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import kotlinx.coroutines.Job


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "fileLoader", method = "setFileLoader"),
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFilePath", method = "setAudioFilePath"),
    BindingMethod(type = FileWaveView::class, attribute = "totalSampleCountReader", method = "setTotalSampleCountReader")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private lateinit var mAudioFilePath: String

    lateinit var mFileLoader: (String) -> Job
    lateinit var mSamplesReader: (Int) -> Array<Float>
    lateinit var mTotalSampleCountReader: () -> Int

    private var fileLoaded = false
    private var totalSampleCount: Int = 0

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath = audioFilePath
        checkAndSetupAudioFileSource()
    }

    fun setFileLoader(fileLoader: (String) -> Job) {
        mFileLoader = fileLoader
        checkAndSetupAudioFileSource()
    }

    fun setSamplesReader(samplesReader: (Int) -> Array<Float>) {
        mSamplesReader = samplesReader
    }

    fun setTotalSampleCountReader(totalSampleCountReader: () -> Int) {
        mTotalSampleCountReader = totalSampleCountReader
        checkAndSetupAudioFileSource()
    }

    private fun checkAndSetupAudioFileSource() {
        if (::mAudioFilePath.isInitialized
            && ::mFileLoader.isInitialized
            && ::mTotalSampleCountReader.isInitialized) {
            mFileLoader(mAudioFilePath).invokeOnCompletion {
                if (it == null) {
                    fileLoaded = true
                    totalSampleCount = mTotalSampleCountReader()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
