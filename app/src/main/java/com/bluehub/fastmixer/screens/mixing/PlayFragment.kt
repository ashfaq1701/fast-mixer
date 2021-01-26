package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.databinding.PlayFragmentBinding

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

        return binding.root
    }
}
