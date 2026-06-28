package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.database.Contact
import com.example.ui.viewmodel.PayViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    contact: Contact,
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    val bankCards by viewModel.bankCards.collectAsState()
    val paymentResult by viewModel.paymentProcessingResult.collectAsState()

    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    
    // Bottom sheet & Payment flows
    var showPaymentDrawer by remember { mutableStateOf(false) }
    var selectedBankCard by remember { mutableStateOf<BankCard?>(null) }
    
    // PIN pad
    var showPinPad by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    
    // Simulated Processing State
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessScreen by remember { mutableStateOf(false) }
    var showErrorScreen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Initialize primary card
    LaunchedEffect(bankCards) {
        if (selectedBankCard == null && bankCards.isNotEmpty()) {
            selectedBankCard = bankCards.find { it.isPrimary } ?: bankCards.first()
        }
    }

    // Monitor payment result from ViewModel
    LaunchedEffect(paymentResult) {
        paymentResult?.let { result ->
            isProcessing = true
            delay(1500) // Realistic processing delay
            isProcessing = false
            
            when (result) {
                is PayViewModel.PaymentResult.Success -> {
                    showSuccessScreen = true
                }
                is PayViewModel.PaymentResult.Error -> {
                    errorMessage = result.message
                    showErrorScreen = true
                }
            }
            viewModel.resetPaymentResult()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = contact.upiId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("payment_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Main payment keyboard input area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Currency Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (amountStr.isEmpty()) "0" else amountStr,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 60.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (amountStr.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("payment_amount_display")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Optional payment note
                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { if (it.length <= 40) noteStr = it },
                    placeholder = { Text("What is this for?") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(260.dp)
                        .testTag("payment_note_input"),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Custom Numeric Keyboard for Amount input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    CustomPinKeyboard(
                        onKeyPressed = { char ->
                            if (char == ".") {
                                if (!amountStr.contains(".") && amountStr.isNotEmpty()) {
                                    amountStr += "."
                                }
                            } else {
                                // Cap at maximum 6 digits
                                if (amountStr.replace(".", "").length < 6) {
                                    amountStr += char
                                }
                            }
                        },
                        onBackspace = {
                            if (amountStr.isNotEmpty()) {
                                amountStr = amountStr.dropLast(1)
                            }
                        },
                        onCheck = {
                            val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                            if (parsedAmount > 0) {
                                showPaymentDrawer = true
                            }
                        },
                        checkButtonActive = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
                    )
                }
            }
        }

        // 1. Payment Drawer (Simulates Google Pay payment sheet)
        if (showPaymentDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showPaymentDrawer = false }
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}, // Prevent clicks closing sheet
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Choose Account to Pay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showPaymentDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close sheet")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Render linked bank accounts
                        bankCards.forEach { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedBankCard = card }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (card.lastFourDigits == "GPAY") Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = card.bankName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (card.lastFourDigits == "GPAY") "Wallet balance" else "${card.accountType} •••• ${card.lastFourDigits}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                RadioButton(
                                    selected = selectedBankCard?.id == card.id,
                                    onClick = { selectedBankCard = card },
                                    modifier = Modifier.testTag("account_radio_${card.lastFourDigits}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Blue "Pay" Button
                        val finalCard = selectedBankCard
                        val finalAmount = amountStr.toDoubleOrNull() ?: 0.0
                        Button(
                            onClick = {
                                showPaymentDrawer = false
                                if (finalCard != null) {
                                    // If wallet balance selected, it does not require a PIN! Directly send
                                    if (finalCard.lastFourDigits == "GPAY") {
                                        isProcessing = true
                                        viewModel.sendMoney(
                                            contact = contact,
                                            amount = finalAmount,
                                            note = noteStr,
                                            selectedBankCardId = finalCard.id,
                                            enteredPin = ""
                                        )
                                    } else {
                                        showPinPad = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("confirm_payment_button"),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "Proceed to Pay $%.2f", finalAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. Custom UPI PIN Input Sheet (Slide-up or Overlay)
        if (showPinPad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)) // Deep GPay PIN security feel
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // PIN Screen Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ENTER 4-DIGIT UPI PIN",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedBankCard?.bankName ?: "Linked Account",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = {
                            showPinPad = false
                            enteredPin = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel PIN", tint = Color.White)
                        }
                    }

                    // Large Pin indicator dots
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Paying $${amountStr} to ${contact.name}",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
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
                    }

                    // Keypad for PIN entry
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
                            },
                            onDelete = {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                }
                            },
                            onDone = {
                                if (enteredPin.length == 4) {
                                    showPinPad = false
                                    isProcessing = true
                                    selectedBankCard?.let { card ->
                                        viewModel.sendMoney(
                                            contact = contact,
                                            amount = amountStr.toDoubleOrNull() ?: 0.0,
                                            note = noteStr,
                                            selectedBankCardId = card.id,
                                            enteredPin = enteredPin
                                        )
                                    }
                                    enteredPin = ""
                                }
                            },
                            isDoneActive = enteredPin.length == 4
                        )
                    }
                }
            }
        }

        // 3. Security Loading Overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1A73E8),
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 5.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Contacting bank server...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Securing transaction with UPI shielding",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 4. Glowing Full-Bleed Success Screen
        if (showSuccessScreen) {
            SuccessAnimationScreen(
                amount = amountStr.toDoubleOrNull() ?: 0.0,
                contactName = contact.name,
                onDismiss = {
                    showSuccessScreen = false
                    viewModel.navigateBack()
                }
            )
        }

        // 5. Transaction Error Screen
        if (showErrorScreen) {
            ErrorResultScreen(
                message = errorMessage,
                onDismiss = {
                    showErrorScreen = false
                }
            )
        }
    }
}

