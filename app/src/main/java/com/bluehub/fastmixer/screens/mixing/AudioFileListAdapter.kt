package com.bluehub.fastmixer.screens.mixing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding
import kotlinx.coroutines.Job
import timber.log.Timber

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
    var readAllSamplesCallback: (String) -> Array<Float>,
    var getTotalSamples: (String) -> Int
) {
    fun readAllSamplesCallbackWithIndex(filePath: String): ()->Array<Float> = { readAllSamplesCallback(filePath) }
    fun getTotalSamplesWithIndex(uuid: String): () -> Int = { getTotalSamples(uuid) }
}