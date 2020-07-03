package com.bluehub.fastmixer.screens.recording

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import javax.inject.Inject

class RecordingScreen : BaseFragment() {

    companion object {
        fun newInstance() = RecordingScreen()
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var viewModel: RecordingScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recording_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPresentationComponent().inject(this)
        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(RecordingScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

}