package com.example.blackbox.data.manager

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.system.Os.geteuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsManager(
    private val context: Context
) {
    data class PermissionsState(
        val hasUsageStatsPermission: Boolean = false,
        val hasNotificationPermission: Boolean = false
    )

    private val _state = MutableStateFlow(PermissionsState())
    val state: StateFlow<PermissionsState> = _state.asStateFlow()


    suspend fun checkPermissions() {
        _state.emit(
            state.value.copy(
                hasUsageStatsPermission = hasUsageStatsPermission(),
                hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) hasAccess(Manifest.permission.POST_NOTIFICATIONS) else true
            )
        )
    }

    private fun hasAccess(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            geteuid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}