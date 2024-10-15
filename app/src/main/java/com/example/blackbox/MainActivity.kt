package com.example.blackbox

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.blackbox.data.permissions.PermissionsManager
import com.example.blackbox.domain.repository.UserPreferencesRepository
import com.example.blackbox.domain.use_case.RecordingServiceUseCases
import com.example.blackbox.presentation.BlackboxApp
import com.example.blackbox.presentation.theme.BlackboxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var permissionsManager: PermissionsManager
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var recordingServiceUseCases: RecordingServiceUseCases

    private var isBackground: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlackboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlackboxApp()
                }
            }
        }
        lifecycleScope.launch {
            userPreferencesRepository.isBackgroundFlow.collect {
                isBackground = it
            }
        }
        lifecycleScope.launch {
            userPreferencesRepository.isAutoStartFlow.collect {
                if (it) {
                    recordingServiceUseCases.startRecordingService()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            permissionsManager.checkPermissions()
        }
    }

    override fun onDestroy() {
        if (!isBackground) {
            recordingServiceUseCases.stopRecordingService()
        }
        super.onDestroy()
    }
}