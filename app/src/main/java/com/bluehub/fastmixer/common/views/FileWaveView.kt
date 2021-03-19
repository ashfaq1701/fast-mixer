package com.bluehub.fastmixer.common.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.*
import android.util.AttributeSet
import android.view.*
import androidx.core.content.ContextCompat
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.utils.getCurrentBackground
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlin.math.*

class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    companion object {
        const val TAIL_WIDTH = 3
        const val SAMPLE_MIN_THRESHOLD: Float = 1.0E-20F
    }

    private val mAudioFileUiState: BehaviorSubject<AudioFileUiState> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    private lateinit var mAudioWidgetSlider: AudioWidgetSlider

    private lateinit var mRawPoints: Array<Float>
    private var mRawPointsSize: Int = 0

    private lateinit var mPlotPoints: Array<Float>

    private var mBitmap: Bitmap? = null

    private var attrsLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var forceFetch = false

    private var segmentSliderActivationX: Float? = null

    private val gestureListener = object:GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent?) {
            e?:return
            val x = e.x
            handleLongClick(x)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    }
    private val gestureDetector: GestureDetector

    private var mAudioSegmentSelector: AudioSegmentSelector? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    private val clearPaint = Paint().apply {
        style = Paint.Style.FILL
        color = getCurrentBackground() ?: Color.WHITE
    }

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

        mAudioFileUiState.value.displayPtsCount
            .observeOn(
                AndroidSchedulers.mainThread()
            )
            .subscribe {
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
                    createAndShowSegmentSelector()
                } else {
                    removeSegmentSelector()
                }
            }

        mAudioFileUiState.value.segmentStartSample
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it?.let {
                    requestLayout()
                }
            }

        mAudioFileUiState.value.segmentEndSample
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it?.let {
                    requestLayout()
                }
            }
    }

    private fun getNumPtsToPlot() = if (mAudioFileUiState.hasValue()) {
        mAudioFileUiState.value.numPtsToPlot
    } else 0

    private fun fetchPointsToPlot() {
        if (!attrsLoaded.hasValue()) return

        val numPts = getNumPtsToPlot()

        if (!::mRawPoints.isInitialized || mRawPointsSize != numPts || forceFetch) {
            mFileWaveViewStore.value.coroutineScope.launch {
                mRawPoints = mSamplesReader.value.apply(numPts).await()
                mRawPointsSize = mRawPoints.size
                forceFetch = false
                processPlotPoints()
            }
        }
    }

    private fun processPlotPoints() {
        if (!::mRawPoints.isInitialized || mRawPoints.isEmpty()) {
            return
        }

        mFileWaveViewStore.value.coroutineScope.launch {
            withContext(Dispatchers.Default) {
                val maximumAbs = mRawPoints.fold(0.0f) { acc, current ->
                    val maxAbs = if (abs(current) > acc) {
                        abs(current)
                    } else acc

                    if (maxAbs < SAMPLE_MIN_THRESHOLD) {
                        0.0f
                    } else maxAbs
                }

                val maxToScale = height * 0.48

                mPlotPoints = mRawPoints.map { current ->
                    if (maximumAbs != 0.0f) {
                        ((current / maximumAbs) * maxToScale.toFloat())
                    } else 0.0f
                }.toTypedArray()

                mRawPoints.dropWhile { true }
            }

            createAndDrawCanvas()
        }
    }

    private fun createAndDrawCanvas() {
        if (!::mPlotPoints.isInitialized || mPlotPoints.isEmpty()) {
            return
        }

        mFileWaveViewStore.value.coroutineScope.launch {
            withContext(Dispatchers.Default) {
                val numPts = getNumPtsToPlot()
                val widthPtRatio = numPts / mPlotPoints.size
                val ptsDistance: Int = if (widthPtRatio >= 1) widthPtRatio else 1

                var currentPoint = 0

                val baseLevel = height / 2

                mBitmap =
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        val canvas = Canvas(bitmap)
                        mPlotPoints.forEach { item ->
                            canvas.drawLine(
                                currentPoint.toFloat(),
                                baseLevel.toFloat(),
                                currentPoint.toFloat(),
                                (baseLevel - item),
                                paint
                            )
                            currentPoint += ptsDistance
                        }
                    }

                mPlotPoints.dropWhile { true }
            }

            invalidate()
        }
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

        mAudioSegmentSelector?.let {
            if (xPosition >= it.left && xPosition <= it.right) {
                activateSegmentSelectorEdge(xPosition)

                return
            }
        }

        if (xPosition < getNumPtsToPlot()) {
            mFileWaveViewStore.value.setPlayHead(mAudioFileUiState.value.path, xPosition.toInt())
            vibrateDevice()
        }
    }

    private fun activateSegmentSelectorEdge(xPosition: Float) {
        mAudioSegmentSelector?.let { audioSegmentSelector ->
            audioSegmentSelector.disableAllEdges()

            val left = audioSegmentSelector.left

            val mid = left + (audioSegmentSelector.width / 2)

            if (xPosition < mid) {
                audioSegmentSelector.activateLeftEdge()
            } else {
                audioSegmentSelector.activateRightEdge()
            }

            segmentSliderActivationX = xPosition
        }
    }

    private fun resizeSegmentSelector(event: MotionEvent?) {

        event ?: return

        mAudioSegmentSelector?.let { audioSegmentSelector ->

            if (!(audioSegmentSelector.leftEdgeActivated || audioSegmentSelector.rightEdgeActivated)) return@let

            val xPos = event.x

            var viewLeft = audioSegmentSelector.left
            var viewRight = audioSegmentSelector.right

            val boundedNewPosition = xPos.toInt().coerceAtLeast(0).coerceAtMost(getNumPtsToPlot())

            if (audioSegmentSelector.leftEdgeActivated) {
                viewLeft = boundedNewPosition
            } else {
                viewRight = boundedNewPosition
            }

            if (viewRight > viewLeft) {
                mAudioFileUiState.value.run {
                    val newPositionInSamples =
                        ((boundedNewPosition.toFloat() / numPtsToPlot) * numSamples).toInt()

                    if (audioSegmentSelector.leftEdgeActivated) {
                        setSegmentStartSample(newPositionInSamples)
                    } else {
                        setSegmentEndSample(newPositionInSamples)
                    }
                }
            }

            audioSegmentSelector.disableAllEdges()
        }
    }

    private fun createAndShowSegmentSelector() {

        val audioSegmentSelector = AudioSegmentSelector(context, null)
        audioSegmentSelector.id = View.generateViewId()

        audioSegmentSelector.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        calculateSegmentSelectorProperties()

        addView(audioSegmentSelector)

        mAudioSegmentSelector = audioSegmentSelector
    }

    private fun removeSegmentSelector() {
        mAudioSegmentSelector?.let {
            removeView(it)
        }
        mAudioSegmentSelector = null
    }

    private fun calculateSegmentSelectorProperties() {
        if (!mAudioFileUiState.hasValue()) return

        return mAudioFileUiState.value.run {
            if (numPtsToPlot == 0 || numSamples == 0) return

            segmentStartSample.value.value ?: run {
                val sliderStartPosition = getSliderLeftPosition()
                setSegmentStartSample(
                    ((sliderStartPosition.toDouble() / numPtsToPlot) * numSamples).toInt()
                )
            }

            segmentEndSample.value.value ?: run {

                val initWidth = context.resources.displayMetrics.widthPixels / 10.0
                val widthInSamples = floor((initWidth / numPtsToPlot) * numSamples).toInt()

                val startSample = segmentStartSample.value.value ?: 0

                val segmentEndSampleValue = if (startSample + widthInSamples >= numSamples) {
                    numSamples - 1
                } else {
                    startSample + widthInSamples
                }

                setSegmentEndSample(segmentEndSampleValue)
            }
        }
    }

    private fun getSegmentSelectorWidth(): Int {

        mAudioFileUiState.value.run {
            val startSample = segmentStartSample.value.value ?: 0
            val endSample = segmentEndSample.value.value ?: 0

            return ceil(((endSample.toDouble() - startSample.toDouble()) / numSamples) * numPtsToPlot).toInt()
        }
    }

    private fun getSliderLeftPosition(): Int = if (mAudioFileUiState.hasValue()) {
        if (mAudioFileUiState.value.playSliderPosition.hasValue()) {
            mAudioFileUiState.value.playSliderPosition.value
        } else 0
    } else 0

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                mAudioSegmentSelector?.let {
                    if (it.leftEdgeActivated || it.rightEdgeActivated) {
                        if (segmentSliderActivationX != event.x) {
                            resizeSegmentSelector(event)
                        }
                    }
                }
            }
        }

        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (::mAudioWidgetSlider.isInitialized) {
            val sliderLeft = getSliderLeftPosition()
            val sliderTop = 0

            mAudioWidgetSlider.run {
                if (visibility != GONE && sliderLeft >= 0 && measuredWidth > 0) {
                    layout(
                        sliderLeft,
                        sliderTop,
                        sliderLeft + measuredWidth,
                        sliderTop + measuredHeight
                    )
                }
            }
        }

        mAudioSegmentSelector?.let {

            if (!mAudioFileUiState.hasValue()) return@let

            mAudioFileUiState.value.run {

                val selectorTop = 0

                if (it.visibility != GONE && it.measuredWidth > 0 && segmentSelectorLeft >= 0) {
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mFileWaveViewStore.hasValue()) {
            mFileWaveViewStore.value.updateMeasuredWidth(measuredWidth)
        }

        if (!mAudioFileUiState.hasValue()) return

        if (childCount >= 1) {
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

            val segmentSelectorWidth = getSegmentSelectorWidth()
            it.measure(
                MeasureSpec.makeMeasureSpec(
                    segmentSelectorWidth,
                    MeasureSpec.EXACTLY
                ),
                measuredHeight
            )
        }

        val calculatedWidth = mAudioFileUiState.value.numPtsToPlot

        val roundedWidth = if (measuredWidth == 0 || calculatedWidth <= measuredWidth) measuredWidth else (calculatedWidth + TAIL_WIDTH)

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

        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), clearPaint)

        mBitmap?.let { b ->
            canvas.drawBitmap(b, 0.0f, 0.0f, paint)
        }
    }
}
