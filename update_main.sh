cat << 'INNER_EOF' > app/src/main/java/com/example/MainActivity.kt
package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthViewModel
import com.example.ui.ExpenseTrackerApp
import com.example.ui.ExpenseViewModel
import com.example.ui.LoginScreen
import com.example.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
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
                val authViewModel: AuthViewModel = viewModel()
                val expenseViewModel: ExpenseViewModel = viewModel()
                var currentUserId by remember { mutableStateOf<Long?>(null) }
                
                if (currentUserId == null) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { id -> 
                            expenseViewModel.setUserId(id)
                            currentUserId = id 
                        }
                    )
                } else {
                    ExpenseTrackerApp(viewModel = expenseViewModel)
                }
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
INNER_EOF
