package com.bluehub.fastmixer.screens.mixing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluehub.fastmixer.databinding.ListItemAudioFileBinding
import kotlinx.coroutines.Deferred

class AudioFileListAdapter(private val clickListener: AudioFileEventListeners, private val fileWaveViewStore: FileWaveViewStore)
    : ListAdapter<AudioFileUiState, AudioFileListAdapter.ViewHolder>(AudioFileDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder.from(parent)
        holder.setIsRecyclable(false)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, fileWaveViewStore)
    }

    class ViewHolder private constructor(val binding: ListItemAudioFileBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFileUiState, clickListener: AudioFileEventListeners, fileWaveViewStore: FileWaveViewStore) {
            binding.audioFileUiState = item
            binding.eventListener = clickListener
            binding.fileWaveViewStore = fileWaveViewStore
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

class AudioFileDiffCallback : DiffUtil.ItemCallback<AudioFileUiState>() {
    override fun areItemsTheSame(oldItem: AudioFileUiState, newItem: AudioFileUiState): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: AudioFileUiState, newItem: AudioFileUiState): Boolean {
        return oldItem == newItem
    }
}

class AudioFileEventListeners(
    var readSamplesCallback: (String) -> (Int) -> Deferred<Array<Float>>,
    var deleteFileCallback: (String) -> Unit,
) {
    fun readSamplesCallbackWithIndex(filePath: String): (Int)->Deferred<Array<Float>> = readSamplesCallback(filePath)
}
