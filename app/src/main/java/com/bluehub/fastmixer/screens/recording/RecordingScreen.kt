package com.bluehub.fastmixer.screens.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import com.visualizer.amplitude.AudioRecordView
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

    private lateinit var recordingSeekbar: SeekBar

    private lateinit var audioRecordView: AudioRecordView

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

        audioRecordView = dataBinding.recordingVisualizer

        recordingSeekbar = dataBinding.recordingSeekbar

        viewModelFactory = RecordingScreenViewModelFactory(context, TAG)

        val localViewModel: RecordingScreenViewModel by viewModels { viewModelFactory }

        RecordingScreenViewModel.setInstance(localViewModel)
        viewModel = localViewModel

        RecordingScreenViewModel.setInstance(viewModel as RecordingScreenViewModel)

        dataBinding.viewModel = viewModel as RecordingScreenViewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    fun initUI() {
        val localViewModel = viewModel as RecordingScreenViewModel

        localViewModel.eventIsRecording.observe(viewLifecycleOwner, Observer { isRecording ->
            if (isRecording) {
                localViewModel.startDrawingVisualizer()
                localViewModel.startUpdatingTimer()
            } else {
                localViewModel.stopDrawingVisualizer()
                localViewModel.stopUpdatingTimer()
            }
        })

        localViewModel.eventIsPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (!isPlaying) {
                dataBinding.togglePlay.text = getString(R.string.play_label)
                localViewModel.stopTrackingSeekbar()
            } else {
                dataBinding.togglePlay.text = getString(R.string.pause_label)
                localViewModel.startTrackingSeekbar()
            }
        })

        localViewModel.eventGoBack.observe(viewLifecycleOwner, Observer { goBack ->
            if (goBack) {
                val action = RecordingScreenDirections.actionRecordingScreenToMixingScreen()
                action.recordedFilePath = localViewModel.repository.getRecordedFilePath()
                localViewModel.resetGoBack()
                findNavController().navigate(action)
            }
        })

        localViewModel.eventRecordPermission.observe(viewLifecycleOwner, Observer { record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.TOGGLE_RECORDING -> localViewModel.toggleRecording()
                    ScreenConstants.STOP_RECORDING -> localViewModel.reset()
                }
            }
        })

        localViewModel.audioVisualizerMaxAmplitude.observe(viewLifecycleOwner, Observer {
            if (localViewModel.audioVisualizerRunning.value == true) {
                audioRecordView.update(it)
            }
        })

        localViewModel.audioVisualizerRunning.observe(viewLifecycleOwner, Observer {
            audioRecordView.recreate()
        })

        localViewModel.seekbarMaxValue.observe(viewLifecycleOwner, Observer {
            recordingSeekbar.max = it
        })

        localViewModel.seekbarProgress.observe(viewLifecycleOwner, Observer {
            recordingSeekbar.progress = it
        })

        recordingSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    localViewModel.setPlayHead(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (localViewModel.eventIsPlaying.value == true) {
                    localViewModel.pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (localViewModel.eventIsPlaying.value == true) {
                    localViewModel.startPlayback()
                }
            }
        })
    }
}