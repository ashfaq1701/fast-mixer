package com.bluehub.fastmixer.screens.mixing

import android.app.Dialog
import android.os.Bundle
import android.view.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseDialogFragment
import com.bluehub.fastmixer.databinding.GainAdjustmentDialogBinding

class GainAdjustmentDialog(private val audioFile: AudioFile) : BaseDialogFragment<GainAdjustmentViewModel>() {

    private lateinit var binding: GainAdjustmentDialogBinding
    override val viewModelClass = GainAdjustmentViewModel::class

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { parentActivity ->

            val inflater = parentActivity.layoutInflater
            binding = GainAdjustmentDialogBinding.inflate(inflater, null, false)

            val dialog = createDialog(binding.root)

            setupViewEvents()

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playFragment = PlayFragment(audioFile)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, playFragment).commit()
    }

    private fun setupViewEvents() {
        binding.closeDialog.setOnClickListener {
            closeDialog()
        }
    }

}
