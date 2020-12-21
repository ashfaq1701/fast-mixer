package com.bluehub.fastmixer.screens.mixing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

class AudioFileListAdapter(private val clickListener: AudioFileEventListeners): ListAdapter<AudioFile, AudioFileListAdapter.ViewHolder>(AudioFileDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder private constructor(val binding: ListItemAudioFileBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFile, clickListener: AudioFileEventListeners) {
            binding.audioFile = item
            binding.eventListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAudioFileBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class AudioFileDiffCallback : DiffUtil.ItemCallback<AudioFile>() {
    override fun areItemsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
        return oldItem == newItem
    }

}

class AudioFileEventListeners(
    var loadFileCallback: (String) -> Job,
    var readSamplesCallback: (String) -> (Int) -> Deferred<Array<Float>>,
    var deleteFileCallback: (String) -> Unit,
    var getTotalSamples: (String) -> Int
) {
    fun readSamplesCallbackWithIndex(filePath: String): (Int)->Deferred<Array<Float>> = readSamplesCallback(filePath)
    fun loadFileCallbackWithIndex(filePath: String): (Unit) -> Job = { loadFileCallback(filePath) }
    fun getTotalSamplesWithIndex(filePath: String): (Unit) -> Int = { getTotalSamples(filePath) }
}