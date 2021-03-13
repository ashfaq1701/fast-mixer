package com.bluehub.fastmixer.screens.recording

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.activity.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.databinding.RecordingScreenBinding
import com.bluehub.fastmixer.screens.recording.RecordingScreenDirections.actionRecordingScreenToMixingScreen
import com.tbruyelle.rxpermissions3.Permission
import com.tbruyelle.rxpermissions3.RxPermissions
import com.visualizer.amplitude.AudioRecordView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.recording_screen.*
import kotlinx.android.synthetic.main.view_loading.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordingScreen : BaseFragment<RecordingScreenViewModel>() {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    @Inject lateinit var rxPermission: RxPermissions

    override val viewModel: RecordingScreenViewModel by viewModels()

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

        RecordingScreenViewModel.setInstance(viewModel)
        dataBinding.viewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        setupViewModel()
        setupView()

        return dataBinding.root
    }

    fun setupViewModel() {
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

        viewModel.livePlaybackEnabled.observe(viewLifecycleOwner, {
            dataBinding.livePlaybackEnabled.isEnabled = it
        })

        viewModel.eventGoBack.observe(viewLifecycleOwner, { goBack ->
            if (goBack) {
                val action = actionRecordingScreenToMixingScreen()
                action.recordedFilePath = viewModel.recordingFilePath
                viewModel.resetGoBack()
                findNavController().navigate(action)
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

        viewModel.requestRecordingPermission.observe(viewLifecycleOwner, {
            if (it) {
                rxPermission
                    .requestEach(Manifest.permission.RECORD_AUDIO)
                    .subscribe(::handleRecordingPermission)
                    .addToDisposables()

                viewModel.resetRequestRecordingPermission()
            }
        })

        viewModel.isRecordButtonEnabled.observe(viewLifecycleOwner, {
            toggleRecord.isEnabled = it
        })

        viewModel.isPlayButtonEnabled.observe(viewLifecycleOwner, {
            togglePlay.isEnabled = it
        })

        viewModel.isPlayWithMixingTracksButtonEnabled.observe(viewLifecycleOwner, {
            togglePlayWithMixingTracks.isEnabled = it
        })

        viewModel.isPlaySeekbarEnabled.observe(viewLifecycleOwner, {
            recordingSeekbar.isEnabled = it
        })

        viewModel.isResetButtonEnabled.observe(viewLifecycleOwner, {
            reset.isEnabled = it
        })
    }

    private fun setupView() {
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

    private fun handleRecordingPermission(permission: Permission) {
        when {
            permission.granted -> {
                viewModel.setRecordingPermissionGranted()
                viewModel.toggleRecording()
            }
            permission.shouldShowRequestPermissionRationale -> {
                // Deny
                showPermissionRequiredDialog(false)
            }
            else -> {
                // Deny and never ask again
                showPermissionRequiredDialog(true)
            }
        }
    }

    private fun showPermissionRequiredDialog(showSettingsLink: Boolean) {
        val alertBuilder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_require_title)
            .setMessage(R.string.permission_recording_require_message)
            .setNegativeButton(R.string.common_close) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.cancel()
            }

        if (showSettingsLink) {
            alertBuilder.setPositiveButton(R.string.open_settings) { _, _ ->
                openAppSettingsPage()
            }
        }

        alertBuilder.setCancelable(false)
            .show()
    }

}
