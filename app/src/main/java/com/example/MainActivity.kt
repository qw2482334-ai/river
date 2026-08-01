package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ExpenseTrackerApp
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions result handled dynamically by feature callers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestAppPermissions()

        setContent {
            ExpenseTrackerTheme {
                val expenseViewModel: ExpenseViewModel = viewModel()
                ExpenseTrackerApp(viewModel = expenseViewModel)
            }
        }
    }

    private fun requestAppPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
}
