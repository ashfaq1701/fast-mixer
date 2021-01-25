package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
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
    }

    private fun checkAndRenderView() {
        if (mAudioFileUiState.hasValue() && mAudioFileEventListeners.hasValue() && mFileWaveViewStore.hasValue()) {
            val waveViewEventListeners = FileWaveViewEventListeners(
                ::toggleDropUpMenu,
                ::waveViewZoomIn,
                ::waveViewZoomOut,
                ::waveViewDelete,
                ::waveViewTogglePlay,
            )

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = FileWaveViewWidgetBinding.inflate(inflater, this, true)
            binding.audioFileUiState = mAudioFileUiState.value
            binding.eventListener = mAudioFileEventListeners.value
            binding.waveViewEventListeners = waveViewEventListeners
            binding.fileWaveViewStore = mFileWaveViewStore.value

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

    private fun toggleDropUpMenu() {
        menu.show()
    }
}

class FileWaveViewEventListeners(
    val toggleDropUpMenu: () -> Unit,
    val waveViewZoomIn: () -> Unit,
    val waveViewZoomOut: () -> Unit,
    val waveViewDelete: () -> Unit,
    val waveViewTogglePlay: () -> Unit
)
