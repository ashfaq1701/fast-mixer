package com.bluehub.fastmixer.screens.mixing

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.models.AudioViewActionType
import com.bluehub.fastmixer.databinding.MixingScreenBinding
import com.bluehub.fastmixer.screens.mixing.MixingScreenDirections.actionMixingScreenToRecordingScreen
import com.bluehub.fastmixer.screens.mixing.modals.*
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.view_loading.*

@AndroidEntryPoint
class MixingScreen : BaseFragment<MixingScreenViewModel>() {
    companion object {
        fun newInstance() = MixingScreen()
    }

    override val viewModel: MixingScreenViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private lateinit var dataBinding: MixingScreenBinding

    private lateinit var audioFileListAdapter: AudioFileListAdapter

    private val navArguments: MixingScreenArgs by navArgs()

    private lateinit var resolver: ContentResolver

    private lateinit var transition: Transition

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil
            .inflate(inflater, R.layout.mixing_screen, container, false)

        viewModel.resetStates()
        MixingScreenViewModel.setInstance(viewModel)
        dataBinding.mixingScreenViewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        audioFileListAdapter = AudioFileListAdapter(
            AudioFileEventListeners(
                { filePath: String -> viewModel.readSamples(filePath) },
                { filePath: String -> viewModel.deleteFile(filePath) },
                { filePath: String -> viewModel.togglePlay(filePath) }
            ),
            viewModel.fileWaveViewStore,
        )
        dataBinding.audioFileListView.adapter = audioFileListAdapter

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) viewModel.addRecordedFilePath(it)
        }

        resolver = requireContext().contentResolver

        setupViewModel()
        setupView()
        setupAnimations()

        return dataBinding.root
    }

    private fun setupViewModel() {
        // Initially close bottom drawer
        viewModel.closeBottomDrawer()

        viewModel.eventDrawerOpen.observe(viewLifecycleOwner, { isOpen ->
            if (isOpen) {
                showBottomDrawer()
                dataBinding.drawerControl.setImageResource(R.drawable.drawer_control_button_close)
            } else {
                hideBottomDrawer()
                dataBinding.drawerControl.setImageResource(R.drawable.drawer_control_button_open)
            }
        })

        viewModel.eventRecord.observe(viewLifecycleOwner, { record ->
            if (record) {
                findNavController().navigate(actionMixingScreenToRecordingScreen())
                viewModel.onRecordNavigated()
            }
        })

        viewModel.audioFilesLiveData.observe(viewLifecycleOwner, {
            audioFileListAdapter.submitList(it)
        })

        viewModel.itemAddedIdx.observe(viewLifecycleOwner, {
            if (it != null) {
                audioFileListAdapter.notifyAddItem(it)
                viewModel.resetItemAddedIdx()
            }
        })

        viewModel.itemRemovedIdx.observe(viewLifecycleOwner, {
            if (it != null) {
                audioFileListAdapter.notifyRemoveItem(it)
                viewModel.resetItemRemovedIdx()
            }
        })

        viewModel.eventRead.observe(viewLifecycleOwner, {
            if (it) {
                openFilePicker()
                viewModel.resetReadFromDisk()
            }
        })

        viewModel.isGroupPlaying.observe(viewLifecycleOwner, {
            if (it) {
                dataBinding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.group_pause_button)
                )
                dataBinding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.pause)
                )

                viewModel.startGroupPlayTimer()
            } else {
                dataBinding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.group_play_button)
                )
                dataBinding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.play)
                )

                viewModel.stopGroupPlayTimer()
            }
        })

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            dataBinding.groupPlayPause.setBtnEnabled(!it)
        })

        viewModel.audioViewAction.observe(viewLifecycleOwner, {
            it?.let {
                when(it.actionType) {
                    AudioViewActionType.GAIN_ADJUSTMENT -> {
                        showGainControlFragment(it.uiState)
                    }
                    AudioViewActionType.SEGMENT_ADJUSTMENT -> {
                        showSegmentControlFragment(it.uiState)
                    }
                    AudioViewActionType.SHIFT -> {
                        showShiftFragment(it.uiState)
                    }
                    AudioViewActionType.CUT -> {
                        viewModel.cutToClipboard(it.uiState)
                    }
                    AudioViewActionType.COPY -> {
                        viewModel.copyToClipboard(it.uiState)
                    }
                    AudioViewActionType.MUTE -> {
                        viewModel.muteAndCopyToClipboard(it.uiState)
                    }
                    AudioViewActionType.PASTE -> {
                        viewModel.pasteFromClipboard(it.uiState)
                    }
                }
            }
        })

        viewModel.fileWaveViewStore.isPasteEnabled
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dataBinding.pasteAsNew.setIsEnabled(it)
            }

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })

        viewModel.isGroupPlayOverlayOpen.observe(viewLifecycleOwner, {
            if (it) {
                dataBinding.groupPlayOverlay.visibility = View.VISIBLE
            } else {
                dataBinding.groupPlayOverlay.visibility = View.GONE
            }
        })

        viewModel.isGroupOverlayCancelEnabled.observe(viewLifecycleOwner, {
            dataBinding.closeGroupPlayOverlay.isEnabled = it
        })

        viewModel.groupPlaySeekbarMaxValue.observe(viewLifecycleOwner, {
            dataBinding.groupPlaySeekbar.valueTo = it.toFloat()

            dataBinding.groupPlayBoundRangeSlider.valueTo = it.toFloat()
            dataBinding.groupPlayBoundRangeSlider.setValues(0.0f, it.toFloat())
        })

        viewModel.groupPlaySeekbarProgress.observe(viewLifecycleOwner, {
            dataBinding.groupPlaySeekbar.value = it.toFloat()
        })
    }

    private fun setupView() {

        dataBinding.groupPlaySeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                if (viewModel.isGroupPlaying.value == true) {
                    viewModel.pauseGroupPlay()
                }
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (viewModel.isGroupPlaying.value == true) {
                    viewModel.startGroupPlay()
                }
            }
        })

        dataBinding.groupPlaySeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setPlayerHead(value.toInt())
            }
        })
    }

    private fun setupAnimations() {
        transition = Slide()
        transition.duration = 400
        transition.addTarget(R.id.drawerContainer)
    }

    private fun showBottomDrawer() {
        TransitionManager.beginDelayedTransition(dataBinding.drawerContainer, transition)
        dataBinding.drawerContainer.visibility = View.VISIBLE
    }

    private fun hideBottomDrawer() {
        TransitionManager.beginDelayedTransition(dataBinding.drawerContainer, transition)
        dataBinding.drawerContainer.visibility = View.GONE
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            val mimeTypes = arrayOf("audio/mpeg", "audio/x-wav")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { documentUri ->
                viewModel.addReadFile(documentUri)
            }
        }
    }

    private fun showGainControlFragment(audioFile: AudioFileUiState) {
        val gainAdjustmentDialog = GainAdjustmentDialog(audioFile.path)
        gainAdjustmentDialog.show(requireActivity().supportFragmentManager, "gain_control")
    }

    private fun showSegmentControlFragment(audioFile: AudioFileUiState) {
        val segmentAdjustmentDialog = SegmentAdjustmentDialog(audioFile)
        segmentAdjustmentDialog.show(requireActivity().supportFragmentManager, "segment_control")
    }

    private fun showShiftFragment(audioFile: AudioFileUiState) {
        val shiftDialog = ShiftDialog(audioFile)
        shiftDialog.show(requireActivity().supportFragmentManager, "shift")
    }
}

private const val OPEN_FILE_REQUEST_CODE = 0x33
