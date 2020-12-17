package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil
            .inflate(inflater, R.layout.mixing_screen, container, false)

        dataBinding.mixingScreenViewModel = viewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        audioFileListAdapter = AudioFileListAdapter(AudioFileEventListeners(
            { filePath: String -> viewModel.addFile(filePath) },
            { filePath: String -> viewModel.readSamples(filePath) },
            { filePath: String -> viewModel.deleteFile(filePath) },
            { filePath: String -> viewModel.getTotalSamples(filePath) }
        ))
        dataBinding.audioFileListView.adapter = audioFileListAdapter

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) viewModel.addRecordedFilePath(it)
        }

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    private fun initUI() {
        viewModel.eventRecord.observe(viewLifecycleOwner, Observer { record ->
            if (record) {
                findNavController().navigate(MixingScreenDirections.actionMixingScreenToRecordingScreen())
                viewModel.onRecordNavigated()
            }
        })

        viewModel.eventWriteFilePermission.observe(viewLifecycleOwner, Observer {record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.WRITE_TO_FILE -> viewModel.onSaveToDisk()
                }
            }
        })

        viewModel.eventReadFilePermission.observe(viewLifecycleOwner, Observer {record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.READ_FROM_FILE -> viewModel.onReadFromDisk()
                }
            }
        })

        viewModel.audioFilesLiveData.observe(viewLifecycleOwner, Observer {
            audioFileListAdapter.submitList(it)
        })
    }
}