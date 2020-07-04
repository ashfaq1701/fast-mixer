package com.bluehub.fastmixer.screens.recording

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.fragments.BaseScreenFragment
import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import javax.inject.Inject

class RecordingScreen : BaseScreenFragment() {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    override var TAG: String = javaClass.simpleName

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var dataBinding: RecordingScreenBinding
    private lateinit var viewModel: RecordingScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPresentationComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil
            .inflate(inflater, R.layout.recording_screen, container, false)

        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(RecordingScreenViewModel::class.java)

        dataBinding.recordingScreenViewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        viewModel.eventSetRecording.observe(viewLifecycleOwner, Observer { setRecording ->
            if (setRecording) {
                dataBinding.toggleRecord.text = getString(R.string.stop_recording_label)
            } else {
                dataBinding.toggleRecord.text = getString(R.string.start_recording_label)
            }
        })

        viewModel.eventDoneRecording.observe(viewLifecycleOwner, Observer { doneRecording ->
            if (doneRecording) {
                findNavController().navigate(RecordingScreenDirections.actionRecordingScreenToMixingScreen())
                viewModel.resetDoneRecording()
            }
        })

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun disableControls() {
        dataBinding.toggleRecord.isEnabled = false
    }

    override fun enableControls() {
        dataBinding.toggleRecord.isEnabled = true
    }
}