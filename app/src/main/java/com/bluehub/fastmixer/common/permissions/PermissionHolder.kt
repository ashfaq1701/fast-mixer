package com.bluehub.fastmixer.common.permissions

data class PermissionHolder (val hasPermission: Boolean, val permissionCode: Int = -1, val fromCallback: Boolean = false)