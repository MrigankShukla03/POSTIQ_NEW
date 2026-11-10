package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.PostIQViewModel
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.PostIQTheme
import com.example.ui.theme.Slate950

class MainActivity : ComponentActivity() {

    private val viewModel: PostIQViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PostIQTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearMessage()
                    }
                }

                // Handle back press for navigation
                BackHandler(enabled = currentScreen !is Screen.Dashboard) {
                    when (currentScreen) {
                        is Screen.Report -> {
                            val rep = currentScreen as Screen.Report
                            val audit = viewModel.repository.getAuditById(rep.auditId)
                            if (audit != null && audit.results.size > 1) {
                                viewModel.navigateTo(Screen.Compare(rep.auditId))
                            } else {
                                viewModel.navigateTo(Screen.Dashboard)
                            }
                        }
                        else -> viewModel.navigateTo(Screen.Dashboard)
                    }
                }

                Scaffold(
                    containerColor = Slate950,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                            is Screen.Analyzer -> AnalyzerScreen(viewModel = viewModel)
                            is Screen.Report -> ReportViewScreen(
                                auditId = screen.auditId,
                                platform = screen.platform,
                                viewModel = viewModel
                            )
                            is Screen.Compare -> CompareViewScreen(
                                auditId = screen.auditId,
                                viewModel = viewModel
                            )
                            is Screen.Calibration -> CalibrationScreen(viewModel = viewModel)
                            is Screen.Pricing -> PricingScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
