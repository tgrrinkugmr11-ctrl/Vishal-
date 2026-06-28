package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.BankCard
import com.example.ui.viewmodel.PayViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    val bankCards by viewModel.bankCards.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }

    // PIN Verification for Balance Check
    var verificationCard by remember { mutableStateOf<BankCard?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    // Temporarily stored revealed balances in memory
    val revealedBalances = remember { mutableStateMapOf<Int, Double>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TopAppBar(
                title = { Text("Bank Accounts & Balance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("balance_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddAccountDialog = true },
                        modifier = Modifier.testTag("add_account_button")
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = "Add Account")
                    }
                }
            )

            // Content List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro text
                item {
                    Text(
                        text = "Linked Bank Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Check real-time balances, update UPI settings, or add new savings accounts linked with your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(bankCards) { card ->
                    val isRevealed = revealedBalances.containsKey(card.id)
                    val balanceValue = revealedBalances[card.id]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_account_card_${card.lastFourDigits}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (card.isPrimary) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (card.isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (card.lastFourDigits == "GPAY") Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = card.bankName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (card.isPrimary) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "PRIMARY",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (card.lastFourDigits == "GPAY") "Wallet Account" else "${card.accountType} •••• ${card.lastFourDigits}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Secondary icon settings
                                if (card.lastFourDigits != "GPAY") {
                                    IconButton(
                                        onClick = {
                                            // Make card primary
                                            viewModel.setPrimaryCard(card.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (card.isPrimary) Icons.Default.Star else Icons.Default.StarOutline,
                                            contentDescription = "Set Primary",
                                            tint = if (card.isPrimary) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Balance reveal action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Available Balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AnimatedContent(
                                        targetState = isRevealed,
                                        transitionSpec = {
                                            slideInVertically { h -> h } + fadeIn() togetherWith
                                                    slideOutVertically { h -> -h } + fadeOut()
                                        },
                                        label = "balance_anim"
                                    ) { revealed ->
                                        if (revealed && balanceValue != null) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "$%.2f", balanceValue),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.testTag("balance_val_${card.lastFourDigits}")
                                            )
                                        } else {
                                            Text(
                                                text = "••••••",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (isRevealed) {
                                    TextButton(onClick = { revealedBalances.remove(card.id) }) {
                                        Text("Hide Balance")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            // GPay Balance/Wallet doesn't require a PIN to check balance!
                                            if (card.lastFourDigits == "GPAY") {
                                                revealedBalances[card.id] = card.balance
                                            } else {
                                                verificationCard = card
                                                showPinDialog = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.testTag("check_balance_button_${card.lastFourDigits}")
                                    ) {
                                        Text("Check Balance", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Footnote Security Guarantee
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Federal Reserve System • Encrypted vault security",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 1. PIN Check Dialog
        if (showPinDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ENTER UPI PIN",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = verificationCard?.bankName ?: "Linked Account",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = {
                            showPinDialog = false
                            enteredPin = ""
                            pinError = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel PIN", tint = Color.White)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "To view your bank account balance, please enter your 4-digit security PIN.",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            repeat(4) { idx ->
                                val active = idx < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (active) Color.White else Color.White.copy(alpha = 0.2f))
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }

                        if (pinError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = pinError,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CustomSecurityKeypad(
                            onKeyClick = { num ->
                                if (enteredPin.length < 4) {
                                    enteredPin += num
                                }
                                pinError = ""
                            },
                            onDelete = {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                }
                                pinError = ""
                            },
                            onDone = {
                                val targetCard = verificationCard
                                if (enteredPin.length == 4 && targetCard != null) {
                                    if (targetCard.upiPin == enteredPin) {
                                        // PIN correct! Reveal balance.
                                        revealedBalances[targetCard.id] = targetCard.balance
                                        showPinDialog = false
                                        enteredPin = ""
                                        pinError = ""
                                        verificationCard = null
                                    } else {
                                        pinError = "Incorrect PIN. Please try again."
                                        enteredPin = ""
                                    }
                                }
                            },
                            isDoneActive = enteredPin.length == 4
                        )
                    }
                }
            }
        }

        // 2. Add Account Dialog
        if (showAddAccountDialog) {
            AddBankDialog(
                onDismiss = { showAddAccountDialog = false },
                onConfirm = { bankName, type, lastFour, balance, pin ->
                    val card = BankCard(
                        bankName = bankName,
                        accountType = type,
                        lastFourDigits = lastFour,
                        balance = balance,
                        isPrimary = false,
                        upiPin = pin
                    )
                    viewModel.addBankCard(card)
                    showAddAccountDialog = false
                }
            )
        }
    }
}

@Composable
fun AddBankDialog(
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, accountType: String, lastFourDigits: String, balance: Double, pin: String) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Savings") }
    var lastFour by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var upiPin by remember { mutableStateOf("1234") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Bank Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    placeholder = { Text("e.g. Bank of America") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_bank_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Savings", "Checking").forEach { type ->
                        val isSelected = accountType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { accountType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { if (it.length <= 4) lastFour = it.filter { c -> c.isDigit() } },
                    label = { Text("Last 4 Digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_bank_last_four")
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Initial Balance ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_bank_balance")
                )

                OutlinedTextField(
                    value = upiPin,
                    onValueChange = { if (it.length <= 4) upiPin = it.filter { c -> c.isDigit() } },
                    label = { Text("Set 4-Digit UPI PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_bank_pin")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balance.toDoubleOrNull() ?: 500.0
                    val l4 = if (lastFour.length == 4) lastFour else "0000"
                    val finalPin = if (upiPin.length == 4) upiPin else "1234"
                    if (bankName.isNotBlank()) {
                        onConfirm(bankName, accountType, l4, bal, finalPin)
                    }
                },
                modifier = Modifier.testTag("dialog_bank_confirm")
            ) {
                Text("Link Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_bank_cancel")) {
                Text("Cancel")
            }
        }
    )
}
