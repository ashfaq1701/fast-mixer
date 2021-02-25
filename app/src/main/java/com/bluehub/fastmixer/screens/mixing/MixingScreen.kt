package com.bluehub.fastmixer.screens.mixing

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.models.*
import com.bluehub.fastmixer.databinding.MixingScreenBinding
import com.bluehub.fastmixer.screens.mixing.MixingScreenDirections.actionMixingScreenToRecordingScreen
import com.bluehub.fastmixer.screens.mixing.modals.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MixingScreen : BaseFragment<MixingScreenViewModel>(ViewModelType.NAV_SCOPED) {
    companion object {
        fun newInstance() = MixingScreen()
    }

    override val viewModelClass = MixingScreenViewModel::class

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
        setupAnimations()

        return dataBinding.root
    }

    private fun setupViewModel() {
        // Initially close bottom drawer
        viewModel.closeBottomDrawer()

        viewModel.eventDrawerOpen.observe(viewLifecycleOwner, { isOpen ->
            TransitionManager.beginDelayedTransition(dataBinding.bottomDrawerContainer, transition)
            if (isOpen) {
                dataBinding.drawerContainer.visibility = View.VISIBLE
                dataBinding.drawerControl.setImageResource(R.drawable.drawer_control_button_close)
            } else {
                dataBinding.drawerContainer.visibility = View.GONE
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
            } else {
                dataBinding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.group_play_button)
                )
                dataBinding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.play)
                )
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
    }

    private fun setupAnimations() {
        transition = Slide()
        transition.duration = 400
        transition.addTarget(R.id.drawerContainer)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
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
