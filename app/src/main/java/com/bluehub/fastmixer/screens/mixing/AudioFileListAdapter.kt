package com.bluehub.fastmixer.screens.mixing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding

class AudioFileListAdapter(context: Context, private val audioFileEventListeners: AudioFileEventListeners, audioFileList: MutableList<AudioFile>): ArrayAdapter<AudioFile>(context, -1, audioFileList) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ListItemAudioFileBinding.inflate(inflater, parent, false)
        binding.audioFile = getItem(position)
        binding.eventListener = audioFileEventListeners
        return binding.root
    }
}

class AudioFileEventListeners(
    var readSamplesCallback: (String) -> Array<Float>,
    var getTotalSamples: (String) -> Int
) {
    fun readSamplesCallbackWithIndex(filePath: String): () -> Array<Float> = { readSamplesCallback(filePath) }
    fun getTotalSamplesWithIndex(filePath: String): () -> Int = { getTotalSamples(filePath) }
}