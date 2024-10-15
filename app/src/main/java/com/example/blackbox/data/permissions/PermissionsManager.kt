package com.example.blackbox.data.permissions

import android.app.AppOpsManager
import android.content.Context
import android.system.Os.geteuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsManager(
    private val context: Context
) {
    data class PermissionsState(
        val hasUsageStatsPermission: Boolean = false,
    )

    private val _state = MutableStateFlow(PermissionsState())
    val state: StateFlow<PermissionsState> = _state.asStateFlow()


    suspend fun checkPermissions() {
        _state.emit(
            state.value.copy(
                hasUsageStatsPermission = hasUsageStatsPermission()
            )
        )
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