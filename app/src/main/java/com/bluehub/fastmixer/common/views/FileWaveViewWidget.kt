package com.bluehub.fastmixer.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.screens.mixing.AudioFile
import com.bluehub.fastmixer.screens.mixing.AudioFileEventListeners
import io.reactivex.rxjava3.subjects.BehaviorSubject

@BindingMethods(value = [
    BindingMethod(type = FileWaveViewWidget::class, attribute = "audioFile", method = "setAudioFile"),
    BindingMethod(type = FileWaveViewWidget::class, attribute = "audioFileEventListeners", method = "setAudioFileEventListeners")
])
class FileWaveViewWidget(context: Context)
    : LinearLayout(context) {

    private var audioFile: BehaviorSubject<AudioFile> = BehaviorSubject.create()
    private val audioFileEventListener: BehaviorSubject<AudioFileEventListeners> = BehaviorSubject.create()
}