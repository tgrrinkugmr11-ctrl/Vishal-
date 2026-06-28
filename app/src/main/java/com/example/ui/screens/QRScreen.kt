package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Contact
import com.example.ui.viewmodel.PayViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Scan, 1 = My QR

    var manualUpi by remember { mutableStateOf("") }
    var showManualUpiField by remember { mutableStateOf(false) }

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
                title = { Text(if (activeTab == 0) "Scan QR Code" else "Receive Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("qr_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            // Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Scan QR Code", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("scan_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("My QR Code", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("my_qr_tab")
                )
            }

            if (activeTab == 0) {
                // TAB 0: Scan QR Code Simulator
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF121212)) // Dark camera viewfinder background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Point camera at a Google Pay / UPI QR Code",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Viewfinder Frame with scanning light animation!
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Viewfinder Corners
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeW = 4.dp.toPx()
                                val cornerL = 32.dp.toPx()
                                val radius = 24.dp.toPx()

                                // Top Left
                                drawArc(
                                    color = Color(0xFF1A73E8),
                                    startAngle = 180f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(0f, 0f),
                                    size = Size(radius * 2, radius * 2),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(Color(0xFF1A73E8), Offset(0f, radius), Offset(0f, cornerL), strokeWidth = strokeW)
                                drawLine(Color(0xFF1A73E8), Offset(radius, 0f), Offset(cornerL, 0f), strokeWidth = strokeW)

                                // Top Right
                                drawArc(
                                    color = Color(0xFF1A73E8),
                                    startAngle = 270f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(size.width - radius * 2, 0f),
                                    size = Size(radius * 2, radius * 2),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(Color(0xFF1A73E8), Offset(size.width, radius), Offset(size.width, cornerL), strokeWidth = strokeW)
                                drawLine(Color(0xFF1A73E8), Offset(size.width - radius, 0f), Offset(size.width - cornerL, 0f), strokeWidth = strokeW)

                                // Bottom Left
                                drawArc(
                                    color = Color(0xFF1A73E8),
                                    startAngle = 90f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(0f, size.height - radius * 2),
                                    size = Size(radius * 2, radius * 2),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(Color(0xFF1A73E8), Offset(0f, size.height - radius), Offset(0f, size.height - cornerL), strokeWidth = strokeW)
                                drawLine(Color(0xFF1A73E8), Offset(radius, size.height), Offset(cornerL, size.height), strokeWidth = strokeW)

                                // Bottom Right
                                drawArc(
                                    color = Color(0xFF1A73E8),
                                    startAngle = 0f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(size.width - radius * 2, size.height - radius * 2),
                                    size = Size(radius * 2, radius * 2),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                                )
                                drawLine(Color(0xFF1A73E8), Offset(size.width, size.height - radius), Offset(size.width, size.height - cornerL), strokeWidth = strokeW)
                                drawLine(Color(0xFF1A73E8), Offset(size.width - radius, size.height), Offset(size.width - cornerL, size.height), strokeWidth = strokeW)
                            }

                            // Horizontal Laser beam animation
                            val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
                            val laserY by infiniteTransition.animateFloat(
                                initialValue = 0.05f,
                                targetValue = 0.95f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "laser_anim"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.015f)
                                    .align(Alignment.TopCenter)
                                    .offset(y = 240.dp * laserY)
                                    .background(Color(0xFF1A73E8))
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Interactive Quick Actions (Simulator & Manual Entry)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Simulated scan action: Pick random merchant contact
                                    val merchants = listOf(
                                        Contact(name = "Starbucks Cafe", phone = "+1 555-9201", upiId = "starbucks@okaxis", avatarColorHex = "#00704A"),
                                        Contact(name = "Central Cinema", phone = "+1 555-8310", upiId = "centralcine@okhdfc", avatarColorHex = "#DB4437"),
                                        Contact(name = "Amazon Web Store", phone = "+1 555-7281", upiId = "amazonpay@okicici", avatarColorHex = "#FF9900"),
                                        Contact(name = "Subway Eats", phone = "+1 555-4820", upiId = "subway@oksbi", avatarColorHex = "#008C45")
                                    )
                                    val merchant = merchants.random()
                                    viewModel.navigateTo(Screen.Payment(merchant))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("simulate_scan_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simulate Scan")
                            }

                            Button(
                                onClick = { showManualUpiField = !showManualUpiField },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("manual_upi_btn")
                            ) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enter UPI ID", color = Color.White)
                            }
                        }

                        AnimatedVisibility(visible = showManualUpiField) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OutlinedTextField(
                                    value = manualUpi,
                                    onValueChange = { manualUpi = it },
                                    label = { Text("Enter recipient UPI ID", color = Color.White) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF1A73E8),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedLabelColor = Color(0xFF1A73E8)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("manual_upi_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (manualUpi.isNotBlank()) {
                                            val name = manualUpi.split("@").firstOrNull()?.replaceFirstChar {
                                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                            } ?: "UPI Merchant"
                                            val simulatedContact = Contact(
                                                name = name,
                                                phone = "+1 555-0000",
                                                upiId = manualUpi,
                                                avatarColorHex = "#9C27B0"
                                            )
                                            viewModel.navigateTo(Screen.Payment(simulatedContact))
                                        }
                                    },
                                    enabled = manualUpi.contains("@"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().testTag("manual_upi_submit")
                                ) {
                                    Text("Pay UPI ID", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Show My Receive QR Code
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Show QR to any scanner to receive payments instantly",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // QR Card Box
                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .testTag("my_qr_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mini brand tag
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1A73E8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "GPay Secure QR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Draw a real visual high-fidelity QR Code using Compose canvas!
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .padding(8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizePx = size.width
                                    val blockCount = 21 // 21x21 QR Grid
                                    val blockSize = sizePx / blockCount

                                    // Let's seed random grid blocks to draw a stunning, realistic QR code!
                                    // Position finders (big square boxes at 3 corners)
                                    fun drawPositionFinder(x: Float, y: Float) {
                                        // Outer box
                                        drawRect(Color.Black, Offset(x, y), Size(blockSize * 7, blockSize * 7))
                                        drawRect(Color.White, Offset(x + blockSize, y + blockSize), Size(blockSize * 5, blockSize * 5))
                                        drawRect(Color.Black, Offset(x + blockSize * 2, y + blockSize * 2), Size(blockSize * 3, blockSize * 3))
                                    }

                                    // Draw position finders
                                    drawPositionFinder(0f, 0f) // Top Left
                                    drawPositionFinder(sizePx - blockSize * 7, 0f) // Top Right
                                    drawPositionFinder(0f, sizePx - blockSize * 7) // Bottom Left

                                    // Draw alignment check block
                                    drawRect(Color.Black, Offset(sizePx - blockSize * 9, sizePx - blockSize * 9), Size(blockSize * 5, blockSize * 5))
                                    drawRect(Color.White, Offset(sizePx - blockSize * 8, sizePx - blockSize * 8), Size(blockSize * 3, blockSize * 3))
                                    drawRect(Color.Black, Offset(sizePx - blockSize * 7, sizePx - blockSize * 7), Size(blockSize, blockSize))

                                    // Fill in the rest with structured random bits
                                    val rand = Random(42) // Constant seed for consistent beautiful QR
                                    for (col in 0 until blockCount) {
                                        for (row in 0 until blockCount) {
                                            // Skip position finders
                                            if ((col < 8 && row < 8) ||
                                                (col >= blockCount - 8 && row < 8) ||
                                                (col < 8 && row >= blockCount - 8)) {
                                                continue
                                            }

                                            if (rand.nextFloat() < 0.48f) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(col * blockSize, row * blockSize),
                                                    size = Size(blockSize, blockSize)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "tgrrinkugmr11@okaxis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "UPI ID Linked checking accounts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Downward sharing row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { /* Simulated clipboard share */ },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Share QR Code") }
                        )
                        AssistChip(
                            onClick = { /* Simulated copy */ },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Copy UPI ID") }
                        )
                    }
                }
            }
        }
    }
}
