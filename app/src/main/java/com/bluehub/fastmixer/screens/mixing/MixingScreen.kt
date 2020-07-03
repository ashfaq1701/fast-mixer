package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import javax.inject.Inject

class MixingScreen : BaseFragment() {
    companion object {
        fun newInstance() = MixingScreen()
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var viewModel: MixingScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mixing_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPresentationComponent().inject(this)
        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(MixingScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

}