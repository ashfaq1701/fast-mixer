package com.bluehub.fastmixer.screens.mixing

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.google.android.material.slider.RangeSlider
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

    private lateinit var binding: MixingScreenBinding

    private lateinit var audioFileListAdapter: AudioFileListAdapter

    private val navArguments: MixingScreenArgs by navArgs()

    private lateinit var resolver: ContentResolver

    private lateinit var transition: Transition

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.mixing_screen, container, false)

        viewModel.resetStates()
        MixingScreenViewModel.setInstance(viewModel)
        binding.mixingScreenViewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        audioFileListAdapter = AudioFileListAdapter(
            AudioFileEventListeners(
                { filePath: String -> viewModel.readSamples(filePath) },
                { filePath: String -> viewModel.deleteFile(filePath) },
                { filePath: String -> viewModel.togglePlay(filePath) }
            ),
            viewModel.fileWaveViewStore
        )

        resolver = requireContext().contentResolver

        setupViewModel()
        setupView()
        setupAnimations()

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) viewModel.addRecordedFilePath(it)
        }

        return binding.root
    }

    private fun setupViewModel() {
        // Initially close bottom drawer
        viewModel.closeBottomDrawer()

        viewModel.eventDrawerOpen.observe(viewLifecycleOwner, { isOpen ->
            if (isOpen) {
                showBottomDrawer()
                binding.drawerControl.setImageResource(R.drawable.drawer_control_button_close)
            } else {
                hideBottomDrawer()
                binding.drawerControl.setImageResource(R.drawable.drawer_control_button_open)
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

        viewModel.itemRemovedIdx.observe(viewLifecycleOwner, {
            it?.let { removedIdx ->
                audioFileListAdapter.removeAtIndex(removedIdx)
                viewModel.resetItemRemoveIdx()
            }
        })

        viewModel.itemAddedIndex.observe(viewLifecycleOwner, {
            it?.let { addedIdx ->
                audioFileListAdapter.addAtIndex(addedIdx)
            }
        })

        viewModel.eventRead.observe(viewLifecycleOwner, {
            if (it) {
                openFilePicker()
                viewModel.resetReadFromDisk()
            }
        })

        viewModel.isGroupPlaying.observe(viewLifecycleOwner, {
            binding.groupPlayBoundRangeSlider.isEnabled = !it

            if (it) {
                binding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.group_pause_button)
                )
                binding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.pause)
                )

                viewModel.startGroupPlayTimer()
            } else {
                binding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.group_play_button)
                )
                binding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.play)
                )

                viewModel.stopGroupPlayTimer()
            }
        })

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            binding.groupPlayPause.setBtnEnabled(!it)
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
                binding.pasteAsNew.setIsEnabled(it)
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
                binding.groupPlayOverlay.visibility = View.VISIBLE
            } else {
                binding.groupPlayOverlay.visibility = View.GONE
            }
        })

        viewModel.isGroupOverlayCancelEnabled.observe(viewLifecycleOwner, {
            binding.closeGroupPlayOverlay.isEnabled = it
        })

        viewModel.groupPlaySeekbarMaxValue.observe(viewLifecycleOwner, {
            binding.groupPlaySeekbar.valueTo = it.toFloat()

            binding.groupPlayBoundRangeSlider.valueTo = it.toFloat()

            if (!viewModel.arePlayerBoundsSet) {
                binding.groupPlayBoundRangeSlider.setValues(0.0f, it.toFloat())
            }
        })

        viewModel.groupPlaySeekbarProgress.observe(viewLifecycleOwner, {
            binding.groupPlaySeekbar.value = it.toFloat()
        })

        viewModel.actionOpenWriteDialog.observe(viewLifecycleOwner, {
            if (it) {
                showWriteFileDialog()
            }
        })

        viewModel.writeButtonEnabled.observe(viewLifecycleOwner, {
            binding.writeToDisk.isEnabled = it
        })
    }

    private fun setupView() {

        binding.audioFileListView.adapter = audioFileListAdapter

        binding.groupPlaySeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
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

        binding.groupPlaySeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setPlayerHead(value.toInt())
            }
        })

        binding.groupPlayBoundRangeSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {}

            override fun onStopTrackingTouch(slider: RangeSlider) {
                val sliderBoundValues = slider.values

                if (sliderBoundValues.size == 2) {
                    viewModel.setPlayerBoundStart(sliderBoundValues[0].toInt())
                    viewModel.setPlayerBoundEnd(sliderBoundValues[1].toInt())
                }
            }
        })
    }

    private fun setupAnimations() {
        transition = Slide()
        transition.duration = 400
        transition.addTarget(R.id.drawerContainer)
    }

    private fun showBottomDrawer() {
        TransitionManager.beginDelayedTransition(binding.drawerContainer, transition)
        binding.drawerContainer.visibility = View.VISIBLE
    }

    private fun hideBottomDrawer() {
        TransitionManager.beginDelayedTransition(binding.drawerContainer, transition)
        binding.drawerContainer.visibility = View.GONE
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

    private fun showWriteFileDialog() {
        val writeDialog = WriteDialog()

        val fragmentManager = requireActivity().supportFragmentManager

        writeDialog.show(fragmentManager, "write")

        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentViewDestroyed(fm, f)

                viewModel.resetOpenWriteDialog()
                fragmentManager.unregisterFragmentLifecycleCallbacks(this)
            }
        }, false)
    }
}

private const val OPEN_FILE_REQUEST_CODE = 0x33
