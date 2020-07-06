package com.bluehub.fastmixer.common.permissions

import com.bluehub.fastmixer.common.utils.DialogManager

interface PermissionFragmentInterface {
    var dialogManager: DialogManager

    var viewModel: PermissionViewModel

    fun setPermissionEvents() {}
}