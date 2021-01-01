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
import com.bluehub.fastmixer.screens.mixing.AudioFile
import com.bluehub.fastmixer.screens.mixing.AudioViewSampleCountStore
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import timber.log.Timber


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFile", method = "setAudioFile"),
    BindingMethod(type = FileWaveView::class, attribute = "audioViewSampleCountStore", method = "setAudioViewSampleCountStore")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    companion object {
        const val ZOOM_STEP = 1
    }

    private val mAudioFile: BehaviorSubject<AudioFile> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    private val mAudioViewSampleCountStore: BehaviorSubject<AudioViewSampleCountStore> = BehaviorSubject.create()

    var mWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    var mHeight: BehaviorSubject<Int> = BehaviorSubject.create()
    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    private lateinit var mPlotPoints: Array<Float>

    private var attrsLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val zoomLevel: BehaviorSubject<Int> = BehaviorSubject.create()

    init {
        attrsLoaded.subscribe {
            if (it) {
                setupObservers()
            }
        }

        zoomLevel.onNext(1)

        mAudioFile.subscribe{ checkAttrs() }
        mSamplesReader.subscribe { checkAttrs() }
        mAudioViewSampleCountStore.subscribe { checkAttrs() }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun setAudioFile(audioFile: AudioFile) {
        mAudioFile.onNext(audioFile)
    }

    fun setSamplesReader(samplesReader: (Int) -> Deferred<Array<Float>>) {
        mSamplesReader.onNext(samplesReader)
    }

    fun setAudioViewSampleCountStore(audioViewSampleCountStore: AudioViewSampleCountStore) {
        mAudioViewSampleCountStore.onNext(audioViewSampleCountStore)
        mAudioViewSampleCountStore.value.isFileSampleCountMapUpdated.subscribe {
            requestLayout()
        }
    }

    fun zoomIn() {
        val numSamples = getPlotNumSamples()

        if (zoomLevel.value * numSamples < mAudioFile.value.numSamples) {
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

    private fun getPlotNumSamples(): Int {
        if (!mAudioViewSampleCountStore.hasValue() || !mAudioFile.hasValue()) return 0

        return mAudioViewSampleCountStore.value.getSampleCount(mAudioFile.value.path) ?: 0
    }

    private fun getPlotNumPts(): Int {
        val numSamples = getPlotNumSamples()

        return if (zoomLevel.hasValue()) {
            zoomLevel.value * numSamples
        } else numSamples
    }

    private fun fetchPointsToPlot() {
        if (!attrsLoaded.hasValue() || !mWidth.hasValue() || mWidth.value == 0) return

        val numPts = getPlotNumPts()

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

    private fun checkAttrs() {
        if (mAudioFile.hasValue()
            && mAudioViewSampleCountStore.hasValue()) {
            attrsLoaded.onNext(true)
        }
    }

    private fun handleZoom() {
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mAudioViewSampleCountStore.hasValue()) {
            mAudioViewSampleCountStore.value.updateMeasuredWidth(measuredWidth)
        }

        if (!mAudioFile.hasValue()) return

        val samplesCount = mAudioViewSampleCountStore.value.getSampleCount(mAudioFile.value.path) ?: measuredWidth

        val calculatedWidth = if (zoomLevel.hasValue()) {
            zoomLevel.value * samplesCount
        } else {
            samplesCount
        }

        val roundedWidth = if (calculatedWidth < measuredWidth) measuredWidth else calculatedWidth

        // If sizes are same but points are empty then still points has to be fetched
        if (roundedWidth == mWidth.value
            && measuredHeight == mHeight.value) {
            fetchPointsToPlot()
        }

        setMeasuredDimension(roundedWidth, measuredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mWidth.value != w) {
            mWidth.onNext(w)
        }
        mHeight.onNext(h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!::mPlotPoints.isInitialized) {
            return
        }

        val numPts = getPlotNumPts()
        val widthPtRatio = numPts / mPlotPoints.size
        val ptsDistance: Int = if (widthPtRatio >= 1) widthPtRatio else 1

        var currentPoint = 0
        mPlotPoints.forEach { item ->
            canvas.drawLine(currentPoint.toFloat(), mHeight.value.toFloat(), currentPoint.toFloat(), (mHeight.value - item), paint)
            currentPoint += ptsDistance
        }
    }
}
