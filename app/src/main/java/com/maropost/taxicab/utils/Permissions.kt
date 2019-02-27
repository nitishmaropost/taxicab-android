package com.maropost.taxicab.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

class Permissions {

    val REQUEST_CODE_ASK_MULTIPLE_PERMISSION = 303
    lateinit var permissionInterface: PermissionInterface

    companion object {
        private var instance: Permissions? = null
        fun getInstance() : Permissions {
            if (instance == null)
                instance = Permissions()
            return instance as Permissions
        }
    }

    fun checkMultiplePermissions(context: Context,permissionInterface: PermissionInterface) {
        this.permissionInterface = permissionInterface
        val hasCallPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        + ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)
        + ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)
        + ContextCompat.checkSelfPermission(context,Manifest.permission.READ_PHONE_STATE)

        if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
            permissionInterface.requestForPermission()
        } else {
            permissionInterface.permissionAlreadyGranted()
        }
    }

    interface PermissionInterface {
        fun requestForPermission()
        fun permissionAlreadyGranted()
    }
}