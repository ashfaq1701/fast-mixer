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
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*


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
    private val mAudioFilePath: BehaviorSubject<String> = BehaviorSubject.create()
    var mFileLoader: BehaviorSubject<Function<Unit, Job>> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    var mTotalSampleCountReader: BehaviorSubject<Function<Unit, Int>> = BehaviorSubject.create()

    var mWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    var mHeight: BehaviorSubject<Int> = BehaviorSubject.create()
    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    lateinit var mPlotPoints: Array<Float>

    private var fileLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var totalSampleCount: Int = 0

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        fileLoaded.subscribe {
            if (it) {
                totalSampleCount = mTotalSampleCountReader.value.apply(Unit)
                setupObservers()
            }
        }

        mAudioFilePath.subscribe{ checkAndSetupAudioFileSource() }
        mFileLoader.subscribe { checkAndSetupAudioFileSource() }
        mSamplesReader.subscribe { checkAndSetupAudioFileSource() }
        mTotalSampleCountReader.subscribe { checkAndSetupAudioFileSource() }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun setAudioFilePath(audioFilePath: String) {
        mAudioFilePath.onNext(audioFilePath)
    }

    fun setFileLoader(fileLoader: (Unit) -> Job) {
        mFileLoader.onNext(fileLoader)
    }

    fun setSamplesReader(samplesReader: (Int) -> Deferred<Array<Float>>) {
        mSamplesReader.onNext(samplesReader)
    }

    fun setTotalSampleCountReader(totalSampleCountReader: (Unit) -> Int) {
        mTotalSampleCountReader.onNext(totalSampleCountReader)
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
        if (!fileLoaded.value || width == 0) return

        coroutineScope.launch {
            mRawPoints.onNext(mSamplesReader.value.apply(numSamples).await())
        }
    }

    private fun processPlotPoints(rawPts: Array<Float>) {
        if (rawPts.isEmpty()) {
            return
        }

        val mean = rawPts.average()

        val maximum = rawPts.maxOrNull()

        val maxLevelInSamples = maximum ?: 3 * mean
        val maxToScale = mHeight.value * 0.95

        mPlotPoints = rawPts.map { current ->
            if (maxLevelInSamples != 0) {
                ((current / maxLevelInSamples.toFloat()) * maxToScale.toFloat())
            } else 0.0f
        }.toTypedArray()

        invalidate()
    }

    private fun checkAndSetupAudioFileSource() {
        if (mAudioFilePath.hasValue()
            && mFileLoader.hasValue()
            && mTotalSampleCountReader.hasValue()) {
            mFileLoader.value.apply(Unit).invokeOnCompletion {
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
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
