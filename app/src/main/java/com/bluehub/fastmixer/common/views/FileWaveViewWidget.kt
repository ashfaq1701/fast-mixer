package com.bluehub.fastmixer.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.HorizontalScrollView
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.models.*
import com.bluehub.fastmixer.databinding.FileWaveViewWidgetBinding
import com.bluehub.fastmixer.screens.mixing.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber
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

    private lateinit var fileWaveViewScroll: HorizontalScrollView
    private lateinit var horizontalScrollBar: CustomHorizontalScrollBar

    private val onMenuItemClick = { menuItem: MenuItem ->
        when(menuItem.itemId) {
            R.id.gainAdjustment -> {
                mFileWaveViewStore.value.audioViewActionLiveData.value = AudioViewAction(
                    actionType = AudioViewActionType.GAIN_ADJUSTMENT,
                    filePath = mAudioFileUiState.value.path
                )
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
        mFileWaveViewStore.value.isPlayingObservable
            .flatMap { isPlaying ->
                mAudioFileUiState.value.isPlaying.map { uiStateIsPlaying ->
                    isPlaying == uiStateIsPlaying
                }
            }
            .flatMap { isPlayingEnabled ->
                mFileWaveViewStore.value.isGroupPlayingObservable.map { isGroupPlaying ->
                    isPlayingEnabled && !isGroupPlaying
                }
            }
            .observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe {
                binding.wavePlayPause.isEnabled = it
            }

        mFileWaveViewStore.value.isPlayingObservable
            .flatMap {  isPlaying ->
                mFileWaveViewStore.value.isGroupPlayingObservable.map { isGroupPlaying ->
                    isPlaying || isGroupPlaying
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                menu.menu.getItem(0).isEnabled = !it
            }
    }

    private fun setupUiStateObservers() {
        mAudioFileUiState.value.isPlaying
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

        mAudioFileUiState.value.zoomLevel
            .subscribe {
                fileWaveViewScroll.post {
                    fileWaveViewScroll.scrollTo(0, fileWaveViewScroll.top)
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

            fileWaveViewScroll = binding.fileWaveViewScroll
            horizontalScrollBar = binding.fileWaveViewScrollBar

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
                if (!horizontalScrollBar.performScrollByWidthFraction(ScrollDirection.LEFT)) {
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
                if (!horizontalScrollBar.performScrollByWidthFraction(ScrollDirection.RIGHT)) {
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
