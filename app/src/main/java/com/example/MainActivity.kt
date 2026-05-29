package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.database.AppDatabase
import com.example.data.repository.DashboardRepository
import com.example.ui.screens.MainDashboardApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.DashboardViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local Room DB and secure repository abstractions
        val database = AppDatabase.getDatabase(this)
        val repository = DashboardRepository(database.dashboardDao())

        // Create unified state ViewModel using the Factory provider contract
        val viewModel: DashboardViewModel by viewModels {
            DashboardViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                MainDashboardApp(viewModel = viewModel)
            }
        }
    }
}
