package com.bluehub.fastmixer.fragments.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment
import com.bluehub.fastmixer.common.viewmodel.ViewModelFactory
import javax.inject.Inject

class RecordingFragment : BaseFragment() {

    companion object {
        fun newInstance() = RecordingFragment()
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var viewModel: RecordingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recording_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPresentationComponent().inject(this)
        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(RecordingViewModel::class.java)
    }

}