package com.bluehub.fastmixer.screens.recording

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import com.visualizer.amplitude.AudioRecordView
import timber.log.Timber
import javax.inject.Inject


class RecordingScreen : PermissionFragment<RecordingScreenViewModel>() {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    override var TAG: String = javaClass.simpleName

    private lateinit var dataBinding: RecordingScreenBinding

    @Inject
    override lateinit var dialogManager: DialogManager

    @Inject
    override lateinit var viewModel: RecordingScreenViewModel

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

        RecordingScreenViewModel.setInstance(viewModel)
        dataBinding.viewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    fun initUI() {
        viewModel.eventIsRecording.observe(viewLifecycleOwner, Observer { isRecording ->
            if (isRecording) {
                viewModel.startDrawingVisualizer()
                viewModel.startUpdatingTimer()
            } else {
                viewModel.stopDrawingVisualizer()
                viewModel.stopUpdatingTimer()
            }
        })

        viewModel.eventIsPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (!isPlaying) {
                dataBinding.togglePlay.text = getString(R.string.play_label)
                viewModel.stopTrackingSeekbar()
            } else {
                dataBinding.togglePlay.text = getString(R.string.pause_label)
                viewModel.startTrackingSeekbar()
            }
        })

        viewModel.eventGoBack.observe(viewLifecycleOwner, Observer { goBack ->
            if (goBack) {
                val action = RecordingScreenDirections.actionRecordingScreenToMixingScreen()
                action.recordedFilePath = viewModel.repository.getRecordedFilePath()
                viewModel.resetGoBack()
                findNavController().navigate(action)
            }
        })

        viewModel.eventRecordPermission.observe(viewLifecycleOwner, Observer { record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.TOGGLE_RECORDING -> viewModel.toggleRecording()
                    ScreenConstants.STOP_RECORDING -> viewModel.reset()
                }
            }
        })

        viewModel.audioVisualizerMaxAmplitude.observe(viewLifecycleOwner, Observer {
            if (viewModel.audioVisualizerRunning.value == true) {
                audioRecordView.update(it)
            }
        })

        viewModel.audioVisualizerRunning.observe(viewLifecycleOwner, Observer {
            audioRecordView.recreate()
        })

        viewModel.seekbarMaxValue.observe(viewLifecycleOwner, Observer {
            recordingSeekbar.max = it
        })

        viewModel.seekbarProgress.observe(viewLifecycleOwner, Observer {
            recordingSeekbar.progress = it
        })

        recordingSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setPlayHead(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (viewModel.eventIsPlaying.value == true) {
                    viewModel.pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (viewModel.eventIsPlaying.value == true) {
                    viewModel.startPlayback()
                }
            }
        })
    }
}