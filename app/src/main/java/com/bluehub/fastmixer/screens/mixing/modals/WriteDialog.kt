package com.bluehub.fastmixer.screens.mixing.modals

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.databinding.WriteDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WriteDialog : BaseDialogFragment<WriteViewModel>() {

    private lateinit var binding: WriteDialogBinding

    override val viewModel: WriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupViewModel()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { parentActivity ->

            val inflater = parentActivity.layoutInflater
            binding = WriteDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupView()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupView() {
        binding.performWrite.setOnClickListener {
            viewModel.performWrite()
        }

        binding.cancelWrite.setOnClickListener {
            viewModel.cancelWrite()
        }
    }

    private fun setupViewModel() {
        viewModel.closeDialog.observe(viewLifecycleOwner, {
            if (it) {
                closeDialog()
            }
        })
    }
}
