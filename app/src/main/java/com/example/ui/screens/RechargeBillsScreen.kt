package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
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
fun RechargeBillsScreen(
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    val bankCards by viewModel.bankCards.collectAsState()
    val paymentResult by viewModel.paymentProcessingResult.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Mobile, 1 = Bills

    // Mobile recharge state variables
    var phoneNumber by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf("Verizon") }
    var selectedPlanPrice by remember { mutableStateOf(15.0) }
    var selectedPlanTitle by remember { mutableStateOf("$15.00 Basic Pack") }

    // Utility bills state variables
    var utilityType by remember { mutableStateOf("Electricity") }
    var consumerId by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }

    // Dialog & Pin Drawer integration state variables
    var showPaymentDrawer by remember { mutableStateOf(false) }
    var payAmount by remember { mutableStateOf(0.0) }
    var payRecipientName by remember { mutableStateOf("") }
    var payRecipientDetails by remember { mutableStateOf("") }
    var payType by remember { mutableStateOf("RECHARGE") } // "RECHARGE" or "BILL"

    var selectedBankCard by remember { mutableStateOf<BankCard?>(null) }
    var showPinPad by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
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

    // Monitor payment processing
    LaunchedEffect(paymentResult) {
        paymentResult?.let { result ->
            isProcessing = true
            delay(1500)
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
            TopAppBar(
                title = { Text("Bills & Recharges", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("recharge_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            // Tabs (Mobile vs Utility)
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Mobile Recharge", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("mobile_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Utility Bills", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("bills_tab")
                )
            }

            // Scrollable Forms Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (activeTab == 0) {
                    // TAB 0: Mobile Plan Recharge
                    Text(
                        text = "Enter Prepaid mobile details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10) phoneNumber = it.filter { c -> c.isDigit() } },
                        label = { Text("Mobile Number (10 digits)") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("phone_input_field")
                    )

                    Text(
                        text = "Select Mobile Operator",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Operator buttons row
                    val operators = listOf("Verizon", "AT&T", "T-Mobile", "Vodafone")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        operators.forEach { op ->
                            val isSelected = selectedOperator == op
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        2.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedOperator = op }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = op,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = "Select Talktime & Data Plan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Mobile Plan packages list
                    val plans = listOf(
                        Pair("$15.00 Basic Pack", 15.0),
                        Pair("$29.99 Premium Unlimited", 29.99),
                        Pair("$49.99 Ultra 5G Data Plan", 49.99)
                    )
                    plans.forEach { (planTitle, price) ->
                        val isSelected = selectedPlanPrice == price
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPlanPrice = price
                                    selectedPlanTitle = planTitle
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = planTitle, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Unlimited SMS + 5GB high speed data, 30 days validity",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPlanPrice = price
                                        selectedPlanTitle = planTitle
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length == 10) {
                                payAmount = selectedPlanPrice
                                payRecipientName = "$selectedOperator Recharge"
                                payRecipientDetails = phoneNumber
                                payType = "RECHARGE"
                                showPaymentDrawer = true
                            }
                        },
                        enabled = phoneNumber.length == 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("recharge_submit_button"),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "Recharge Mobile $%.2f", selectedPlanPrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                } else {
                    // TAB 1: Utility Bill Payment
                    Text(
                        text = "Pay Household Utility Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Select Utility Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Grid of utility categories
                    val bills = listOf(
                        Pair("Electricity", Icons.Default.Lightbulb),
                        Pair("Water Board", Icons.Default.WaterDrop),
                        Pair("Broadband Gas", Icons.Default.LocalGasStation),
                        Pair("Home Wifi", Icons.Default.Wifi)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bills.forEach { (type, icon) ->
                            val isSelected = utilityType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        2.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { utilityType = type }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = type.split(" ").first(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = consumerId,
                        onValueChange = { consumerId = it },
                        label = { Text("Consumer Account Number") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("consumer_id_field")
                    )

                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Billing Amount ($)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("bill_amount_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val isAmountValid = (billAmount.toDoubleOrNull() ?: 0.0) > 0.0
                    Button(
                        onClick = {
                            if (consumerId.isNotBlank() && isAmountValid) {
                                payAmount = billAmount.toDouble()
                                payRecipientName = "$utilityType Bill"
                                payRecipientDetails = "Ref: $consumerId"
                                payType = "BILL"
                                showPaymentDrawer = true
                            }
                        },
                        enabled = consumerId.isNotBlank() && isAmountValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("pay_bill_submit_button"),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "Pay %s $%.2f", utilityType, billAmount.toDoubleOrNull() ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // 1. Drawer Sheet
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
                        .clickable(enabled = false) {},
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
                                text = "Confirm Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showPaymentDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Show target recipient billing info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (payType == "RECHARGE") Icons.Default.PhoneAndroid else Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = payRecipientName, fontWeight = FontWeight.Bold)
                                    Text(text = payRecipientDetails, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render accounts list
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
                                    onClick = { selectedBankCard = card }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val finalCard = selectedBankCard
                        Button(
                            onClick = {
                                showPaymentDrawer = false
                                if (finalCard != null) {
                                    if (finalCard.lastFourDigits == "GPAY") {
                                        isProcessing = true
                                        if (payType == "RECHARGE") {
                                            viewModel.performRecharge(payRecipientDetails, payAmount, selectedOperator, finalCard.id, "")
                                        } else {
                                            viewModel.payBill(utilityType, payRecipientDetails, payAmount, finalCard.id, "")
                                        }
                                    } else {
                                        showPinPad = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("recharge_confirm_drawer_button"),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "Proceed to Pay $%.2f", payAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. PIN Pad Overlay
        if (showPinPad) {
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
                                text = "ENTER 4-DIGIT UPI PIN",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "Paying $%.2f to %s", payAmount, payRecipientName),
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
                                        if (payType == "RECHARGE") {
                                            viewModel.performRecharge(payRecipientDetails, payAmount, selectedOperator, card.id, enteredPin)
                                        } else {
                                            viewModel.payBill(utilityType, payRecipientDetails, payAmount, card.id, enteredPin)
                                        }
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
                        text = "Securing billing channel...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encrypting data with network tokenizers",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 4. Success Overlay
        if (showSuccessScreen) {
            SuccessAnimationScreen(
                amount = payAmount,
                contactName = payRecipientName,
                onDismiss = {
                    showSuccessScreen = false
                    viewModel.navigateBack()
                }
            )
        }

        // 5. Error Overlay
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
