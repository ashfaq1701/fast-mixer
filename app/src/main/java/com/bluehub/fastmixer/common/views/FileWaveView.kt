package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import kotlinx.coroutines.Job
import timber.log.Timber
import kotlin.random.Random


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

    lateinit var mFileLoader: () -> Job
    lateinit var mSamplesReader: (Int) -> Array<Float>
    lateinit var mTotalSampleCountReader: () -> Int

    var mWidth: Int = 0
    var mHeight: Int = 0

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

    fun setFileLoader(fileLoader: () -> Job) {
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
            mFileLoader().invokeOnCompletion {
                if (it == null) {
                    fileLoaded = true
                    totalSampleCount = mTotalSampleCountReader()
                }
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mHeight = h
        invalidate()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mWidth == 0 || mHeight == 0) {
            return
        }
        for (i in 0 until mWidth) {
            val randNum = Random.nextInt(0, mHeight)
            canvas.drawLine(i.toFloat(), mHeight.toFloat(), i.toFloat(), (mHeight - randNum).toFloat(), paint)
        }
        //if(::mAudioFilePath.isInitialized) {
            //canvas.drawText("$mAudioFilePath, width: $mWidth, height: $mHeight", 10F, 10F, paint)
        //}
    }
}
