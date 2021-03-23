package com.bluehub.mixi.screens.mixing

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.bluehub.mixi.R
import com.bluehub.mixi.common.fragments.BaseFragment
import com.bluehub.mixi.databinding.PlayFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayFragment (private val audioFilePath: String, val isLoading: MutableLiveData<Boolean>) : BaseFragment<PlayViewModel>() {

    private lateinit var binding: PlayFragmentBinding

    override val viewModel: PlayViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil
            .inflate(inflater, R.layout.play_fragment, container, false)

        viewModel.setAudioFilePath(audioFilePath)

        binding.viewModel = viewModel

        setupViewViewModel()

        return binding.root
    }

    private fun setupViewViewModel() {
        viewModel.setIsLoadingLiveData(isLoading)

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it) {
                binding.playSingle.text = getString(R.string.pause_label)
                viewModel.startTrackingSeekbarTimer()
            } else {
                binding.playSingle.text = getString(R.string.play_label)
                viewModel.stopTrackingSeekbarTimer()
            }
        })

        viewModel.isGroupPlaying.observe(viewLifecycleOwner, {
            if (it) {
                binding.playAll.text = getString(R.string.pause_label)
                viewModel.startTrackingSeekbarTimer()
            } else {
                binding.playAll.text = getString(R.string.play_mixed_label)
                viewModel.stopTrackingSeekbarTimer()
            }
        })

        viewModel.seekbarMaxValue.observe(viewLifecycleOwner, {
            binding.playSeekbar.max = it
        })

        viewModel.seekbarProgress.observe(viewLifecycleOwner, {
            binding.playSeekbar.progress = it
        })

        binding.playSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (viewModel.isGroupPlaying.value == true) {
                        viewModel.setPlayerHead(progress)
                    } else {
                        viewModel.setTrackPlayHead(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (viewModel.isPlaying.value == true
                    || viewModel.isGroupPlaying.value == true) {
                    viewModel.pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (viewModel.isPlaying.value == true || viewModel.isGroupPlaying.value == true) {
                    viewModel.startPlayback()
                }
            }
        })
    }
}
