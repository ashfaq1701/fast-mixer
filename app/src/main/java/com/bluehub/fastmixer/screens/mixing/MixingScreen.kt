package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.permissions.PermissionFragment
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.DialogManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.databinding.MixingScreenBinding
import timber.log.Timber
import javax.inject.Inject

class MixingScreen : PermissionFragment() {
    companion object {
        fun newInstance() = MixingScreen()
    }

    override var TAG: String = javaClass.simpleName

    override lateinit var viewModel: PermissionViewModel

    @Inject
    override lateinit var dialogManager: DialogManager

    private lateinit var viewModelFactory: MixingScreenViewModelFactory

    private lateinit var dataBinding: MixingScreenBinding

    val navArguments: MixingScreenArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPresentationComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil
            .inflate(inflater, R.layout.mixing_screen, container, false)

        viewModelFactory = MixingScreenViewModelFactory(context, TAG)

       val scopedViewModel: MixingScreenViewModel by navGraphViewModels(R.id.nav_graph) { viewModelFactory }

        viewModel = scopedViewModel

        val localViewModel = viewModel as MixingScreenViewModel
        dataBinding.mixingScreenViewModel = localViewModel

        dataBinding.lifecycleOwner = viewLifecycleOwner

        navArguments.recordedFilePath?.let {
            if (it.isNotEmpty()) localViewModel.addRecordedFilePath(it)
        }

        setPermissionEvents()
        initUI()

        return dataBinding.root
    }

    fun initUI() {
        val localViewModel = viewModel as MixingScreenViewModel

        localViewModel.eventRecord.observe(viewLifecycleOwner, Observer { record ->
            if (record) {
                findNavController().navigate(MixingScreenDirections.actionMixingScreenToRecordingScreen())
                localViewModel.onRecordNavigated()
            }
        })

        localViewModel.eventWriteFilePermission.observe(viewLifecycleOwner, Observer {record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.WRITE_TO_FILE -> localViewModel.onSaveToDisk()
                }
            }
        })

        localViewModel.eventReadFilePermission.observe(viewLifecycleOwner, Observer {record ->
            if (record.fromCallback && record.hasPermission) {
                when(record.permissionCode) {
                    ScreenConstants.READ_FROM_FILE -> localViewModel.onReadFromDisk()
                }
            }
        })
    }
}