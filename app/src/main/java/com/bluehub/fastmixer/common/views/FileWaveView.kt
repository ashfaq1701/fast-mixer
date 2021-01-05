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
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFile", method = "setAudioFile"),
    BindingMethod(type = FileWaveView::class, attribute = "fileWaveViewStore", method = "setFileWaveViewStore")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val mAudioFile: BehaviorSubject<AudioFile> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    private lateinit var mPlotPoints: Array<Float>

    private var attrsLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        attrsLoaded.subscribe {
            if (it) {
                setupObservers()
                requestLayout()
            }
        }

        mAudioFile.subscribe{ checkAttrs() }
        mSamplesReader.subscribe { checkAttrs() }
        mFileWaveViewStore.subscribe { checkAttrs() }
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

    fun setFileWaveViewStore(fileWaveViewStore: FileWaveViewStore) {
        mFileWaveViewStore.onNext(fileWaveViewStore)
        mFileWaveViewStore.value.isFileSampleCountMapUpdated.subscribe {
            requestLayout()
        }
    }

    private fun getZoomLevel(): Int {
        if (!mFileWaveViewStore.hasValue() || !mAudioFile.hasValue()) return 1
        return mFileWaveViewStore.value.getZoomLevel(mAudioFile.value.path) ?: 1
    }

    fun zoomIn() {
        val numSamples = getPlotNumSamples()

        val zoomLevel = getZoomLevel()
        if (zoomLevel * numSamples < mAudioFile.value.numSamples) {
            mFileWaveViewStore.value.zoomIn(mAudioFile.value.path)
            handleZoom()
        }
    }

    fun zoomOut() {
        val zoomLevel = getZoomLevel()
        if (zoomLevel >= 1 + FileWaveViewStore.ZOOM_STEP) {
            mFileWaveViewStore.value.zoomOut(mAudioFile.value.path)
            handleZoom()
        }
    }

    private fun setupObservers() {
        mRawPoints.subscribe { ptsArr ->
            processPlotPoints(ptsArr)
        }
    }

    private fun getPlotNumSamples(): Int {
        if (!mFileWaveViewStore.hasValue() || !mAudioFile.hasValue()) return 0

        return mFileWaveViewStore.value.getSampleCount(mAudioFile.value.path) ?: 0
    }

    private fun getPlotNumPts(): Int {
        val numSamples = getPlotNumSamples()

        val zoomLevel = getZoomLevel()
        return zoomLevel * numSamples
    }

    private fun fetchPointsToPlot() {
        if (!attrsLoaded.hasValue()) return

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
        val maxToScale = height * 0.95

        mPlotPoints = rawPts.map { current ->
            if (maxLevelInSamples != 0) {
                ((current / maxLevelInSamples.toFloat()) * maxToScale.toFloat())
            } else 0.0f
        }.toTypedArray()

        invalidate()
    }

    private fun checkAttrs() {
        if (mAudioFile.hasValue()
            && mFileWaveViewStore.hasValue()) {
            attrsLoaded.onNext(true)
        }
    }

    private fun handleZoom() {
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mFileWaveViewStore.hasValue()) {
            mFileWaveViewStore.value.updateMeasuredWidth(measuredWidth)
        }

        if (!mAudioFile.hasValue()) return

        val samplesCount = mFileWaveViewStore.value.getSampleCount(mAudioFile.value.path) ?: measuredWidth

        val zoomLevel = getZoomLevel()
        val calculatedWidth = zoomLevel * samplesCount

        val roundedWidth = if (measuredWidth == 0 || calculatedWidth < measuredWidth) measuredWidth else calculatedWidth

        if (roundedWidth > 0) {
            fetchPointsToPlot()
        }

        setMeasuredDimension(roundedWidth, measuredHeight)
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
            canvas.drawLine(currentPoint.toFloat(), height.toFloat(), currentPoint.toFloat(), (height - item), paint)
            currentPoint += ptsDistance
        }
    }
}
