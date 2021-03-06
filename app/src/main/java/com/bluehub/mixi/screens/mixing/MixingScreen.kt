package com.bluehub.mixi.screens.mixing

import android.Manifest
import android.app.Activity
import android.content.*
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.bluehub.mixi.R
import com.bluehub.mixi.common.di.screens.MixingScreenRxPermission
import com.bluehub.mixi.common.fragments.BaseFragment
import com.bluehub.mixi.common.models.AudioFileUiState
import com.bluehub.mixi.common.models.AudioViewActionType
import com.bluehub.mixi.databinding.MixingScreenBinding
import com.bluehub.mixi.screens.mixing.MixingScreenDirections.actionMixingScreenToRecordingScreen
import com.bluehub.mixi.screens.mixing.modals.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.tbruyelle.rxpermissions3.Permission
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.view_loading.*
import javax.inject.Inject

@AndroidEntryPoint
class MixingScreen : BaseFragment<MixingScreenViewModel>() {
    companion object {
        fun newInstance() = MixingScreen()
    }

    @MixingScreenRxPermission
    @Inject lateinit var rxPermission: RxPermissions

    override val viewModel: MixingScreenViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private lateinit var binding: MixingScreenBinding

    private var menu: Menu? = null

    private lateinit var audioFileListAdapter: AudioFileListAdapter

    private val navArguments: MixingScreenArgs by navArgs()

    private lateinit var resolver: ContentResolver

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) viewModel.addRecordedFilePath(it)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mixing_screen_menu, menu)
        this.menu = menu
        addMenuItemEnabledListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_record -> {
                viewModel.onRecord()
                true
            }
            R.id.action_read -> {
                viewModel.onReadFromDisk()
                true
            }
            R.id.action_write -> {
                viewModel.onSaveToDisk()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewModel() {
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
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_pause_30)
                )
                binding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.pause)
                )

                viewModel.startGroupPlayTimer()
            } else {
                binding.groupPlayPause.setBtnDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_play_arrow_30)
                )
                binding.groupPlayPause.setBtnLabel(
                    requireContext().getString(R.string.play)
                )

                viewModel.stopGroupPlayTimer()
            }
        })

        viewModel.groupPlayEnabled.observe(viewLifecycleOwner, {
            binding.groupPlayPause.setBtnEnabled(it)
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
            binding.apply {
                if (groupPlaySeekbar.valueFrom != it.toFloat()) {
                    groupPlaySeekbar.valueTo = it.toFloat()
                    groupPlayBoundRangeSlider.valueTo = it.toFloat()
                }

                if (!viewModel.arePlayerBoundsSet) {
                    val playBoundStart = 0.0f
                    val playBoundEnd = it.toFloat()

                    if (playBoundStart != playBoundEnd) {
                        groupPlayBoundRangeSlider.setValues(playBoundStart, playBoundEnd)
                    }
                }
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

        viewModel.eventShowUnsupportedFileToast.observe(viewLifecycleOwner, {
            if (it) {
                showUnsupportedFileTypeToast()
                viewModel.resetEventShowUnsupportedFileToast()
            }
        })

        viewModel.requestReadPermission.observe(viewLifecycleOwner, {
            if (it) {
                rxPermission
                    .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(::handleReadPermission)
                    .addToDisposables()

                viewModel.resetRequestReadPermission()
            }
        })

        viewModel.requestWritePermission.observe(viewLifecycleOwner, {
            if (it) {
                rxPermission
                    .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(::handleWritePermission)
                    .addToDisposables()

                viewModel.resetRequestWritePermission()
            }
        })
    }

    private fun setupView() {

        // Initially close bottom drawer

        bottomSheetBehavior = BottomSheetBehavior.from(binding.drawerContainer)

        binding.audioFileListView.adapter = audioFileListAdapter

        binding.audioFileListView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val itemCount = parent.adapter?.itemCount ?: return

                if (parent.getChildAdapterPosition(view) == itemCount - 1) {
                    outRect.bottom = requireContext().resources
                        .getDimension(R.dimen.mixing_list_last_item_margin_bottom)
                        .toInt()
                }
            }
        })

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

    private fun handleReadPermission(permission: Permission) {
        when {
            permission.granted -> {
                viewModel.setReadPermissionGranted()
                viewModel.addReadFile()
            }
            permission.shouldShowRequestPermissionRationale -> {
                // Deny
                showPermissionRequiredDialog(false, permission.name)
            }
            else -> {
                // Deny and never ask again
                showPermissionRequiredDialog(true, permission.name)
            }
        }
    }

    private fun handleWritePermission(permission: Permission) {
        when {
            permission.granted -> {
                viewModel.setWritePermissionGranted()
                viewModel.onSaveToDisk()
            }
            permission.shouldShowRequestPermissionRationale -> {
                // Deny
                showPermissionRequiredDialog(false, permission.name)
            }
            else -> {
                // Deny and never ask again
                showPermissionRequiredDialog(true, permission.name)
            }
        }
    }

    private fun addMenuItemEnabledListener() {
        viewModel.writeButtonEnabled.observe(viewLifecycleOwner, {
            menu?.getItem(2)?.isEnabled = it
        })

        viewModel.readButtonEnabled.observe(viewLifecycleOwner, {
            menu?.getItem(1)?.isEnabled = it
        })

        viewModel.recordButtonEnabled.observe(viewLifecycleOwner, {
            menu?.getItem(0)?.isEnabled = it
        })
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

    private fun showUnsupportedFileTypeToast() {
        val infoTxt = requireContext().getString(R.string.error_unsupported_file_type)
        Toast.makeText(requireContext(), infoTxt, Toast.LENGTH_LONG).show()
    }

    private fun showPermissionRequiredDialog(showSettingsLink: Boolean, permissionName: String) {
        val message = when (permissionName) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> R.string.permission_read_require_message
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> R.string.permission_write_require_message
            else -> return
        }

        val alertBuilder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_require_title)
            .setMessage(message)
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

private const val OPEN_FILE_REQUEST_CODE = 0x33
