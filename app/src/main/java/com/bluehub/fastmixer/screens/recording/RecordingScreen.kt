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
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import javax.inject.Inject

class RecordingScreen : PermissionFragment() {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    override var TAG: String = javaClass.simpleName

    private lateinit var dataBinding: RecordingScreenBinding

    @Inject
    override lateinit var dialogManager: DialogManager

    override lateinit var viewModel: PermissionViewModel
    private lateinit var viewModelFactory: RecordingScreenViewModelFactory

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

        viewModelFactory = RecordingScreenViewModelFactory(context, TAG)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RecordingScreenViewModel::class.java)

        dataBinding.viewModel = viewModel as RecordingScreenViewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    fun initUI() {

        val localViewModel = viewModel as RecordingScreenViewModel

        localViewModel.eventSetRecording.observe(viewLifecycleOwner, Observer { setRecording ->
            if (setRecording) {
                dataBinding.toggleRecord.text = getString(R.string.stop_recording_label)
            } else {
                dataBinding.toggleRecord.text = getString(R.string.start_recording_label)
            }
        })

        localViewModel.eventDoneRecording.observe(viewLifecycleOwner, Observer { doneRecording ->
            if (doneRecording) {
                findNavController().navigate(RecordingScreenDirections.actionRecordingScreenToMixingScreen())
                localViewModel.resetDoneRecording()
            }
        })
    }
}