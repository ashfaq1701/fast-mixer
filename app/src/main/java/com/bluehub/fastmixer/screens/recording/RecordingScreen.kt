package com.bluehub.fastmixer.screens.recording

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.activity.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionControlFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.common.models.ViewModelType
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import com.bluehub.fastmixer.screens.recording.RecordingScreenDirections.actionRecordingScreenToMixingScreen
import com.visualizer.amplitude.AudioRecordView
import kotlinx.android.synthetic.main.view_loading.*
import javax.inject.Inject


class RecordingScreen : PermissionControlFragment<RecordingScreenControlViewModel>(ViewModelType.FRAGMENT_SCOPED) {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    override val viewModelClass = RecordingScreenControlViewModel::class

    @Inject
    override lateinit var dialogManager: DialogManager

    private lateinit var dataBinding: RecordingScreenBinding

    private lateinit var recordingSeekbar: SeekBar

    private lateinit var audioRecordView: AudioRecordView

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.setGoBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil
            .inflate(inflater, R.layout.recording_screen, container, false)

        audioRecordView = dataBinding.recordingVisualizer

        recordingSeekbar = dataBinding.recordingSeekbar

        RecordingScreenControlViewModel.setInstance(viewModel)
        dataBinding.viewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    fun initUI() {
        viewModel.eventIsRecording.observe(viewLifecycleOwner, { isRecording ->
            dataBinding.mixingPlayEnabled.isEnabled = !isRecording
            if (isRecording) {
                viewModel.startDrawingVisualizer()
                viewModel.startUpdatingTimer()
            } else {
                viewModel.stopDrawingVisualizer()
                viewModel.stopTrackingRecordingTimer()
            }
        })

        viewModel.eventIsPlaying.observe(viewLifecycleOwner, { isPlaying ->
            if (!isPlaying) {
                dataBinding.togglePlay.text = getString(R.string.play_label)
                viewModel.stopTrackingSeekbarTimer()
            } else {
                dataBinding.togglePlay.text = getString(R.string.pause_label)
                viewModel.startTrackingSeekbar()
            }
        })

        viewModel.eventIsPlayingWithMixingTracks.observe(viewLifecycleOwner, { isPlaying ->
            if (!isPlaying) {
                dataBinding.togglePlayWithMixingTracks.text = getString(R.string.play_mixed_label)
                viewModel.stopTrackingSeekbarTimer()
            } else {
                dataBinding.togglePlayWithMixingTracks.text = getString(R.string.pause_label)
                viewModel.startTrackingSeekbar()
            }
        })

        viewModel.eventGoBack.observe(viewLifecycleOwner, { goBack ->
            if (goBack) {
                val action = actionRecordingScreenToMixingScreen()
                action.recordedFilePath = viewModel.repository.getRecordedFilePath()
                viewModel.resetGoBack()
                findNavController().navigate(action)
            }
        })

        viewModel.eventRecordPermission.observe(viewLifecycleOwner, { record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.TOGGLE_RECORDING -> viewModel.toggleRecording()
                    ScreenConstants.STOP_RECORDING -> viewModel.reset()
                }
            }
        })

        viewModel.audioVisualizerMaxAmplitude.observe(viewLifecycleOwner, {
            if (viewModel.audioVisualizerRunning.value == true) {
                audioRecordView.update(it)
            }
        })

        viewModel.audioVisualizerRunning.observe(viewLifecycleOwner, {
            audioRecordView.recreate()
        })

        viewModel.seekbarMaxValue.observe(viewLifecycleOwner, {
            recordingSeekbar.max = it
        })

        viewModel.seekbarProgress.observe(viewLifecycleOwner, {
            recordingSeekbar.progress = it
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })

        recordingSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setPlayHead(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (viewModel.eventIsPlaying.value == true
                    || viewModel.eventIsPlayingWithMixingTracks.value == true) {
                    viewModel.pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (viewModel.eventIsPlaying.value == true) {
                    viewModel.startPlayback()
                } else if (viewModel.eventIsPlayingWithMixingTracks.value == true) {
                    viewModel.startPlaybackWithMixingTracks()
                }
            }
        })
    }
}
