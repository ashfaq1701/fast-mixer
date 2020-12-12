package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


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
    lateinit var mSamplesReader: (Int) -> Deferred<Array<Float>>
    lateinit var mTotalSampleCountReader: () -> Int

    var mWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    var mHeight: BehaviorSubject<Int> = BehaviorSubject.create()
    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    lateinit var mPlotPoints: Array<Float>

    private var fileLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var totalSampleCount: Int = 0

    private val coroutineScope = MainScope()

    init {
        fileLoaded.subscribe {
            if (it) {
                totalSampleCount = mTotalSampleCountReader()
                setupObservers()
            }
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath = audioFilePath
        checkAndSetupAudioFileSource()
    }

    fun setFileLoader(fileLoader: () -> Job) {
        mFileLoader = fileLoader
        checkAndSetupAudioFileSource()
    }

    fun setSamplesReader(samplesReader: (Int) -> Deferred<Array<Float>>) {
        mSamplesReader = samplesReader
    }

    fun setTotalSampleCountReader(totalSampleCountReader: () -> Int) {
        mTotalSampleCountReader = totalSampleCountReader
        checkAndSetupAudioFileSource()
    }

    fun setupObservers() {
        mWidth.zipWith(mHeight, { first: Int, second: Int ->
            Pair(first, second)
        }).subscribe { pair ->
            fetchPointsToPlot(pair.first, pair.second)
        }

        mRawPoints.subscribe { ptsArr ->
            processPlotPoints(ptsArr)
        }
    }

    private fun fetchPointsToPlot(numSamples: Int, height: Int) {
        if (!fileLoaded.value) return

        coroutineScope.launch {
            mRawPoints.onNext(mSamplesReader(numSamples).await())
        }
    }

    private fun processPlotPoints(rawPts: Array<Float>) {
        val mean = rawPts.average()

        val maximum = rawPts.maxOrNull()

        val maxLevelInSamples = maximum ?: 3 * mean
        val maxToScale = mHeight.value * 0.95

        mPlotPoints = rawPts.map { current ->
            ((current / maxLevelInSamples.toFloat()) * maxToScale.toFloat())
        }.toTypedArray()

        invalidate()
    }

    private fun checkAndSetupAudioFileSource() {
        if (::mAudioFilePath.isInitialized
            && ::mFileLoader.isInitialized
            && ::mTotalSampleCountReader.isInitialized) {
            mFileLoader().invokeOnCompletion {
                if (it == null) {
                    fileLoaded.onNext(true)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth.onNext(w)
        mHeight.onNext(h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (! ::mPlotPoints.isInitialized) {
            return
        }

        mPlotPoints.forEachIndexed { idx, item ->
            canvas.drawLine(idx.toFloat(), mHeight.value.toFloat(), idx.toFloat(), (mHeight.value - item), paint)
        }
    }
}
