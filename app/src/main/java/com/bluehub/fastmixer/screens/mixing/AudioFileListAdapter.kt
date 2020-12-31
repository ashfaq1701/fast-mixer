package com.bluehub.fastmixer.screens.mixing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

class AudioFileListAdapter(private val clickListener: AudioFileEventListeners, private val audioViewSampleCountStore: AudioViewSampleCountStore)
    : ListAdapter<AudioFile, AudioFileListAdapter.ViewHolder>(AudioFileDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder.from(parent)
        holder.setIsRecyclable(false)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, audioViewSampleCountStore)
    }

    class ViewHolder private constructor(val binding: ListItemAudioFileBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFile, clickListener: AudioFileEventListeners, audioViewSampleCountStore: AudioViewSampleCountStore) {
            binding.audioFile = item
            binding.eventListener = clickListener
            binding.audioViewSampleCountStore = audioViewSampleCountStore
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

    fun notifyAddItem(pos: Int) {
        notifyItemInserted(pos)
        notifyItemChanged(pos)
        notifyDataSetChanged()
    }

    fun notifyRemoveItem(pos: Int) {
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, itemCount)
        notifyDataSetChanged()
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