package com.bluehub.fastmixer.screens.mixing.modals

import android.app.Dialog
import android.os.Bundle
import android.view.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.databinding.GainAdjustmentDialogBinding
import com.bluehub.fastmixer.screens.mixing.PlayFragment
import com.warkiz.widget.*
import kotlinx.android.synthetic.main.view_loading.*

class GainAdjustmentDialog(private val audioFilePath: String) : BaseDialogFragment<GainAdjustmentViewModel>() {

    private lateinit var binding: GainAdjustmentDialogBinding
    override val viewModelClass = GainAdjustmentViewModel::class

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
            binding = GainAdjustmentDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupView()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playFragment = PlayFragment(audioFilePath, viewModel.isLoading)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, playFragment).commit()
    }

    private fun setupView() {

        binding.gainValuePicker.apply {
            min = -30.0f
            max = 30.0f
            onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(seekParams: SeekParams?) {}

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) { }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                    seekBar?.progress?.let {
                        viewModel.setGainValue(it)
                    }
                }
            }
        }

        binding.applyGain.setOnClickListener {
            viewModel.applyGain()
        }

        binding.saveGainApplication.setOnClickListener {
            viewModel.saveGainApplication()
        }

        binding.cancelGainApplication.setOnClickListener {
            viewModel.cancelGainApplication()
        }
    }

    private fun setupViewModel() {
        viewModel.setGainValue(0)
        viewModel.setAudioFilePath(audioFilePath)

        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.GONE
            }
        })

        viewModel.gainValue.observe(viewLifecycleOwner, {
            binding.gainValuePicker.setProgress(it.toFloat())
        })

        viewModel.closeDialog.observe(viewLifecycleOwner, {
            if (it) {
                closeDialog()
            }
        })
    }
}
