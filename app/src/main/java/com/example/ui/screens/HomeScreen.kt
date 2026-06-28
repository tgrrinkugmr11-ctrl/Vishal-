package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.Contact
import com.example.data.database.Transaction
import com.example.ui.viewmodel.PayViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val scratchCards by viewModel.scratchCards.collectAsState()
    val bankCards by viewModel.bankCards.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Filter contacts based on search query
    val filteredContacts = if (searchQuery.isEmpty()) {
        contacts
    } else {
        contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery) ||
                    it.upiId.contains(searchQuery, ignoreCase = true)
        }
    }

    // Calculated fields
    val totalCashbackWon = scratchCards.filter { it.isScratched && it.rewardType == "CASHBACK" }.sumOf { it.amount }
    val primaryCard = bankCards.find { it.isPrimary } ?: bankCards.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App Bar / Search Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customized Search Box styled like Google Search / GPay
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search friends, bills or UPI ID") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        } else {
                            IconButton(onClick = { viewModel.navigateTo(Screen.QRCodeReceive) }) {
                                Icon(Icons.Default.QrCode2, contentDescription = "My QR Code")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(56.dp)
                        .testTag("search_bar"),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar / Profile icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { viewModel.navigateTo(Screen.BalanceInquiry) }
                        .testTag("profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Settings",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Dashboard Content
            ScrollableDashboard(
                filteredContacts = filteredContacts,
                transactions = transactions.take(4),
                totalCashbackWon = totalCashbackWon,
                primaryCardName = primaryCard?.bankName ?: "Linked Account",
                primaryCardBalance = primaryCard?.balance ?: 0.0,
                onAddContactClick = { showAddContactDialog = true },
                onContactSelect = { viewModel.navigateTo(Screen.Payment(it)) },
                onScanQrClick = { viewModel.navigateTo(Screen.QRCodeScan) },
                onCheckBalanceClick = { viewModel.navigateTo(Screen.BalanceInquiry) },
                onRechargeBillsClick = { viewModel.navigateTo(Screen.RechargeBills) },
                onRewardsClick = { viewModel.navigateTo(Screen.ScratchCardsList) },
                onSeeAllHistoryClick = { viewModel.navigateTo(Screen.TransactionHistory) }
            )
        }

        // Add Contact FAB (Float over dashboard)
        FloatingActionButton(
            onClick = { showAddContactDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_contact_fab"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
        }

        // Custom Add Contact Dialog
        if (showAddContactDialog) {
            AddContactDialog(
                onDismiss = { showAddContactDialog = false },
                onConfirm = { name, phone, upi ->
                    viewModel.addContact(name, phone, upi)
                    showAddContactDialog = false
                }
            )
        }
    }
}

