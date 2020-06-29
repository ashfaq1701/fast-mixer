package com.bluehub.fastmixer.screens.mixing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.screens.common.ViewModelFactory
import com.bluehub.fastmixer.screens.common.fragments.BaseFragment
import kotlinx.android.synthetic.main.mixing_fragment.*
import javax.inject.Inject

class MixingFragment : BaseFragment() {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        fun newInstance() = MixingFragment()
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    private lateinit var viewModel: MixingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mixing_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPresentationComponent().inject(this)
        viewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(MixingViewModel::class.java)
        // TODO: Use the ViewModel
        sample_text.text = stringFromJNI()
    }

}