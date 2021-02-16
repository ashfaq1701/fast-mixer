package com.bluehub.fastmixer.common.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.*
import android.util.AttributeSet
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlin.math.*


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFileUiState", method = "setAudioFileUiState"),
    BindingMethod(type = FileWaveView::class, attribute = "fileWaveViewStore", method = "setFileWaveViewStore")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val mAudioFileUiState: BehaviorSubject<AudioFileUiState> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    private lateinit var mAudioWidgetSlider: AudioWidgetSlider

    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    private lateinit var mPlotPoints: Array<Float>

    private var attrsLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var forceFetch = false

    private val gestureListener = object:GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent?) {
            e?:return
            val x = e.x
            handleLongClick(x)
        }
    }
    private val gestureDetector: GestureDetector

    private var mAudioSegmentSelector: AudioSegmentSelector? = null

    init {
        attrsLoaded.subscribe {
            if (it) {
                setupObservers()
            }
        }

        mAudioFileUiState.subscribe{ checkAttrs() }
        mSamplesReader.subscribe { checkAttrs() }
        mFileWaveViewStore.subscribe { checkAttrs() }

        gestureDetector = GestureDetector(context, gestureListener)

        setWillNotDraw(false)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        mAudioFileUiState.onNext(audioFileUiState)
    }

    fun setSamplesReader(samplesReader: (Int) -> Deferred<Array<Float>>) {
        mSamplesReader.onNext(samplesReader)
    }

    fun setFileWaveViewStore(fileWaveViewStore: FileWaveViewStore) {
        mFileWaveViewStore.onNext(fileWaveViewStore)
    }

    fun zoomIn() {
        if (mFileWaveViewStore.value.zoomIn(mAudioFileUiState.value)) {
            handleZoom()
        }
    }

    fun zoomOut() {
        if (mFileWaveViewStore.value.zoomOut(mAudioFileUiState.value)) {
            handleZoom()
        }
    }

    private fun resetZoom() {
        if (mFileWaveViewStore.hasValue() && mAudioFileUiState.hasValue()) {
            mFileWaveViewStore.value.resetZoomLevel(mAudioFileUiState.value.path)
            handleZoom()
        }
    }

    private fun setupObservers() {
        mRawPoints.subscribe { ptsArr ->
            processPlotPoints(ptsArr)
        }

        mAudioFileUiState.value.displayPtsCount.subscribe {
            requestLayout()
        }
        mAudioFileUiState.value.zoomLevel.subscribe {
            handleZoom()
        }
        mAudioFileUiState.value.playSliderPosition
            .observeOn(
                AndroidSchedulers.mainThread()
            )
            .subscribe {
                requestLayout()
            }
        mAudioFileUiState.value.reRender
            .observeOn(
                AndroidSchedulers.mainThread()
            )
            .subscribe {
                forceFetch = true
                requestLayout()
            }

        mAudioFileUiState.value.showSegmentSelector
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    removeSegmentSelector()
                    createAndShowSegmentSelector()
                } else {
                    removeSegmentSelector()
                }
            }
    }

    private fun getNumPtsToPlot() = if (mAudioFileUiState.hasValue()) {
        mAudioFileUiState.value.numPtsToPlot
    } else 0

    private fun fetchPointsToPlot() {
        if (!attrsLoaded.hasValue()) return

        val numPts = getNumPtsToPlot()

        if (!mRawPoints.hasValue() || mRawPoints.value.size != numPts || forceFetch) {
            mFileWaveViewStore.value.coroutineScope.launch {
                mRawPoints.onNext(mSamplesReader.value.apply(numPts).await())
            }
            forceFetch = false
        }
    }

    private fun processPlotPoints(rawPts: Array<Float>) {
        if (rawPts.isEmpty()) {
            return
        }

        val maximumAbs = rawPts.fold(Float.MIN_VALUE) { acc, current ->
            if (abs(current) > acc) {
                abs(current)
            } else acc
        }

        val maxToScale = height * 0.48

        mPlotPoints = rawPts.map { current ->
            if (maximumAbs != 0.0f) {
                ((current / maximumAbs) * maxToScale.toFloat())
            } else 0.0f
        }.toTypedArray()

        invalidate()
    }

    private fun checkAttrs() {
        if (mAudioFileUiState.hasValue()
            && mFileWaveViewStore.hasValue()) {
            attrsLoaded.onNext(true)
        }
    }

    private fun handleZoom() {
        requestLayout()
    }

    private fun vibrateDevice() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(
                    250,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            v.vibrate(250)
        }
    }

    private fun handleLongClick(xPosition: Float) {
        mFileWaveViewStore.value.setPlayHead(mAudioFileUiState.value.path, xPosition.toInt())
        vibrateDevice()
    }

    private fun createAndShowSegmentSelector() {
        val audioSegmentSelector = AudioSegmentSelector(context, null)
        audioSegmentSelector.id = View.generateViewId()

        audioSegmentSelector.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        addView(audioSegmentSelector)

        mAudioSegmentSelector = audioSegmentSelector
    }

    private fun removeSegmentSelector() {
        mAudioSegmentSelector?.let {
            removeView(it)
        }
        mAudioSegmentSelector = null

        mAudioFileUiState.value.run {
            segmentStartSample = null
            segmentEndSample = null
        }
    }

    private fun calculateSegmentSelectorProperties(): Int {
        if (!mAudioFileUiState.hasValue()) return 0

        return mAudioFileUiState.value.run {
            if (numPtsToPlot == 0 || numSamples == 0) return 0

            segmentStartSample ?: run {
                val sliderStartPosition = getSliderLeftPosition()
                segmentStartSample = ((sliderStartPosition.toDouble() / numPtsToPlot) * numSamples).toInt()
            }

            segmentEndSample ?: run {
                val initWidth = context.resources.displayMetrics.widthPixels / 10.0
                val widthInSamples = floor((initWidth / numPtsToPlot) * numSamples).toInt()

                val startSample = segmentStartSample ?: 0

                segmentEndSample = if (startSample + widthInSamples >= numSamples) {
                    numSamples - 1
                } else {
                    startSample + widthInSamples
                }
            }

            val startSample = segmentStartSample ?: 0
            val endSample = segmentEndSample ?: 0

            val estWidth = ceil(((endSample.toDouble() - startSample.toDouble()) / numSamples) * numPtsToPlot).toInt()

            if (segmentSelectorLeft + estWidth >= numPtsToPlot) {
                numPtsToPlot - segmentSelectorLeft - 1
            } else estWidth
        }
    }

    private fun getSliderLeftPosition() = if (mAudioFileUiState.hasValue()) {
        if (mAudioFileUiState.value.playSliderPosition.hasValue()) {
            mAudioFileUiState.value.playSliderPosition.value
        } else 0
    } else 0

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (::mAudioWidgetSlider.isInitialized) {
            val sliderLeft = getSliderLeftPosition()
            val sliderTop = 0
            if (mAudioWidgetSlider.visibility != GONE) {
                mAudioWidgetSlider.layout(
                    sliderLeft,
                    sliderTop,
                    sliderLeft + mAudioWidgetSlider.measuredWidth,
                    sliderTop + mAudioWidgetSlider.measuredHeight
                )
            }

            mAudioSegmentSelector?.let {
                if (!mAudioFileUiState.hasValue()) return@let

                mAudioFileUiState.value.run {

                    val selectorTop = 0

                    if (it.visibility != GONE && it.measuredWidth > 0) {
                        it.layout(
                            segmentSelectorLeft,
                            selectorTop,
                            segmentSelectorRight,
                            selectorTop + it.measuredHeight
                        )
                    }
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mFileWaveViewStore.hasValue()) {
            mFileWaveViewStore.value.updateMeasuredWidth(measuredWidth)
        }

        if (!mAudioFileUiState.hasValue()) return

        if (childCount == 1) {
            val child = getChildAt(0)
            if (child is AudioWidgetSlider && !::mAudioWidgetSlider.isInitialized) {
                mAudioWidgetSlider = child
            }
        }

        if (::mAudioWidgetSlider.isInitialized) {
            val sliderWidth = context.resources.getDimension(R.dimen.audio_view_slider_line_width)
            mAudioWidgetSlider.measure(
                MeasureSpec.makeMeasureSpec(
                    ceil(sliderWidth).toInt(),
                    MeasureSpec.EXACTLY
                ),
                measuredHeight
            )
        }

        mAudioSegmentSelector?.let {
            val segmentSelectorWidth = calculateSegmentSelectorProperties()
            it.measure(
                MeasureSpec.makeMeasureSpec(
                    segmentSelectorWidth,
                    MeasureSpec.EXACTLY
                ),
                measuredHeight
            )
        }

        val calculatedWidth = mAudioFileUiState.value.numPtsToPlot

        val roundedWidth = if (measuredWidth == 0 || calculatedWidth < measuredWidth) measuredWidth else calculatedWidth

        if (roundedWidth > 0) {
            fetchPointsToPlot()
        }

        setMeasuredDimension(roundedWidth, measuredHeight)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        resetZoom()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!::mPlotPoints.isInitialized) {
            return
        }

        val numPts = getNumPtsToPlot()
        val widthPtRatio = numPts / mPlotPoints.size
        val ptsDistance: Int = if (widthPtRatio >= 1) widthPtRatio else 1

        var currentPoint = 0

        val baseLevel = height / 2

        mPlotPoints.forEach { item ->
            canvas.drawLine(currentPoint.toFloat(), baseLevel.toFloat(), currentPoint.toFloat(), (baseLevel - item), paint)
            currentPoint += ptsDistance
        }
    }
}
