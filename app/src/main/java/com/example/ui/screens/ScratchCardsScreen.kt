package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.database.ScratchCard
import com.example.ui.viewmodel.PayViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchCardsScreen(
    viewModel: PayViewModel,
    modifier: Modifier = Modifier
) {
    val scratchCards by viewModel.scratchCards.collectAsState()
    var activeScratchCard by remember { mutableStateOf<ScratchCard?>(null) }

    val totalCashback = scratchCards.filter { it.isScratched && it.rewardType == "CASHBACK" }.sumOf { it.amount }
    val unscratchedCount = scratchCards.count { !it.isScratched }

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
                title = { Text("Rewards & Scratch Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("rewards_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            // Reward Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Rewards Earned",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", totalCashback),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$unscratchedCount unscratched cards waiting!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700), // Gold
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Grid of Scratch Cards
            if (scratchCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No rewards yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Make phone recharges, pay bills, or send money to friends to win exciting scratch cards!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(scratchCards) { card ->
                        ScratchCardItem(
                            card = card,
                            onClick = {
                                if (!card.isScratched) {
                                    activeScratchCard = card
                                } else {
                                    activeScratchCard = card // Can view scratched cards too!
                                }
                            }
                        )
                    }
                }
            }
        }

        // Scratch Dialog Overlay
        activeScratchCard?.let { card ->
            ScratchActiveDialog(
                card = card,
                onScratchComplete = {
                    viewModel.scratchCard(card)
                },
                onDismiss = {
                    activeScratchCard = null
                }
            )
        }
    }
}

@Composable
fun ScratchCardItem(
    card: ScratchCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .testTag("scratch_card_item_${card.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isScratched) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else Color.Transparent
        )
    ) {
        if (!card.isScratched) {
            // Unscratched card is a beautiful golden/holographic pattern!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700), // Gold
                                Color(0xFFFFA500), // Orange
                                Color(0xFFFF8C00)  // DarkOrange
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Diagonal stripes background pattern
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 40.dp.toPx()
                    for (x in -size.width.toInt()..size.width.toInt() step step.toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat() + size.width, size.height),
                            strokeWidth = 8.dp.toPx()
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = "Unscratched Reward",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TAP TO SCRATCH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Scratched card reveals the actual reward details!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (card.rewardType == "CASHBACK") Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (card.rewardType == "CASHBACK") Icons.Default.CheckCircle else Icons.Default.LocalActivity,
                        contentDescription = null,
                        tint = if (card.rewardType == "CASHBACK") Color(0xFF2E7D32) else Color(0xFF1565C0),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (card.rewardType == "CASHBACK") String.format(Locale.getDefault(), "$%.2f", card.amount) else "Voucher",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = card.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ScratchActiveDialog(
    card: ScratchCard,
    onScratchComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var revealedPercent by remember { mutableStateOf(0f) }
    var isScratchedLocal by remember { mutableStateOf(card.isScratched) }
    val dragPoints = remember { mutableStateListOf<Offset>() }

    // Total distance of rubs to trigger reveal
    var totalRubDistance by remember { mutableStateOf(0f) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(totalRubDistance) {
        if (!isScratchedLocal && totalRubDistance > 1800f) {
            isScratchedLocal = true
            revealedPercent = 100f
            onScratchComplete()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Scratch Card",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // The Scratch Card Box Container
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Underneath layer: The actual reward!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (card.rewardType == "CASHBACK") Icons.Default.CheckCircle else Icons.Default.LocalActivity,
                            contentDescription = null,
                            tint = if (card.rewardType == "CASHBACK") Color(0xFF0F9D58) else Color(0xFF1A73E8),
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (card.rewardType == "CASHBACK") String.format(Locale.getDefault(), "$%.2f Cashback", card.amount) else "Free Promo Code",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = card.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = card.subtitle,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        if (card.code.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = card.code,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Foreground Scratchable silver coating layer
                    if (!isScratchedLocal) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            lastPoint = offset
                                            dragPoints.add(offset)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val currentPoint = change.position
                                            dragPoints.add(currentPoint)

                                            // Accumulate finger rub motion distance
                                            lastPoint?.let { lp ->
                                                val dist = abs(currentPoint.x - lp.x) + abs(currentPoint.y - lp.y)
                                                totalRubDistance += dist
                                            }
                                            lastPoint = currentPoint
                                        },
                                        onDragEnd = {
                                            lastPoint = null
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Silver coating
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E), Color(0xFF757575))
                                    )
                                )

                                // Add details to the silver surface
                                drawRect(
                                    color = Color.White.copy(alpha = 0.15f),
                                    style = Stroke(width = 8.dp.toPx())
                                )

                                // Draw erased scratching paths as holes (visual rub effect)
                                dragPoints.forEach { pt ->
                                    drawCircle(
                                        color = Color.Transparent,
                                        radius = 32.dp.toPx(),
                                        center = pt,
                                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                                    )
                                }
                            }

                            // Guide text instructions
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "RUB FINGER HERE TO SCRATCH",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                if (isScratchedLocal) {
                    Text(
                        text = "Congratulations! 🎉 Balance added to GPay wallet.",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Scratch the silver coating to reveal your reward",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(44.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
