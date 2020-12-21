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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.file_wave_view_widget.view.*


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
        )
    ]
)
class FileWaveViewWidget(context: Context, attributeSet: AttributeSet?)
    : ConstraintLayout(context, attributeSet) {

    private var mAudioFile: BehaviorSubject<AudioFile> = BehaviorSubject.create()
    private val mAudioFileEventListeners: BehaviorSubject<AudioFileEventListeners> = BehaviorSubject.create()

    init {
        mAudioFile.subscribe { checkAndRenderView() }
        mAudioFileEventListeners.subscribe { checkAndRenderView() }
    }

    fun setAudioFile(audioFile: AudioFile) {
        mAudioFile.onNext(audioFile)
    }

    fun setAudioFileEventListeners(audioFileEventListeners: AudioFileEventListeners) {
        mAudioFileEventListeners.onNext(audioFileEventListeners)
    }

    private fun checkAndRenderView() {
        if (mAudioFile.hasValue() && mAudioFileEventListeners.hasValue()) {
            val waveViewEventListeners = FileWaveViewEventListeners(
                ::waveViewZoomIn,
                ::waveViewZoomOut,
                ::waveViewDelete
            )

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = FileWaveViewWidgetBinding.inflate(inflater, this, true)
            binding.audioFile = mAudioFile.value
            binding.eventListener = mAudioFileEventListeners.value
            binding.waveViewEventListeners = waveViewEventListeners
        }
    }

    private fun waveViewDelete() {
        mAudioFileEventListeners.value.deleteFileCallback(mAudioFile.value.path)
    }

    private fun waveViewZoomIn() {
        fileWaveView.zoomIn()
    }

    private fun waveViewZoomOut() {
        fileWaveView.zoomOut()
    }
}

class FileWaveViewEventListeners(
    val waveViewZoomIn: () -> Unit,
    val waveViewZoomOut: () -> Unit,
    val waveViewDelete: () -> Unit
)