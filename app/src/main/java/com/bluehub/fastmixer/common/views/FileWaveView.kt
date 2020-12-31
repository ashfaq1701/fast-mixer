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
import com.bluehub.fastmixer.screens.mixing.AudioFileWithNumSamples
import com.bluehub.fastmixer.screens.mixing.AudioViewSampleCountStore
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import timber.log.Timber


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "fileLoader", method = "setFileLoader"),
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFilePath", method = "setAudioFilePath"),
    BindingMethod(type = FileWaveView::class, attribute = "totalSampleCountReader", method = "setTotalSampleCountReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioViewSampleCountStore", method = "setAudioViewSampleCountStore")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    companion object {
        const val ZOOM_STEP = 1
    }

    private val mAudioFilePath: BehaviorSubject<String> = BehaviorSubject.create()
    var mFileLoader: BehaviorSubject<Function<Unit, Job>> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    var mTotalSampleCountReader: BehaviorSubject<Function<Unit, Int>> = BehaviorSubject.create()
    val mAudioViewSampleCountStore: BehaviorSubject<AudioViewSampleCountStore> = BehaviorSubject.create()

    var mWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    var mHeight: BehaviorSubject<Int> = BehaviorSubject.create()
    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()
    var mTotalSamplesCount: BehaviorSubject<Int> = BehaviorSubject.create()

    private lateinit var mPlotPoints: Array<Float>

    private var fileLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var totalSampleCount: Int = 0

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val zoomLevel: BehaviorSubject<Int> = BehaviorSubject.create()

    init {
        fileLoaded.subscribe {
            if (it) {
                totalSampleCount = mTotalSampleCountReader.value.apply(Unit)
                setupObservers()
            }
        }

        zoomLevel.onNext(1)

        mAudioFilePath.subscribe{ checkAndSetupAudioFileSource() }
        mFileLoader.subscribe { checkAndSetupAudioFileSource() }
        mSamplesReader.subscribe { checkAndSetupAudioFileSource() }
        mTotalSampleCountReader.subscribe { checkAndSetupAudioFileSource() }
        mAudioViewSampleCountStore.subscribe { checkAndSetupAudioFileSource() }
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

    fun setAudioViewSampleCountStore(audioViewSampleCountStore: AudioViewSampleCountStore) {
        mAudioViewSampleCountStore.onNext(audioViewSampleCountStore)
        mAudioViewSampleCountStore.value.isFileSampleCountMapUpdated.subscribe {
            fetchPointsToPlot()
        }
    }

    fun zoomIn() {
        if (zoomLevel.value * mWidth.value < mTotalSamplesCount.value) {
            zoomLevel.onNext(zoomLevel.value + ZOOM_STEP)
        }
    }

    fun zoomOut() {
        if (zoomLevel.value >= 1 + ZOOM_STEP) {
            zoomLevel.onNext(zoomLevel.value - ZOOM_STEP)
        }
    }

    private fun setupObservers() {
        mWidth.subscribe {
            fetchPointsToPlot()
        }

        mRawPoints.subscribe { ptsArr ->
            processPlotPoints(ptsArr)
        }

        zoomLevel.subscribe {
            handleZoom()
        }
    }

    private fun fetchPointsToPlot() {
        if (!fileLoaded.hasValue() || !mWidth.hasValue() || mWidth.value == 0) return

        val numPts = mWidth.value

        coroutineScope.launch {
            mRawPoints.onNext(mSamplesReader.value.apply(numPts).await())
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
            && mTotalSampleCountReader.hasValue()
            && mAudioViewSampleCountStore.hasValue()) {
            mFileLoader.value.apply(Unit).invokeOnCompletion {
                if (it == null) {
                    mTotalSamplesCount.onNext(mTotalSampleCountReader.value.apply(Unit))

                    mAudioViewSampleCountStore.value.addAudioFile(AudioFileWithNumSamples(
                            mAudioFilePath.value,
                            mTotalSamplesCount.value
                    ))

                    fileLoaded.onNext(true)
                }
            }
        }
    }

    private fun handleZoom() {
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val calculatedWidth = if (zoomLevel.hasValue()) {
            zoomLevel.value * measuredWidth
        } else {
            measuredWidth
        }
        setMeasuredDimension(calculatedWidth, measuredHeight)
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

        val widthPtRatio = mWidth.value / mPlotPoints.size
        val ptsDistance: Int = if (widthPtRatio >= 1) widthPtRatio else 1

        var currentPoint = 0
        mPlotPoints.forEach { item ->
            canvas.drawLine(currentPoint.toFloat(), mHeight.value.toFloat(), currentPoint.toFloat(), (mHeight.value - item), paint)
            currentPoint += ptsDistance
        }
    }
}
