package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getStringOrThrow
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "app:onLoadFile", method = "onLoadFile"),
    BindingMethod(type = FileWaveView::class, attribute = "app:onReadSamples", method = "onReadSamples"),
    BindingMethod(type = FileWaveView::class, attribute = "app:onDeleteFile", method = "onDeleteFile")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    filePath: String? = null
) : View(context, attrs) {
    private val mAudioFilePath: String

    lateinit var mLoadFileCallback: (String) -> Unit
    lateinit var mReadSamplesCallback: (Int) -> Array<Float>
    lateinit var mDeleteFileCallback: () -> Unit

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FileWaveView)

        mAudioFilePath = filePath ?: typedArray.getStringOrThrow(R.styleable.FileWaveView_audioFilePath)

        typedArray.recycle()

        mLoadFileCallback(mAudioFilePath)
    }

    fun onLoadFile(loadFileCallback: (String) -> Unit) {
        mLoadFileCallback = loadFileCallback
    }

    fun onReadSamples(readSamplesCallback: (Int) -> Array<Float>) {
        mReadSamplesCallback = readSamplesCallback
    }

    fun onDeleteFile(deleteFileCallback: () -> Unit) {
        mDeleteFileCallback = deleteFileCallback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
