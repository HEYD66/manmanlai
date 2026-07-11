package com.slowly.manmanlai

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slowly.manmanlai.ui.ManManLaiAppRoot
import com.slowly.manmanlai.ui.ManManLaiViewModel
import com.slowly.manmanlai.ui.ManManLaiViewModelFactory
import com.slowly.manmanlai.worker.ReminderWorker

class MainActivity : ComponentActivity() {
    private var reminderTaskId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ManManLaiApp
        reminderTaskId = intent.reminderTaskId()
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
            LaunchedEffect(reminderTaskId) {
                reminderTaskId?.let(viewModel::focusTask)
                reminderTaskId = null
            }
            ManManLaiAppRoot(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        reminderTaskId = intent.reminderTaskId()
    }

    private fun Intent.reminderTaskId(): Long? =
        getLongExtra(ReminderWorker.EXTRA_TASK_ID, 0L).takeIf { it > 0L }
}
