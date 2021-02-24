package com.bluehub.fastmixer.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.*
import android.widget.HorizontalScrollView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.*
import com.bluehub.fastmixer.databinding.FileWaveViewWidgetBinding
import com.bluehub.fastmixer.screens.mixing.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.file_wave_view_widget.view.*
import kotlinx.android.synthetic.main.view_loading.*
import kotlinx.android.synthetic.main.view_loading.view.*
import java.util.*

@BindingMethods(
    value = [
        BindingMethod(
            type = FileWaveViewWidget::class,
            attribute = "audioFileUiState",
            method = "setAudioFileUiState"
        ),
        BindingMethod(
            type = FileWaveViewWidget::class,
            attribute = "audioFileEventListeners",
            method = "setAudioFileEventListeners"
        ),
        BindingMethod(
            type = FileWaveViewWidget::class,
            attribute = "fileWaveViewStore",
            method = "setFileWaveViewStore"
        )
    ]
)
class FileWaveViewWidget(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private var mAudioFileUiState: BehaviorSubject<AudioFileUiState> = BehaviorSubject.create()
    private val mAudioFileEventListeners: BehaviorSubject<AudioFileEventListeners> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    private lateinit var menu: PopupMenu
    private lateinit var binding: FileWaveViewWidgetBinding

    private lateinit var mFileWaveViewScroll: HorizontalScrollView
    private lateinit var mHorizontalScrollBar: CustomHorizontalScrollBar
    private lateinit var mFileWaveView: FileWaveView

    private val onMenuItemClick = { menuItem: MenuItem ->
        when(menuItem.itemId) {
            R.id.gainAdjustment -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.GAIN_ADJUSTMENT,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.segmentAdjustment -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.SEGMENT_ADJUSTMENT,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.shift -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.SHIFT,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.cut -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.CUT,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.copy -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.COPY,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.mute -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.MUTE,
                    uiState = mAudioFileUiState.value
                )
                true
            }
            R.id.paste -> {

                true
            }
            else -> false
        }
    }

    private var slideLeftTimer: Timer? = null
    private var slideRightTimer: Timer? = null

    init {
        setupObservers()
    }

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        mAudioFileUiState.onNext(audioFileUiState)
    }

    fun setAudioFileEventListeners(audioFileEventListeners: AudioFileEventListeners) {
        mAudioFileEventListeners.onNext(audioFileEventListeners)
    }

    fun setFileWaveViewStore(fileWaveViewStore: FileWaveViewStore) {
        mFileWaveViewStore.onNext(fileWaveViewStore)
    }

    private fun setupObservers() {
        mAudioFileUiState.subscribe { checkAndRenderView() }
        mAudioFileEventListeners.subscribe { checkAndRenderView() }
        mFileWaveViewStore.subscribe { checkAndRenderView() }
    }

    private fun setupStoreObservers() {
        mFileWaveViewStore.value.run {
            isPlayingObservable
                .flatMap { isPlaying ->
                    mAudioFileUiState.value.isPlaying.map { uiStateIsPlaying ->
                        isPlaying == uiStateIsPlaying
                    }
                }
                .flatMap { isPlayingEnabled ->
                    isGroupPlayingObservable.map { isGroupPlaying ->
                        isPlayingEnabled && !isGroupPlaying
                    }
                }
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe {
                    binding.wavePlayPause.isEnabled = it
                }

            isAnyPlayingObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    menu.menu.getItem(0).isEnabled = !it
                    menu.menu.getItem(2).isEnabled = !it
                }


            isPasteEnabled
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    menu.menu.getItem(6).isEnabled = it
                }
        }
    }

    private fun setupUiStateObservers() {

        mAudioFileUiState.value.run {
            isPlaying
                .subscribe {
                    if (it) {
                        binding.wavePlayPause.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.pause_button)
                        )

                    } else {
                        binding.wavePlayPause.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.play_button)
                        )
                    }
                }

            zoomLevel
                .subscribe {
                    mFileWaveViewScroll.post {
                        mFileWaveViewScroll.scrollTo(0, mFileWaveViewScroll.top)
                    }
                }

            showSegmentSelector
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it) {
                        binding.toggleSegmentSelector.backgroundTintList = AppCompatResources.getColorStateList(context, R.color.gray_400)
                    } else {
                        binding.toggleSegmentSelector.backgroundTintList = ColorStateList(arrayOf<IntArray>(), IntArray(0))
                    }
                }

            playSliderPositionMs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    binding.playHeadValue.text = it.toString()
                }

            showSegmentSelector
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    menu.menu.getItem(1).isEnabled = it
                }

            showSegmentSelector
                .flatMap { segmentSelectorShown ->
                    segmentStartSample.map { startSample ->
                        Pair(segmentSelectorShown, startSample.value)
                    }
                }
                .flatMap { dataPair ->
                    segmentEndSample.map { endSample ->
                        Triple(dataPair.first, dataPair.second, endSample.value)
                    }
                }
                .subscribe { (segmentSelectorShown, startSample, endSample) ->
                    if (segmentSelectorShown && startSample != null && endSample != null) {
                        mFileWaveViewStore.value.setSourceBounds(path)
                    } else {
                        mFileWaveViewStore.value.resetSourceBounds(path)
                    }
                }

            showSegmentSelector
                .flatMap { segmentSelectorShown ->
                    isPlaying.map { isPlayingFlag ->
                        segmentSelectorShown && !isPlayingFlag
                    }
                }
                .flatMap { result ->
                    mFileWaveViewStore.value.isGroupPlayingObservable.map { isGroupPlayingFlag ->
                        result && !isGroupPlayingFlag
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (::menu.isInitialized) {
                        menu.menu.getItem(3).isEnabled = it
                        menu.menu.getItem(5).isEnabled = it
                    }
                }

            isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it) {
                        pbLoading.visibility = View.VISIBLE
                    } else {
                        pbLoading.visibility = View.GONE
                    }
                }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkAndRenderView() {
        if (mAudioFileUiState.hasValue() && mAudioFileEventListeners.hasValue() && mFileWaveViewStore.hasValue()) {
            val waveViewEvListeners = FileWaveViewEventListeners(
                ::toggleDropUpMenu,
                ::waveViewZoomIn,
                ::waveViewZoomOut,
                ::waveViewDelete,
                ::waveViewTogglePlay,
                ::toggleSegmentSelector
            )

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = FileWaveViewWidgetBinding.inflate(inflater, this, true)

            binding.apply {
                audioFileUiState = mAudioFileUiState.value
                eventListener = mAudioFileEventListeners.value
                waveViewEventListeners = waveViewEvListeners
                fileWaveViewStore = mFileWaveViewStore.value

                fileWaveViewScrollBar.setHorizontalScrollView(fileWaveViewScroll)
                fileWaveViewScrollBar.setControlledView(fileWaveView)

                waveViewSlideLeft.setOnTouchListener { _, event ->
                    setupLeftSliderTouchEvent(event)
                    true
                }

                waveViewSlideRight.setOnTouchListener { _, event ->
                    setupRightSliderTouchEvent(event)
                    true
                }
            }

            mFileWaveViewScroll = binding.fileWaveViewScroll
            mHorizontalScrollBar = binding.fileWaveViewScrollBar
            mFileWaveView = mFileWaveViewScroll.fileWaveView

            setupStoreObservers()
            setupUiStateObservers()

            menu = PopupMenu(context, binding.dropdownMenuTrigger)
            menu.menuInflater.inflate(
                R.menu.waveview_drop_up_menu, menu.menu
            )

            menu.setOnMenuItemClickListener(onMenuItemClick)
        }
    }

    private fun waveViewTogglePlay() {
        mAudioFileEventListeners.value.togglePlayCallback(mAudioFileUiState.value.path)
        mFileWaveViewStore.value.togglePlayFlag(mAudioFileUiState.value.path)
    }

    private fun waveViewDelete() {
        mAudioFileEventListeners.value.deleteFileCallback(mAudioFileUiState.value.path)
    }

    private fun waveViewZoomIn() {
        binding.fileWaveView.zoomIn()
    }

    private fun waveViewZoomOut() {
        binding.fileWaveView.zoomOut()
    }

    private fun toggleSegmentSelector() {
        mAudioFileUiState.value.showSegmentSelector.onNext(
            !mAudioFileUiState.value.showSegmentSelector.value
        )
    }

    private fun toggleDropUpMenu() {
        menu.show()
    }

    private fun setupLeftSliderTouchEvent(event: MotionEvent) {

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> setupLeftSliderTimer()
            MotionEvent.ACTION_UP -> stopLeftSliderTimer()
        }
    }

    private fun setupRightSliderTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> setupRightSliderTimer()
            MotionEvent.ACTION_UP -> stopRightSliderTimer()
        }
    }

    private fun setupLeftSliderTimer() {
        slideLeftTimer = Timer()
        slideLeftTimer?.schedule(object: TimerTask() {
            override fun run() {
                if (!mHorizontalScrollBar.performScrollByWidthFraction(ScrollDirection.LEFT)) {
                    stopLeftSliderTimer()
                }
            }
        }, 0, 300)
    }

    private fun stopLeftSliderTimer() {
        slideLeftTimer?.let {
            it.cancel()
            slideLeftTimer = null
        }
    }

    private fun setupRightSliderTimer() {
        slideRightTimer = Timer()
        slideRightTimer?.schedule(object: TimerTask() {
            override fun run() {
                if (!mHorizontalScrollBar.performScrollByWidthFraction(ScrollDirection.RIGHT)) {
                    stopRightSliderTimer()
                }
            }
        }, 0, 300)
    }

    private fun stopRightSliderTimer() {
        slideRightTimer?.let {
            it.cancel()
            slideRightTimer = null
        }
    }
}

class FileWaveViewEventListeners(
    val toggleDropUpMenu: () -> Unit,
    val waveViewZoomIn: () -> Unit,
    val waveViewZoomOut: () -> Unit,
    val waveViewDelete: () -> Unit,
    val waveViewTogglePlay: () -> Unit,
    val toggleSegmentSelector: () -> Unit
)
