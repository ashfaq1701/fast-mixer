package com.bluehub.fastmixer.screens.mixing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding

class AudioFileListAdapter(context: Context, private val audioFileEventListeners: AudioFileEventListeners): ArrayAdapter<AudioFile>(context, -1) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ListItemAudioFileBinding.inflate(inflater, parent, false)
        binding.audioFile = getItem(position)
        binding.eventListener = audioFileEventListeners
        binding.index = position
        return binding.root
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