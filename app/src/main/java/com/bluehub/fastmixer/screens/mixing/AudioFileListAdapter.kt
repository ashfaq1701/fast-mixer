package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class AudioFileListAdapter(context: Context, audioFileEventListeners: AudioFileEventListeners): ArrayAdapter<AudioFile>(context, -1) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //inflater.inflate()
        return super.getView(position, convertView, parent)
    }
}

class AudioFileEventListeners(
    val loadFileCallback: (String) -> Unit,
    val readSamplesCallback: (Int)->(Int)->Array<Float>,
    val deleteFileCallback: (Int)->Unit
) {
    fun readSamplesCallbackWithIndex(idx: Int): (Int)->Array<Float> = readSamplesCallback(idx)
    fun deleteFileCallbackWithIndex(idx: Int): (Int)->Unit = { deleteFileCallback(idx) }
}