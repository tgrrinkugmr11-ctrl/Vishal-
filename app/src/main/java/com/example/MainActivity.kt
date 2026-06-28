package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.database.PayDatabase
import com.example.data.repository.PayRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PayViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Database and Repository
        val database = PayDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = PayRepository(
            contactDao = database.contactDao(),
            transactionDao = database.transactionDao(),
            scratchCardDao = database.scratchCardDao(),
            bankCardDao = database.bankCardDao()
        )

        // 2. Instantiate PayViewModel using custom Factory
        val factory = PayViewModel.Factory(repository)
        val viewModel = ViewModelProvider(this, factory)[PayViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Intercept System Back Button to navigate screens backward
                BackHandler(enabled = currentScreen != Screen.Home) {
                    viewModel.navigateBack()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Payment -> {
                            PaymentScreen(
                                contact = screen.contact,
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.BalanceInquiry -> {
                            BalanceScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.QRCodeScan -> {
                            QRScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.QRCodeReceive -> {
                            QRScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.ScratchCardsList -> {
                            ScratchCardsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.RechargeBills -> {
                            RechargeBillsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.TransactionHistory -> {
                            TransactionHistoryScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