@Composable
fun ScrollableDashboard(
    filteredContacts: List<Contact>,
    transactions: List<Transaction>,
    totalCashbackWon: Double,
    primaryCardName: String,
    primaryCardBalance: Double,
    onAddContactClick: () -> Unit,
    onContactSelect: (Contact) -> Unit,
    onScanQrClick: () -> Unit,
    onCheckBalanceClick: () -> Unit,
    onRechargeBillsClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onSeeAllHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(bottom = 80.dp) // Avoid overlap with FAB
    ) {
        // Quick Action Grid styled like Google Pay main screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                icon = Icons.Default.QrCodeScanner,
                label = "Scan QR",
                color = Color(0xFF1A73E8),
                onClick = onScanQrClick,
                modifier = Modifier.weight(1f).testTag("quick_scan_qr")
            )
            QuickActionButton(
                icon = Icons.Default.Contacts,
                label = "Pay Contacts",
                color = Color(0xFFE91E63),
                onClick = onAddContactClick,
                modifier = Modifier.weight(1f).testTag("quick_pay_contacts")
            )
            QuickActionButton(
                icon = Icons.Default.Receipt,
                label = "Pay Bills",
                color = Color(0xFF4CAF50),
                onClick = onRechargeBillsClick,
                modifier = Modifier.weight(1f).testTag("quick_pay_bills")
            )
            QuickActionButton(
                icon = Icons.Default.AccountBalance,
                label = "Bank Transfer",
                color = Color(0xFFFF9800),
                onClick = onCheckBalanceClick,
                modifier = Modifier.weight(1f).testTag("quick_bank_transfer")
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // People / Contacts Circular Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "People",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onAddContactClick) {
                    Text("+ Add Friend")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contacts found. Add one with the '+' button!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Grid of people
                val chunkedPeople = filteredContacts.chunked(4)
                chunkedPeople.forEach { rowContacts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        rowContacts.forEach { contact ->
                            ContactGridItem(
                                contact = contact,
                                onClick = { onContactSelect(contact) },
                                modifier = Modifier
                                    .weight(1.0f)
                                    .testTag("contact_item_${contact.name.replace(" ", "_")}")
                            )
                        }
                        // Pad out remaining slots in row if not full
                        val emptySlots = 4 - rowContacts.size
                        if (emptySlots > 0) {
                            Spacer(modifier = Modifier.weight(emptySlots.toFloat()))
                        }
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Businesses & Bills Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Bills & Recharges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BillCategoryItem(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "Mobile",
                    color = Color(0xFF2196F3),
                    onClick = onRechargeBillsClick
                )
                BillCategoryItem(
                    icon = Icons.Outlined.Lightbulb,
                    label = "Electricity",
                    color = Color(0xFFFFC107),
                    onClick = onRechargeBillsClick
                )
                BillCategoryItem(
                    icon = Icons.Outlined.Tv,
                    label = "DTH",
                    color = Color(0xFFE91E63),
                    onClick = onRechargeBillsClick
                )
                BillCategoryItem(
                    icon = Icons.Outlined.LocalActivity,
                    label = "Fastag",
                    color = Color(0xFF00BCD4),
                    onClick = onRechargeBillsClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rewards visual promotion using generated img_hero_rewards asset!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onRewardsClick() }
                .testTag("rewards_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_rewards_1782495400115),
                        contentDescription = "Rewards banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Custom overlay for modern styling
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Rewards & Scratch Cards",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Cashback Won",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", totalCashbackWon),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onRewardsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Scratch Now")
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Activity / Transactions Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onSeeAllHistoryClick) {
                    Text("See All")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions. Send some money to win rewards!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        transactions.forEachIndexed { index, tx ->
                            TransactionListItem(transaction = tx)
                            if (index < transactions.size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Footnote / GPay Guarantee
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GPay Secure Payment • FDIC Insured Bank transfers",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContactGridItem(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = contact.name.split(" ").firstOrNull() ?: contact.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BillCategoryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TransactionListItem(transaction: Transaction) {
    val formattedDate = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(transaction.timestamp))
    }

    val typeLabel = when (transaction.type) {
        "SEND" -> "To ${transaction.recipientName}"
        "RECEIVE" -> "From ${transaction.senderName}"
        "BILL" -> transaction.recipientName
        "RECHARGE" -> "Mobile Top-up"
        else -> "Transaction"
    }

    val amountColor = when (transaction.type) {
        "RECEIVE" -> Color(0xFF0F9D58) // Green
        else -> MaterialTheme.colorScheme.onBackground
    }

    val iconContainerColor = when (transaction.type) {
        "RECEIVE" -> Color(0xFFE8F5E9)
        "SEND" -> Color(0xFFE3F2FD)
        "BILL" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF3E5F5)
    }

    val iconTint = when (transaction.type) {
        "RECEIVE" -> Color(0xFF2E7D32)
        "SEND" -> Color(0xFF1565C0)
        "BILL" -> Color(0xFFE65100)
        else -> Color(0xFF6A1B9A)
    }

    val icon = when (transaction.type) {
        "RECEIVE" -> Icons.Default.CallReceived
        "SEND" -> Icons.Default.CallMade
        "BILL" -> Icons.Default.ReceiptLong
        else -> Icons.Default.PhoneAndroid
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.type,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1.0f)
        ) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (transaction.note.isNotEmpty()) transaction.note else formattedDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%s$%.2f", if (transaction.type == "RECEIVE") "+" else "-", transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (transaction.status == "SUCCESS") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = transaction.status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.status == "SUCCESS") Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, upi: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = false
                        // Automatically make a nice UPI ID based on name/phone
                        val cleanName = name.replace(" ", "").lowercase()
                        if (cleanName.isNotEmpty()) {
                            upi = "$cleanName@okaxis"
                        } else if (it.isNotEmpty()) {
                            upi = "${it.filter { c -> c.isDigit() }}@okaxis"
                        }
                    },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_phone_input")
                )

                OutlinedTextField(
                    value = upi,
                    onValueChange = { upi = it },
                    label = { Text("UPI ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_upi_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) nameError = true
                    if (phone.isBlank()) phoneError = true

                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val finalUpi = if (upi.isBlank()) "${name.replace(" ", "").lowercase()}@okaxis" else upi
                        onConfirm(name, phone, finalUpi)
                    }
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_button")) {
                Text("Cancel")
            }
        }
    )
}

// Helper deleted to avoid conflict with standard painterResource
