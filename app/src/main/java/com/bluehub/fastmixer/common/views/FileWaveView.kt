package com.bluehub.fastmixer.common.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getStringOrThrow
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R


/*@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "onLoadFile", method = "setOnLoadFile"),
    BindingMethod(type = FileWaveView::class, attribute = "onReadSamples", method = "setOnReadSamples")
])*/
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    filePath: String? = null
) : View(context, attrs) {
    private val mAudioFilePath: String

//    lateinit var mLoadFileCallback: (String) -> Unit
//    lateinit var mReadSamplesCallback: (Int) -> Array<Float>

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FileWaveView)

        mAudioFilePath = filePath ?: typedArray.getStringOrThrow(R.styleable.FileWaveView_audioFilePath)

        typedArray.recycle()

        //mLoadFileCallback(mAudioFilePath)
    }

    /*fun setOnLoadFile(loadFileCallback: (String) -> Unit) {
        mLoadFileCallback = loadFileCallback
    }

    fun setOnReadSamples(readSamplesCallback: (Int) -> Array<Float>) {
        mReadSamplesCallback = readSamplesCallback
    }*/

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
