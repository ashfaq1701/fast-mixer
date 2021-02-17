package com.bluehub.fastmixer.screens.mixing

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.core.widget.doOnTextChanged
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.databinding.SegmentAdjustmentDialogBinding

class SegmentAdjustmentDialog(private val audioFileUiState: AudioFileUiState) : BaseDialogFragment<SegmentAdjustmentViewModel>() {

    private lateinit var binding: SegmentAdjustmentDialogBinding
    override val viewModelClass = SegmentAdjustmentViewModel::class

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
            binding = SegmentAdjustmentDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupView()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupView() {
        binding.segmentStart.doOnTextChanged { text, _, _, _ ->
            viewModel.setError("")
            text?.let {
                val str = it.toString()

                val numVal = if (str == "") 0 else str.toInt()

                binding.segmentStart.apply {
                    setSelection(this.length())
                }

                viewModel.setSegmentStart(numVal)
            }
        }

        binding.segmentEnd.doOnTextChanged { text, _, _, _ ->
            viewModel.setError("")
            text?.let {
                val str = it.toString()

                val numVal = if (str == "") 0 else str.toInt()

                binding.segmentEnd.apply {
                    setSelection(this.length())
                }

                viewModel.setSegmentEnd(numVal)
            }
        }

        binding.saveSegmentAdjustment.setOnClickListener {
            viewModel.saveSegmentAdjustment()
        }

        binding.cancelSegmentAdjustment.setOnClickListener {
            viewModel.cancelSegmentAdjustment()
        }
    }

    private fun setupViewModel() {
        viewModel.setAudioFileUiState(audioFileUiState)

        viewModel.closeDialog.observe(viewLifecycleOwner, {
            if (it) {
                closeDialog()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            binding.errorText.text = it
        })

        viewModel.segmentStart.observe(viewLifecycleOwner, {
            binding.segmentStart.setText(it.toString())
        })

        viewModel.segmentEnd.observe(viewLifecycleOwner, {
            binding.segmentEnd.setText(it.toString())
        })
    }
}
