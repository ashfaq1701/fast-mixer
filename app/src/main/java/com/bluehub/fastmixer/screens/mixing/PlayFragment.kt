package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.databinding.PlayFragmentBinding
import kotlinx.android.synthetic.main.view_loading.*

class PlayFragment(private val audioFile: AudioFile) : BaseFragment<PlayViewModel>() {

    private lateinit var binding: PlayFragmentBinding
    override val viewModelClass = PlayViewModel::class

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil
            .inflate(inflater, R.layout.play_fragment, container, false)

        viewModel.selectedAudioFile = audioFile

        setupViewViewModel()

        return binding.root
    }

    private fun setupViewViewModel() {

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it) {
                binding.playSingle.text = getString(R.string.pause_label)
            } else {
                binding.playSingle.text = getString(R.string.play_label)
            }
        })

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it) {
                binding.playAll.text = getString(R.string.pause_label)
            } else {
                binding.playAll.text = getString(R.string.play_mixed_label)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })
    }
}
