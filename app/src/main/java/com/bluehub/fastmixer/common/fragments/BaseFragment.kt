package com.bluehub.fastmixer.common.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.dependencyinjection.application.ApplicationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationComponent
import com.bluehub.fastmixer.common.dependencyinjection.presentation.PresentationModule
import com.bluehub.fastmixer.common.utils.PermissionManager.AUDIO_RECORD_REQUEST
import com.bluehub.fastmixer.common.utils.PermissionManager.PERMISSIONS
import com.bluehub.fastmixer.common.utils.PermissionManager.isRequiredPermissionsGranted


abstract class BaseFragment: Fragment() {
    abstract var TAG: String

    private var mIsInjectorUsed = false

    @UiThread
    fun getPresentationComponent(): PresentationComponent {
        if (mIsInjectorUsed) {
            throw RuntimeException("there is no need to use injector more than once")
        }
        mIsInjectorUsed = true
        return getApplicationComponent()
            .newPresentationComponent(PresentationModule())
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (activity!!.getApplication() as MixerApplication).getApplicationComponent()
    }
}