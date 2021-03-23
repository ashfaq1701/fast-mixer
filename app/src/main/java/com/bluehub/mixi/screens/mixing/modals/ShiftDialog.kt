package com.bluehub.mixi.screens.mixing.modals

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.bluehub.mixi.common.fragments.BaseDialogFragment
import com.bluehub.mixi.common.models.AudioFileUiState
import com.bluehub.mixi.databinding.ShiftDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_loading.*

@AndroidEntryPoint
class ShiftDialog(private val audioFileUiState: AudioFileUiState) : BaseDialogFragment<ShiftViewModel>() {

    private lateinit var binding: ShiftDialogBinding

    override val viewModel: ShiftViewModel by viewModels()

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
            binding = ShiftDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupView()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupView() {
        binding.shiftDuration.doOnTextChanged { text, _, _, _ ->
            viewModel.setError("")
            text?.let {
                val str = it.toString()

                val numVal = if (str == "") null else str.toInt()

                binding.shiftDuration.apply {
                    setSelection(this.length())
                }

                viewModel.setShiftDuration(numVal)
            }
        }

        binding.saveShift.setOnClickListener {
            viewModel.saveShift()
        }

        binding.cancelShift.setOnClickListener {
            viewModel.cancelShift()
        }
    }

    private fun setupViewModel() {
        viewModel.setAudioFileUiState(audioFileUiState)

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })

        viewModel.closeDialog.observe(viewLifecycleOwner, {
            if (it) {
                closeDialog()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            binding.errorText.text = it
        })

        viewModel.shiftDuration.observe(viewLifecycleOwner, {
            val str = it?.toString() ?: ""
            binding.shiftDuration.setText(str)
        })
    }
}
