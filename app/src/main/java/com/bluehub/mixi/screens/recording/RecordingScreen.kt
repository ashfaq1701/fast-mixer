package com.bluehub.mixi.screens.recording

import android.Manifest
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.activity.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bluehub.mixi.R
import com.bluehub.mixi.common.di.screens.RecordingScreenRxPermission
import com.bluehub.mixi.common.fragments.BaseFragment
import com.bluehub.mixi.databinding.RecordingScreenBinding
import com.bluehub.mixi.screens.recording.RecordingScreenDirections.actionRecordingScreenToMixingScreen
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

    @RecordingScreenRxPermission
    @Inject lateinit var rxPermission: RxPermissions

    override val viewModel: RecordingScreenViewModel by viewModels()

    private var menu: Menu? = null

    private lateinit var binding: RecordingScreenBinding

    private lateinit var recordingSeekbar: SeekBar

    private lateinit var audioRecordView: AudioRecordView

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.setGoBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.recording_screen, container, false)

        audioRecordView = binding.recordingVisualizer

        recordingSeekbar = binding.recordingSeekbar

        RecordingScreenViewModel.setInstance(viewModel)
        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        setupViewModel()
        setupView()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recording_screen_menu, menu)
        this.menu = menu
        addMenuItemEnabledListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                viewModel.reset()
                true
            }
            R.id.action_go_back -> {
                viewModel.setGoBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewModel() {
        viewModel.eventIsRecording.observe(viewLifecycleOwner, { isRecording ->
            binding.mixingPlayEnabled.isEnabled = !isRecording
            if (isRecording) {
                binding.toggleRecord.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_stop_red_30)
                )
                viewModel.startDrawingVisualizer()
                viewModel.startUpdatingTimer()
            } else {
                binding.toggleRecord.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fiber_manual_record_red_30)
                )
                viewModel.stopDrawingVisualizer()
                viewModel.stopTrackingRecordingTimer()
            }
        })

        viewModel.eventIsPlaying.observe(viewLifecycleOwner, { isPlaying ->
            if (!isPlaying) {
                binding.togglePlay.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_play_arrow_green_30)
                )
                viewModel.stopTrackingSeekbarTimer()
            } else {
                binding.togglePlay.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_pause_blue_30)
                )
                viewModel.startTrackingSeekbar()
            }
        })

        viewModel.eventIsPlayingWithMixingTracks.observe(viewLifecycleOwner, { isPlaying ->
            if (!isPlaying) {
                binding.togglePlayWithMixingTracks.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_playlist_play_green_30)
                )
                viewModel.stopTrackingSeekbarTimer()
            } else {
                binding.togglePlayWithMixingTracks.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_pause_blue_30)
                )
                viewModel.startTrackingSeekbar()
            }
        })

        viewModel.livePlaybackEnabled.observe(viewLifecycleOwner, {
            binding.livePlaybackEnabled.isEnabled = it
        })

        viewModel.eventGoBack.observe(viewLifecycleOwner, { goBack ->
            if (goBack) {
                val action = actionRecordingScreenToMixingScreen()
                action.recordedFilePath = viewModel.recordingFilePath
                viewModel.resetGoBack()
                findNavController().navigate(action)
            }
        })

        setupVisualizerObserver()

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
    }

    private fun addMenuItemEnabledListener() {
        viewModel.isResetButtonEnabled.observe(viewLifecycleOwner, {
            menu?.getItem(0)?.isEnabled = it
        })

        viewModel.isGoBackButtonEnabled.observe(viewLifecycleOwner, {
            menu?.getItem(1)?.isEnabled = it
        })
    }

    private fun setupVisualizerObserver() {
        viewModel.audioVisualizerMaxAmplitude.removeObservers(viewLifecycleOwner)

        viewModel.audioVisualizerMaxAmplitude.observe(viewLifecycleOwner, {
            if (viewModel.audioVisualizerRunning.value == true) {
                audioRecordView.update(it)
            }
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        viewModel.audioVisualizerMaxAmplitude.removeObservers(viewLifecycleOwner)

        audioRecordView.recreate()

        setupVisualizerObserver()
    }

}
