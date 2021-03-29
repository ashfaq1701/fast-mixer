package com.bluehub.mixi.screens.splash

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bluehub.mixi.R
import com.bluehub.mixi.common.fragments.BaseFragment
import com.bluehub.mixi.screens.splash.SplashScreenDirections.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreen : BaseFragment<SplashViewModel>() {

    override val viewModel: SplashViewModel  by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupViewModel()
        return inflater.inflate(R.layout.splash_screen, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val window = requireActivity().window

        val insetsControllerCompat = WindowInsetsControllerCompat(window, window.decorView)
        insetsControllerCompat.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsControllerCompat.hide(systemBars())
    }

    override fun onDetach() {
        super.onDetach()

        val window = requireActivity().window

        val insetsControllerCompat = WindowInsetsControllerCompat(window, window.decorView)
        insetsControllerCompat.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsControllerCompat.show(systemBars())
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private fun setupViewModel() {
        viewModel.startSplashScreenTimer()

        viewModel.eventNavigateToMixingScreen.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(actionSplashScreenToMixingScreen())
                viewModel.resetNavigateToMixingScreen()
            }
        })
    }
}
