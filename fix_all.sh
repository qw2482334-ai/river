sed -i 's/viewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()/viewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),\n    onLogout: () -> Unit = {}/' app/src/main/java/com/example/ui/ExpenseTrackerApp.kt

sed -i 's/Icons.AutoMirrored.Filled.ExitToApp/Icons.Default.ExitToApp/' app/src/main/java/com/example/ui/ExpenseTrackerApp.kt