@Composable
fun CustomPinKeyboard(
    onKeyPressed: (String) -> Unit,
    onBackspace: () -> Unit,
    onCheck: () -> Unit,
    checkButtonActive: Boolean
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "BACK")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (key == "BACK") Color.Transparent
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable {
                                when (key) {
                                    "BACK" -> onBackspace()
                                    else -> onKeyPressed(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "BACK") {
                            Icon(Icons.Default.Backspace, contentDescription = "Backspace")
                        } else {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large confirm pay FAB icon
        Button(
            onClick = onCheck,
            enabled = checkButtonActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("pay_proceed_keyboard_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A73E8),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Proceed to Pay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun CustomSecurityKeypad(
    onKeyClick: (String) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    isDoneActive: Boolean
) {
    val numKeys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BACK", "0", "DONE")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        numKeys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { k ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (k) {
                                    "DONE" -> if (isDoneActive) Color(0xFF0F9D58) else Color.White.copy(alpha = 0.05f)
                                    "BACK" -> Color.Transparent
                                    else -> Color.White.copy(alpha = 0.1f)
                                }
                            )
                            .clickable {
                                when (k) {
                                    "BACK" -> onDelete()
                                    "DONE" -> if (isDoneActive) onDone()
                                    else -> onKeyClick(k)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (k) {
                            "BACK" -> Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                            "DONE" -> Icon(Icons.Default.Check, contentDescription = "Submit", tint = if (isDoneActive) Color.White else Color.Gray)
                            else -> Text(
                                text = k,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessAnimationScreen(
    amount: Double,
    contactName: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F9D58)), // Google Pay Success Green!
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Success Animated Checkmark
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = String.format(Locale.getDefault(), "$%.2f", amount),
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sent successfully to $contactName",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scratch Card earned alert!
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You earned a Scratch Card! 🎁 Go to rewards on the dashboard to scratch and earn cashbacks.",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(54.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .testTag("success_dismiss_button")
            ) {
                Text("Done", color = Color(0xFF0F9D58), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ErrorResultScreen(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDB4437)), // Google Red
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PriorityHigh,
                    contentDescription = "Error",
                    tint = Color(0xFFDB4437),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Transaction Failed",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .testTag("error_dismiss_button")
            ) {
                Text("Try Again", color = Color(0xFFDB4437), fontWeight = FontWeight.Bold)
            }
        }
    }
}
