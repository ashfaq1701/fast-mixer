package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "fileLoader", method = "setFileLoader"),
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFilePath", method = "setAudioFilePath")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private lateinit var mAudioFilePath: String

    lateinit var mFileLoader: (String) -> Unit
    lateinit var mSamplesReader: (Int) -> Array<Float>

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath = audioFilePath
        checkAndSetupAudioFileSource()
    }

    fun setFileLoader(fileLoader: (String) -> Unit) {
        mFileLoader = fileLoader
        checkAndSetupAudioFileSource()
    }

    fun setSamplesReader(samplesReader: (Int) -> Array<Float>) {
        mSamplesReader = samplesReader
    }

    private fun checkAndSetupAudioFileSource() {
        if (::mAudioFilePath.isInitialized && ::mFileLoader.isInitialized) {
            mFileLoader(mAudioFilePath)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
