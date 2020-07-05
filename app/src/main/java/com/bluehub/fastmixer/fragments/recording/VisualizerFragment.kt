package com.bluehub.fastmixer.fragments.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.fragments.BaseFragment

class VisualizerFragment : BaseFragment() {

    companion object {
        fun newInstance() = VisualizerFragment()
    }

    override var TAG: String = javaClass.simpleName

    private lateinit var viewModel: VisualizerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPresentationComponent().inject(this)
        viewModel = ViewModelProviders.of(this)
            .get(VisualizerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.visualizer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}