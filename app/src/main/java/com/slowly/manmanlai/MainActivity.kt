package com.slowly.manmanlai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slowly.manmanlai.ui.ManManLaiAppRoot
import com.slowly.manmanlai.ui.ManManLaiViewModel
import com.slowly.manmanlai.ui.ManManLaiViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ManManLaiApp
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) {}
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val viewModel: ManManLaiViewModel = viewModel(
                factory = ManManLaiViewModelFactory(app.repository, app.settings, applicationContext),
            )
            ManManLaiAppRoot(viewModel)
        }
    }
}
