package com.bluehub.fastmixer.screens.mixing.modals

import android.app.Dialog
import android.os.Bundle
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.databinding.WriteDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_loading.*
import kotlinx.android.synthetic.main.write_dialog.*

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

        binding.clearAmountIcon.setOnClickListener {
            viewModel.clearFileName()
        }

        binding.writeFileName.doOnTextChanged { text, _, _, _ ->
            text?.let { viewModel.setFileName(it.toString()) }
        }
    }

    private fun setupViewModel() {
        viewModel.closeDialog.observe(viewLifecycleOwner, {
            if (it) {
                closeDialog()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })

        viewModel.fileName.observe(viewLifecycleOwner, {
            if (it != writeFileName.text.toString()) {
                binding.writeFileName.setText(it)
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            binding.errorText.text = it
        })

        viewModel.writtenFilePath.observe(viewLifecycleOwner, { maybeFilePath ->
            maybeFilePath?.let { filePath ->

                val infoTxt = requireContext().getString(R.string.info_file_wrote_to_directory) + "\n$filePath"
                Toast.makeText(requireContext(), infoTxt, Toast.LENGTH_LONG).show()

                viewModel.resetWrittenFilePath()
                viewModel.closeDialog()
            }
        })
    }
}
