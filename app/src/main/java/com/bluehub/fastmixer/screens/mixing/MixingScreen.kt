package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import com.bluehub.fastmixer.databinding.MixingScreenBinding
import com.bluehub.fastmixer.screens.recording.RecordingScreenViewModel
import javax.inject.Inject

class MixingScreen : BaseFragment() {
    companion object {
        fun newInstance() = MixingScreen()
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var viewModel: MixingScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPresentationComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding: MixingScreenBinding = DataBindingUtil
            .inflate(inflater, R.layout.mixing_screen, container, false)

        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(MixingScreenViewModel::class.java)

        dataBinding.mixingScreenViewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        viewModel.eventRecord.observe(viewLifecycleOwner, Observer { record ->
            if (record) {
                findNavController().navigate(MixingScreenDirections.actionMixingScreenToRecordingScreen())
                viewModel.onRecordNavigated()
            }
        })

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}