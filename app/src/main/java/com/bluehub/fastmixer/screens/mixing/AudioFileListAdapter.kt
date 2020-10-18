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
    var loadFileCallback: (String) -> (String) -> Job,
    var readSamplesCallback: (String) -> (Int) -> Array<Float>,
    var deleteFileCallback: (String) -> Unit,
    var getTotalSamples: (String) -> Int
) {
    fun readSamplesCallbackWithIndex(uuid: String): (Int)->Array<Float> = readSamplesCallback(uuid)
    fun loadFileCallbackWithIndex(uuid: String): (String)->Job = loadFileCallback(uuid)
    fun deleteFileCallbackWithIndex(uuid: String): (String)->Unit = { deleteFileCallback(uuid) }
    fun getTotalSamplesWithIndex(uuid: String): () -> Int = { getTotalSamples(uuid) }
}