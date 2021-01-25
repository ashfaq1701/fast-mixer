package com.bluehub.fastmixer.screens.mixing

import android.app.Dialog
import android.os.Bundle
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.databinding.GainAdjustmentDialogBinding

class GainAdjustmentDialog : BaseDialogFragment<GainAdjustmentViewModel>() {

    private lateinit var binding: GainAdjustmentDialogBinding
    override val viewModelClass = GainAdjustmentViewModel::class

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { parentActivity ->

            val inflater = parentActivity.layoutInflater
            binding = GainAdjustmentDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupViewEvents()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupViewEvents() {
        binding.closeDialog.setOnClickListener {
            closeDialog()
        }
    }

}
