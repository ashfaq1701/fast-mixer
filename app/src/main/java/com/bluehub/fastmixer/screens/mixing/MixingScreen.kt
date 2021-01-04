package com.bluehub.fastmixer.screens.mixing

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ViewModelType
import com.bluehub.fastmixer.databinding.MixingScreenBinding
import javax.inject.Inject

class MixingScreen : PermissionFragment<MixingScreenViewModel>(ViewModelType.NAV_SCOPED) {
    companion object {
        fun newInstance() = MixingScreen()
    }

    override var TAG: String = javaClass.simpleName

    override val viewModelClass = MixingScreenViewModel::class

    @Inject
    override lateinit var dialogManager: DialogManager

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

        dataBinding.mixingScreenViewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        audioFileListAdapter = AudioFileListAdapter(
            AudioFileEventListeners(
                    { filePath: String -> viewModel.readSamples(filePath) },
                    { filePath: String -> viewModel.deleteFile(filePath) }
            ),
            viewModel.fileWaveViewStore
        )
        dataBinding.audioFileListView.adapter = audioFileListAdapter

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) viewModel.addRecordedFilePath(it)
        }

        resolver = requireContext().contentResolver

        setPermissionEvents()
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
                findNavController().navigate(MixingScreenDirections.actionMixingScreenToRecordingScreen())
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
}

private const val OPEN_FILE_REQUEST_CODE = 0x33