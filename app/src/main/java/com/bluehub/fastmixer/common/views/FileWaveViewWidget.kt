package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.databinding.FileWaveViewWidgetBinding
import com.bluehub.fastmixer.screens.mixing.AudioFile
import com.bluehub.fastmixer.screens.mixing.AudioFileEventListeners
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import io.reactivex.rxjava3.subjects.BehaviorSubject


@BindingMethods(
    value = [
        BindingMethod(
            type = FileWaveViewWidget::class,
            attribute = "audioFile",
            method = "setAudioFile"
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

    private var mAudioFile: BehaviorSubject<AudioFile> = BehaviorSubject.create()
    private val mAudioFileEventListeners: BehaviorSubject<AudioFileEventListeners> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    private lateinit var binding: FileWaveViewWidgetBinding

    init {
        mAudioFile.subscribe { checkAndRenderView() }
        mAudioFileEventListeners.subscribe { checkAndRenderView() }
        mFileWaveViewStore.subscribe { checkAndRenderView() }
    }

    fun setAudioFile(audioFile: AudioFile) {
        mAudioFile.onNext(audioFile)
    }

    fun setAudioFileEventListeners(audioFileEventListeners: AudioFileEventListeners) {
        mAudioFileEventListeners.onNext(audioFileEventListeners)
    }

    fun setFileWaveViewStore(fileWaveViewStore: FileWaveViewStore) {
        mFileWaveViewStore.onNext(fileWaveViewStore)
    }

    private fun checkAndRenderView() {
        if (mAudioFile.hasValue() && mAudioFileEventListeners.hasValue() && mFileWaveViewStore.hasValue()) {
            val waveViewEventListeners = FileWaveViewEventListeners(
                ::waveViewZoomIn,
                ::waveViewZoomOut,
                ::waveViewDelete
            )

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = FileWaveViewWidgetBinding.inflate(inflater, this, true)
            binding.audioFile = mAudioFile.value
            binding.eventListener = mAudioFileEventListeners.value
            binding.waveViewEventListeners = waveViewEventListeners
            binding.fileWaveViewStore = mFileWaveViewStore.value
        }
    }

    private fun waveViewDelete() {
        mAudioFileEventListeners.value.deleteFileCallback(mAudioFile.value.path)
    }

    private fun waveViewZoomIn() {
        binding.fileWaveView.zoomIn()
    }

    private fun waveViewZoomOut() {
        binding.fileWaveView.zoomOut()
    }
}

class FileWaveViewEventListeners(
    val waveViewZoomIn: () -> Unit,
    val waveViewZoomOut: () -> Unit,
    val waveViewDelete: () -> Unit
)