package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.HistoryLog
import com.example.data.model.TelegramChannel
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboardApp(viewModel: DashboardViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = DeepMidnight,
        bottomBar = {
            GlowingBottomBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowPurple.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(200f, 300f),
                        radius = 800f
                    )
                )
        ) {
            // --- Custom High-Tech Header ---
            TopDashboardHeader(profile = profile)

            // --- Panel Switcher ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { if (it > targetState) -it else it } togetherWith
                                fadeOut() + slideOutHorizontally { if (it > targetState) it else -it }
                    },
                    label = "TabContent"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> PortalPanel(viewModel = viewModel)
                        1 -> TelegramPanel(viewModel = viewModel)
                        2 -> AIHubPanel(viewModel = viewModel)
                        3 -> CreativePanel(viewModel = viewModel)
                        4 -> ProfilePanel(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. HOME PORTAL / DASHBOARD SCREEN
// ==========================================

@Composable
fun PortalPanel(viewModel: DashboardViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val logs by viewModel.historyLogs.collectAsStateWithLifecycle()
    val channels by viewModel.telegramChannels.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardAccent, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DarkCardAccent)
                                .border(2.dp, NeonMagenta, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAvatarIcon(profile?.avatarIdentifier ?: ""),
                                contentDescription = "Active Avatar",
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "স্বাগতম, ${profile?.name ?: "ইউজার"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = profile?.bio ?: "সবগুলো এআই ফিচার একের মাঝে",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = DarkCardAccent)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লাইভ স্ট্যাটাস:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonAmber
                        )
                        Text(
                            text = profile?.status ?: "অল-ইন-ওয়ান ড্যাশবোর্ড একটিভ",
                            fontSize = 12.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "টেলিগ্রাম হোস্ট",
                    value = "${channels.size} টি চ্যানেল",
                    tint = NeonCyan,
                    icon = Icons.Default.Campaign
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "মোট জেনারেশনস",
                    value = "${logs.size} টি অপারেশন",
                    tint = NeonMagenta,
                    icon = Icons.Default.AutoAwesome
                )
            }
        }

        // Live Clock bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardAccent)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Active Node",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সার্ভার ক্লায়েন্ট নোড একটিভ",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "UTC: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber
                )
            }
        }

        // Operational shortcuts
        item {
            Text(
                text = "এআই কন্ট্রোল প্যানেল",
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ControlPortalRow(
                    title = "টেলিগ্রাম ম্যাসেঞ্ছার ব্রডকাস্ট",
                    desc = "হাতে থাকা চ্যানেলগুলোতে ম্যাসেজ এবং ছবি ব্রডকাস্ট করুন।",
                    icon = Icons.Default.Send,
                    tint = NeonCyan,
                    onClick = { viewModel.selectTab(1) }
                )
                ControlPortalRow(
                    title = "সোশ্যাল মিডিয়া ভাইরাল টেক্সট ক্রিয়েটর",
                    desc = "YouTube, Facebook, TikTok শিরোনাম ও হ্যাসট্যাগ জেনারেট করুন।",
                    icon = Icons.Default.Hub,
                    tint = NeonMagenta,
                    onClick = { viewModel.selectTab(2) }
                )
                ControlPortalRow(
                    title = "রিয়েলটাইম ল্যাঙ্গুয়েজ ট্রান্সলেটর",
                    desc = "যেকোনো ভাষাকে মুহূর্তের মধ্যে অনুবাদ করুন।",
                    icon = Icons.Default.Language,
                    tint = NeonAmber,
                    onClick = { viewModel.selectTab(2) }
                )
                ControlPortalRow(
                    title = "এআই মিউজিক কম্পোজার ও সং মেকার",
                    desc = "অ্যাকোস্টিক লিরিক ক্রিয়েট করে প্রসিডুরাল সিন্থ শুনুন।",
                    icon = Icons.Default.MusicNote,
                    tint = GlowPurple,
                    onClick = { viewModel.selectTab(3) } // Tabs: Song maker and vision are combined in Tab 3 + Hub
                )
            }
        }

        // Operations History Timeline
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "কার্যক্রমের হিস্ট্রি লগ",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (logs.isNotEmpty()) {
                    Text(
                        text = "মুছে ফেলুন",
                        fontSize = 11.sp,
                        color = NeonMagenta,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showClearConfirm = true }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark Icon",
                                tint = TextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোন হিস্ট্রি রেকর্ড পাওয়া যায়নি\nআজই প্রথম অপারেশন দিয়ে শুরু করুন।",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(logs) { log ->
                HistoryRow(
                    log = log,
                    onCopy = {
                        clipboard.setText(AnnotatedString(log.generatedOutput))
                        Toast.makeText(context, "অনুলিপি ক্লায়েন্ট নোডে সংরক্ষিত!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = DarkCard,
            title = { Text("হিস্ট্রি ক্লিয়ার করুন", color = TextPrimary) },
            text = { Text("আপনি কি নিশ্চিত আপনার সমস্ত কার্যক্রমের লগ ইতিহাসের ডেটা ডাটাবেস থেকে মুছে দিতে চান?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("হ্যাঁ, ডিলিট করুন", color = NeonMagenta)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("বাতিল", color = TextPrimary)
                }
            }
        )
    }
}

// ==========================================
// 2. TELEGRAM DASHBOARD & BROADCASTER SCREEN
// ==========================================

@Composable
fun TelegramPanel(viewModel: DashboardViewModel) {
    val channels by viewModel.telegramChannels.collectAsStateWithLifecycle()
    val isPosting by viewModel.isPosting.collectAsStateWithLifecycle()
    val postStatus by viewModel.telegramPostState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var activeChannelIndex by remember { mutableIntStateOf(0) }

    // Forms
    var showAddDialog by remember { mutableStateOf(false) }
    var channelNameInput by remember { mutableStateOf("") }
    var channelIdentifierInput by remember { mutableStateOf("") }
    var botTokenInput by remember { mutableStateOf("") }

    var broadcastText by remember { mutableStateOf("") }
    var imageLinkInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Broadcast layout
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = GlowPurple.copy(alpha = 0.5f),
                            size = size,
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2f)
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "টেলিগ্রাম ব্রডকাস্টার",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "হাতে থাকা চ্যানেলগুলোতে মুহূর্তের মধ্যে পোস্ট পাঠান",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Campaign Icon",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Divider(color = DarkCardAccent)

                    // Channel Selector
                    if (channels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardAccent)
                                .clickable { showAddDialog = true }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add icon",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "কোন টেলিগ্রাম চ্যানেল যুক্ত করা নেই\nযুক্ত করতে চাপুন",
                                    fontSize = 12.sp,
                                    color = NeonCyan,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "ব্রডকাস্ট পার্সোনাল চ্যানেল সিলেক্ট করুন:",
                            fontSize = 11.sp,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )

                        // Selector loop
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(channels.size) { idx ->
                                val channel = channels[idx]
                                val selected = activeChannelIndex == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(if (selected) NeonCyan else DarkCardAccent)
                                        .clickable { activeChannelIndex = idx }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (selected) DeepMidnight else NeonMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = channel.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) DeepMidnight else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Text Field Message Content
                        OutlinedTextField(
                            value = broadcastText,
                            onValueChange = { broadcastText = it },
                            label = { Text("ম্যাসেজ বা বার্তার কন্টেন্ট লিখুন...", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("telegram_message_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkCardAccent,
                                focusedContainerColor = DarkCardAccent,
                                unfocusedContainerColor = DarkCardAccent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Photo link input
                        OutlinedTextField(
                            value = imageLinkInput,
                            onValueChange = { imageLinkInput = it },
                            label = { Text("ছবি অ্যাটাচমেন্ট লিংক (ঐচ্ছিক)", color = TextSecondary) },
                            singleLine = true,
                            placeholder = { Text("যেমন: https://your-server/photo.jpg", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("telegram_photo_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkCardAccent,
                                focusedContainerColor = DarkCardAccent,
                                unfocusedContainerColor = DarkCardAccent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Posting states
                        if (!postStatus.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GlowPurple.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isPosting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = NeonCyan,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Text(
                                        text = postStatus ?: "",
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Dismiss",
                                        tint = TextSecondary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.clearPostStatus() }
                                    )
                                }
                            }
                        }

                        // Send Button
                        Button(
                            onClick = {
                                if (broadcastText.trim().isEmpty() && imageLinkInput.trim().isEmpty()) {
                                    Toast.makeText(context, "অনুগ্রহ করে বার্তা অথবা ছবির লিংক পূরণ করুন", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val selectedChannel = channels.getOrNull(activeChannelIndex)
                                if (selectedChannel != null) {
                                    viewModel.broadcastToTelegram(
                                        channel = selectedChannel,
                                        text = broadcastText,
                                        photoUrl = imageLinkInput.trim().ifEmpty { null }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("telegram_broadcast_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isPosting
                        ) {
                            Text(
                                text = "টেলিগ্রামে ব্রডকাস্ট পাঠান",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Active integrated Channels list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সংযুক্ত টেলিগ্রাম চ্যানেলসমূহ (${channels.size})",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "নতুন চ্যানেল",
                        fontSize = 11.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (channels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোন টেলিগ্রাম চ্যানেল সেটআপ করা নেই। যুক্ত করতে ওপরের বাটনটি ব্যবহার করুন।",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(channels) { channel ->
                TelegramChannelRow(
                    channel = channel,
                    onDelete = { viewModel.removeTelegramChannel(channel) }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkCard,
            title = {
                Text(
                    text = "টেলিগ্রাম চ্যানেল যুক্ত করুন",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ইনস্ট্রাকশন: নতুন চ্যানেল বটের মাধ্যমে কন্ট্রোল করতে প্রথমে টেলিগ্রাম থেকে একটি বট বানিয়ে নিন (@BotFather ব্যবহার করে) এবং বটটিকে আপনার চ্যানেলে এডমিন বানান। এরপর বটের Token এবং চ্যানেলের Username (@সহ) বা ID লিখুন।",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                    OutlinedTextField(
                        value = channelNameInput,
                        onValueChange = { channelNameInput = it },
                        label = { Text("চ্যানেলের নাম (যেমন: অফিশিয়াল গ্রুপ)", fontSize = 11.sp, color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardAccent
                        )
                    )
                    OutlinedTextField(
                        value = channelIdentifierInput,
                        onValueChange = { channelIdentifierInput = it },
                        label = { Text("চ্যানেলের ID/Username (যেমন: @my_news_bd)", fontSize = 11.sp, color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardAccent
                        )
                    )
                    OutlinedTextField(
                        value = botTokenInput,
                        onValueChange = { botTokenInput = it },
                        label = { Text("টেলিগ্রাম বট টোকেন (বটফাদার বটের)", fontSize = 11.sp, color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardAccent
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (channelNameInput.trim().isEmpty() || channelIdentifierInput.trim().isEmpty() || botTokenInput.trim().isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে সবগুলো ঘর সঠিক টোকেন দিয়ে পূরণ করুন", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        viewModel.addTelegramChannel(
                            name = channelNameInput.trim(),
                            identifier = channelIdentifierInput.trim(),
                            botToken = botTokenInput.trim()
                        )
                        showAddDialog = false
                        channelNameInput = ""
                        channelIdentifierInput = ""
                        botTokenInput = ""
                    }
                ) {
                    Text("সংরক্ষণ করুন", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল", color = TextPrimary)
                }
            }
        )
    }
}

// ==========================================
// 3. AI HUB PANEL (SOCIAL ENHANCER & TRANSLATOR)
// ==========================================

@Composable
fun AIHubPanel(viewModel: DashboardViewModel) {
    var subTabIdx by remember { mutableIntStateOf(0) } // 0: Content Gen, 1: Translator
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Simple Beautiful segmented headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(DarkCardAccent)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (subTabIdx == 0) NeonMagenta else Color.Transparent)
                    .clickable { subTabIdx = 0 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "সোশ্যাল মিডিয়া মেটা-জেনারেটর",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subTabIdx == 0) DeepMidnight else TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (subTabIdx == 1) NeonCyan else Color.Transparent)
                    .clickable { subTabIdx = 1 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "মাল্টি-ল্যাঙ্গুয়েজ ট্রান্সলেটর",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subTabIdx == 1) DeepMidnight else TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (subTabIdx == 0) {
                SocialMediaSubPanel(
                    viewModel = viewModel,
                    onCopyToClipboard = { title, desc ->
                        clipboard.setText(AnnotatedString("$title\n\n$desc"))
                        Toast.makeText(context, "শিরোনাম এবং হ্যাশট্যাগ অনুলিপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                TranslationSubPanel(
                    viewModel = viewModel,
                    onCopyToClipboard = { text ->
                        clipboard.setText(AnnotatedString(text))
                        Toast.makeText(context, "অনুবাদ অনুলিপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun SocialMediaSubPanel(
    viewModel: DashboardViewModel,
    onCopyToClipboard: (String, String) -> Unit
) {
    val platform by viewModel.socialPlatform.collectAsStateWithLifecycle()
    val topic by viewModel.socialTopic.collectAsStateWithLifecycle()
    val tone by viewModel.socialTone.collectAsStateWithLifecycle()
    val generatedTitle by viewModel.socialGeneratedTitle.collectAsStateWithLifecycle()
    val generatedDesc by viewModel.socialGeneratedDesc.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingSocial.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Platform cards selector
        item {
            Column {
                Text(
                    text = "টার্গেট প্ল্যাটফর্ম সিলেক্ট করুন:",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("YouTube", "Facebook", "TikTok").forEach { plt ->
                        val selected = (plt == platform)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) NeonMagenta else DarkCardAccent)
                                .clickable { viewModel.setSocialPlatform(plt) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(plt) {
                                        "YouTube" -> Icons.Default.PlayArrow
                                        "Facebook" -> Icons.Default.Settings
                                        else -> Icons.Default.MusicNote
                                    },
                                    contentDescription = plt,
                                    tint = if (selected) DeepMidnight else TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = plt,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) DeepMidnight else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Context / Topic Input
        item {
            OutlinedTextField(
                value = topic,
                onValueChange = { viewModel.setSocialTopic(it) },
                label = { Text("আপনার ভিডিওর বিষয়বস্তু বা টপিকটি লিখুন...", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("social_topic_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkCardAccent,
                    focusedContainerColor = DarkCardAccent,
                    unfocusedContainerColor = DarkCardAccent
                ),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("যেমন: গরুর দুধের উপকারিতা, travel exploring sylhet", color = TextSecondary) }
            )
        }

        // Tone chips loop
        item {
            Column {
                Text(
                    text = "ভিডিওর রাইটিং টোন (Tone of voice):",
                    fontSize = 11.sp,
                    color = NeonAmber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("Catchy & Viral", "Professional", "Informative", "Humorous").forEach { t ->
                        val selected = (t == tone)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (selected) NeonCyan else DarkCardAccent)
                                .clickable { viewModel.setSocialTone(t) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) DeepMidnight else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Generate button
        item {
            Button(
                onClick = {
                    if (topic.trim().isEmpty()) {
                        Toast.makeText(context, "প্লেসহোল্ডার টপিক ফিল্ডটি পূর্ণ করুন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.generateSocialContents()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("social_generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp),
                enabled = !isGenerating
            ) {
                Text(
                    text = "মেটা কন্টেন্ট জেনারেট করুন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepMidnight
                )
            }
        }

        // Output Display Cards
        if (generatedTitle.isNotEmpty() || generatedDesc.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = NeonCyan.copy(alpha = 0.5f),
                                size = size,
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 1.5f)
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মেটা-ট্যাগ ফলাফল:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onCopyToClipboard(generatedTitle, generatedDesc) }
                            )
                        }

                        Divider(color = DarkCard)

                        // Title block
                        if (generatedTitle.isNotEmpty()) {
                            Text(
                                text = "শিরোনাম বিকল্প (Title):",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = generatedTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }

                        // Description and hashtag blocks
                        if (generatedDesc.isNotEmpty()) {
                            Text(
                                text = "ডেসক্রিপশন এবং ভাইরাল ট্যাগসমূহ (Description & Hashtags):",
                                fontSize = 11.sp,
                                color = NeonMagenta,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = generatedDesc,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationSubPanel(
    viewModel: DashboardViewModel,
    onCopyToClipboard: (String) -> Unit
) {
    val input by viewModel.translatorInput.collectAsStateWithLifecycle()
    val targetLang by viewModel.translatorTargetLang.collectAsStateWithLifecycle()
    val output by viewModel.translatorOutput.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Target Language Grid
        item {
            Column {
                Text(
                    text = "অনুবাদ করার ভাষা (Target Language):",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("English", "Bengali (বাংলা)", "Arabic (العربية)", "Hindi (हिन्दी)").forEach { lang ->
                        val selected = (lang == targetLang)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (selected) NeonCyan else DarkCardAccent)
                                .clickable { viewModel.selectTranslatorTarget(lang) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) DeepMidnight else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Input payload
        item {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.updateTranslatorInput(it) },
                label = { Text("অনুবাদ করার জন্য বাক্য বা অনুচ্ছেদটি লিখুন...", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("translator_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkCardAccent,
                    focusedContainerColor = DarkCardAccent,
                    unfocusedContainerColor = DarkCardAccent
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Translating indicators or action
        item {
            Button(
                onClick = {
                    if (input.trim().isEmpty()) {
                        Toast.makeText(context, "অনুগ্রহ করে লেখার ঘরটি পূরণ করুন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.performTranslation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("translator_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                shape = RoundedCornerShape(10.dp),
                enabled = !isTranslating
            ) {
                Text(
                    text = "ভাষান্তর করুন (A.I Translation)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Output Result
        if (output.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = NeonCyan.copy(alpha = 0.5f),
                                size = size,
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 1.5f)
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "অনুবাদিত ফলাফল ($targetLang):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onCopyToClipboard(output) }
                            )
                        }

                        Divider(color = DarkCard)

                        Text(
                            text = output,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CREATIVE STUDIO (PROMPT-TO-IMAGE & VISION) & SONG COMPOSER
// ==========================================

@Composable
fun CreativePanel(viewModel: DashboardViewModel) {
    var subSection by remember { mutableIntStateOf(0) } // 0: Image Studio, 1: Song Maker
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toggle Studio Segment Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(DarkCardAccent)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (subSection == 0) NeonMagenta else Color.Transparent)
                    .clickable { subSection = 0 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Image Studio",
                        tint = if (subSection == 0) DeepMidnight else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "এআই ভিজ্যুয়াল স্টুডিও",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subSection == 0) DeepMidnight else TextPrimary
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (subSection == 1) NeonCyan else Color.Transparent)
                    .clickable { subSection = 1 }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Song Maker",
                        tint = if (subSection == 1) DeepMidnight else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "এআই মিউজিক কম্পোজার",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subSection == 1) DeepMidnight else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (subSection == 0) {
                VisualStudioPanel(viewModel = viewModel)
            } else {
                MusicComposerPanel(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VisualStudioPanel(viewModel: DashboardViewModel) {
    var toolMode by remember { mutableIntStateOf(0) } // 0: Prompt to Image, 1: Image to Prompt
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val imagePrompt by viewModel.imagePromptText.collectAsStateWithLifecycle()
    val generatedUrl by viewModel.generatedImageUrl.collectAsStateWithLifecycle()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsStateWithLifecycle()

    val visionBitmap by viewModel.visionBitmap.collectAsStateWithLifecycle()
    val visionPrompt by viewModel.visionGeneratedPrompt.collectAsStateWithLifecycle()
    val isAnalyzingVision by viewModel.isAnalyzingVision.collectAsStateWithLifecycle()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TabRow(
                selectedTabIndex = toolMode,
                containerColor = Color.Transparent,
                contentColor = NeonCyan,
                divider = { Divider(color = DarkCardAccent) }
            ) {
                Tab(
                    selected = toolMode == 0,
                    onClick = { toolMode = 0 },
                    text = { Text("প্রম্পট থেকে ছবি তৈরী (Text-to-Image)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = toolMode == 1,
                    onClick = { toolMode = 1 },
                    text = { Text("ছবি থেকে প্রম্পট (Vision)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (toolMode == 0) {
            // Text to Image
            item {
                OutlinedTextField(
                    value = imagePrompt,
                    onValueChange = { viewModel.updateImagePrompt(it) },
                    label = { Text("কিরকমের ছবি জেনারেট করতে চান, তার বিবরণ লিখুন (English prefered)", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("image_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardAccent,
                        focusedContainerColor = DarkCardAccent,
                        unfocusedContainerColor = DarkCardAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("যেমন: Astronaut riding a glowing neon horse on Mars, cyber-punk, hyper-details", color = TextSecondary) }
                )
            }

            item {
                Button(
                    onClick = {
                        if (imagePrompt.trim().isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে ছবির বিবরণটি পূরণ করুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.generateImage()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_image_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isGeneratingImage
                ) {
                    if (isGeneratingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepMidnight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("এআই ক্যানভাস আঁকছে...", color = DeepMidnight)
                    } else {
                        Text("ছবি তৈরী করুন (Generate A.I Image)", color = DeepMidnight, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Generated Image Output View
            if (isGeneratingImage || generatedUrl != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .drawBehind {
                                drawRoundRect(
                                    color = NeonMagenta.copy(alpha = 0.5f),
                                    size = size,
                                    cornerRadius = CornerRadius(16f, 16f),
                                    style = Stroke(width = 2f)
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGeneratingImage) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = NeonMagenta)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "জিপিইউ নোডে আর্টিস্টিক রেন্ডার হচ্ছে...\nডিটেইলস হাইপার-স্কেলিং সম্পন্ন হচ্ছে",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else if (generatedUrl != null) {
                                // Real Image Render with Coil
                                AsyncImage(
                                    model = generatedUrl,
                                    contentDescription = "Dynamically Prepared AI Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay metadata and quick controls
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "আকার: 768 × 768 px",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Button(
                                            onClick = {
                                                clipboard.setText(AnnotatedString(generatedUrl ?: ""))
                                                Toast.makeText(context, "ছবির ব্রডকাস্ট লিংক ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = TextPrimary, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("কপি লিংক", fontSize = 10.sp, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Image to Prompt (Vision Tool)
            item {
                Text(
                    text = "ছবি থেকে প্রম্পট তৈরী করার জন্য ছবি আপলোড বা সিলেক্ট করুনঃ",
                    fontSize = 11.sp,
                    color = NeonAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCreativeSampleCard(
                        modifier = Modifier.weight(1f),
                        title = "সাইবারপঙ্ক সিটি কোলাজ",
                        tag = "Preset 1",
                        onClick = {
                            val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_gallery) ?: Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            viewModel.loadVisionImage(bitmap)
                            android.widget.Toast.makeText(context, "সাইবারপঙ্ক টেমপ্লেট লোড হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    PresetCreativeSampleCard(
                        modifier = Modifier.weight(1f),
                        title = "অ্যাবস্ট্রাক্ট কসমিক পোর্ট্রেট",
                        tag = "Preset 2",
                        onClick = {
                            val bitmap = BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_compass) ?: Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            viewModel.loadVisionImage(bitmap)
                            android.widget.Toast.makeText(context, "কসমিক পোর্ট্রেট লোড হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (visionBitmap != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCardAccent)
                            .border(1.5.dp, NeonCyan, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = getComposeImage(visionBitmap!!),
                            contentDescription = "Selected input image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.generatePromptFromImage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("analyze_vision_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isAnalyzingVision
                    ) {
                        if (isAnalyzingVision) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("এআই চোখ দিয়ে কন্টেন্ট দেখছে...", color = TextPrimary)
                        } else {
                            Text("ছবিটি বিশ্লেষণ করে প্রম্পট লিখুন", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (visionPrompt.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRoundRect(
                                    color = NeonCyan.copy(alpha = 0.5f),
                                size = size,
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 1.5f)
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "বিশ্লেষিত প্রম্পট ফলাফল (Creative Prompt):",
                                    fontSize = 11.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Prompt text",
                                        tint = NeonCyan,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                clipboard.setText(AnnotatedString(visionPrompt))
                                                Toast.makeText(context, "বিশ্লেষিত প্রম্পট কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                    // Send to dynamic text generator shortcut
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "Apply prompt in generator",
                                        tint = NeonMagenta,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                viewModel.updateImagePrompt(visionPrompt)
                                                toolMode = 0 // jump to generator view
                                                Toast.makeText(context, "প্রম্পট জেনারেটর বোর্ডে ফিড করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }

                            Divider(color = DarkCard)

                            Text(
                                text = visionPrompt,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MusicComposerPanel(viewModel: DashboardViewModel) {
    val topic by viewModel.songTopic.collectAsStateWithLifecycle()
    val genre by viewModel.songGenre.collectAsStateWithLifecycle()
    val tempo by viewModel.songTempo.collectAsStateWithLifecycle()
    val lyrics by viewModel.songLyricsOutput.collectAsStateWithLifecycle()
    val isComposing by viewModel.isComposingSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isSongPlaying.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Topic Input
        item {
            OutlinedTextField(
                value = topic,
                onValueChange = { viewModel.setSongTopic(it) },
                label = { Text("গানের মূল থিম বা বিষয়বস্তু লিখুন...", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("song_topic_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkCardAccent,
                    focusedContainerColor = DarkCardAccent,
                    unfocusedContainerColor = DarkCardAccent
                ),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("যেমন: বৃষ্টির রাত, lonely space traveler journey, হারিয়ে যাওয়া প্রেম", color = TextSecondary) }
            )
        }

        // Genre Selector Chips
        item {
            Column {
                Text(
                    text = "মিউজিক জনরা (Music Genre) সিলেক্ট করুনঃ",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Lofi Acoustic", "Synthwave", "Cyberbeat", "Pop Rock").forEach { g ->
                        val selected = (g == genre)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (selected) NeonMagenta else DarkCardAccent)
                                .clickable { viewModel.setSongGenre(g) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) DeepMidnight else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Tempo control row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCardAccent)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "টেম্পো বা বিট স্পীড:",
                    fontSize = 11.sp,
                    color = neonLight(0.9f),
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Slow (80 BPM)", "Medium (100 BPM)", "Fast (130 BPM)").forEach { tmp ->
                        val selected = (tmp == tempo)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) NeonCyan else DarkCard)
                                .clickable { viewModel.setSongTempo(tmp) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tmp.split(" ").first(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) DeepMidnight else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Magic Compose Button
        item {
            Button(
                onClick = {
                    if (topic.trim().isEmpty()) {
                        Toast.makeText(context, "দয়া করে গানের বিষয়বস্তুর বিবরণ দিন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.composeSong()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("compose_song_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp),
                enabled = !isComposing
            ) {
                if (isComposing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepMidnight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সং কম্পোজার লিরিক ও অ্যাকোস্টিক লিখছে...", color = DeepMidnight)
                } else {
                    Text("মিউজিক ও গান জেনারেট করুন", color = DeepMidnight, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Lyricist Output panel with Synthesiser Beat Player Overlay!
        if (lyrics.isNotEmpty()) {
            item {
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .drawBehind {
                             drawRoundRect(
                                 color = GlowPurple.copy(alpha = 0.5f),
                                 size = size,
                                 cornerRadius = CornerRadius(16f, 16f),
                                 style = Stroke(width = 1.5f)
                             )
                         },
                    colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header play buttons info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "প্রসিডুরাল সং অ্যাকোস্টিক শীটঃ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonAmber
                            )

                            // Synth Beats Audio toggle!
                            Button(
                                onClick = { viewModel.toggleSongAudioPlayback() },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) NeonMagenta else NeonCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Audio Play/Stop",
                                    tint = if (isPlaying) TextPrimary else DeepMidnight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPlaying) "Stop Demo Beat" else "Listen Demo Beats",
                                    fontSize = 10.sp,
                                    color = if (isPlaying) TextPrimary else DeepMidnight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isPlaying) {
                            // Pulsing audio light ring simulation to give a beautiful futuristic dynamic feedback
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonMagenta.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Synth active",
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "লাইভ সিন্থ নোড বাজছে... ($genre / $tempo)",
                                    fontSize = 11.sp,
                                    color = NeonMagenta,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = DarkCard)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Full lyrics and Chords",
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        clipboard.setText(AnnotatedString(lyrics))
                                        Toast.makeText(context, "লিরিক এবং অ্যাকোস্টিক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }

                        Text(
                            text = lyrics,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PROFILE CONTROL PANEL
// ==========================================

@Composable
fun ProfilePanel(viewModel: DashboardViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val avatars = listOf("avatar_neon_1", "avatar_neon_2", "avatar_neon_3", "avatar_neon_4")

    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("") }
    var activeAvatarId by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            editName = it.name
            editBio = it.bio
            editStatus = it.status
            activeAvatarId = it.avatarIdentifier
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "ইউজার প্রোফাইল সেটিংস",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Large display card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = NeonMagenta.copy(alpha = 0.5f),
                            size = size,
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2f)
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(DarkCardAccent)
                            .border(3.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAvatarIcon(activeAvatarId.ifEmpty { "avatar_neon_1" }),
                            contentDescription = "Profile Avatar",
                            tint = NeonMagenta,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Text(
                        text = editName.ifEmpty { "ইউজার প্রোফাইল" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Text(
                        text = editBio.ifEmpty { "এআই এক্সপ্লোরার" },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Avatar selector
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "প্রোফাইল অ্যাভাটার লিংক সিলেক্ট করুনঃ",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    avatars.forEach { id ->
                        val active = (id == activeAvatarId)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) NeonCyan else DarkCardAccent)
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) NeonCyan else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { activeAvatarId = id }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAvatarIcon(id),
                                contentDescription = id,
                                tint = if (active) DeepMidnight else TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input forms
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("প্রোফাইল নাম", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardAccent,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = editBio,
                    onValueChange = { editBio = it },
                    label = { Text("প্রোফাইল Bio বিবরণী", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_bio_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardAccent,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = editStatus,
                    onValueChange = { editStatus = it },
                    label = { Text("ড্যাশবোর্ড কাস্টম স্ট্যাটাস", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_status_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkCardAccent,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    if (editName.trim().isEmpty() || editBio.trim().isEmpty()) {
                        Toast.makeText(context, "নাম ও Bio ফিল্ডগুলো খালি রাখা যাবে না", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.updateProfile(
                        name = editName.trim(),
                        bio = editBio.trim(),
                        status = editStatus.trim(),
                        avatarId = activeAvatarId
                    )
                    Toast.makeText(context, "প্রোফাইল ডাটাবেসে সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "প্রোফাইল কাস্টমাইজেশন সেভ করুন",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// CUSTOM UI UTILS & DESIGN PIECES
// ==========================================

@Composable
fun TopDashboardHeader(profile: UserProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "AI Nexus",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "All-in-One Dashboard",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.2.sp
            )
        }

        // Sleek profile ring matching details: "w-11 h-11 rounded-full bg-[#D1E4FF] border-2 border-[#43474E]"
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NeonMagenta)
                .border(2.dp, DarkCardAccent, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(NeonMagenta),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAvatarIcon(profile?.avatarIdentifier ?: "avatar_neon_1"),
                    contentDescription = "Header profile node",
                    tint = Color(0xFF001D36), // High contrast deep blue for perfect visibility on light background
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun GlowingBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(68.dp)
            .border(
                width = 1.1.dp,
                color = DarkCardAccent, // Sleek solid border matching #43474E
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        containerColor = DarkCard, // #2E3033
        tonalElevation = 8.dp
    ) {
        val tabConfig = listOf(
            Triple("ড্যাশবোর্ড", Icons.Default.Hub, 0),
            Triple("টেলিগ্রাম", Icons.Default.Campaign, 1),
            Triple("এআই হাব", Icons.Default.Language, 2),
            Triple("ক্রিয়েটিভ", Icons.Default.Palette, 3),
            Triple("প্রোফাইল", Icons.Default.Person, 4)
        )

        tabConfig.forEach { (label, icon, idx) ->
            val isSelected = (selectedTab == idx)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(idx) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) NeonMagenta else TextSecondary, // NeonMagenta is #D1E4FF (Sleek light blue)
                        modifier = Modifier
                            .size(22.dp)
                            .testTag("nav_icon_${idx}")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isSelected) NeonMagenta else TextSecondary,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = DarkCardAccent.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    tint: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = DarkCardAccent, // Sleek border matching #43474E
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkCard), // Sleek BG matching #2E3033
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ControlPortalRow(
    title: String,
    desc: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCard) // Sleek surface bg (#2E3033)
            .border(1.5.dp, DarkCardAccent, RoundedCornerShape(24.dp)) // Sleek border (#43474E)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Navigate icon",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun HistoryRow(log: HistoryLog, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCardAccent.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (log.toolName) {
                                "AI Translator" -> NeonCyan
                                "AI Song Composer" -> GlowPurple
                                else -> NeonMagenta
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.toolName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonAmber
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ইনপুট: ${log.promptInput}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ফলাফল: ${log.generatedOutput}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy history logs text",
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun TelegramChannelRow(channel: TelegramChannel, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardAccent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = channel.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "ID/Username: ${channel.identifier}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete telegram target channel",
                tint = NeonMagenta,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PresetCreativeSampleCard(
    modifier: Modifier = Modifier,
    title: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkCardAccent),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonMagenta.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = tag, fontSize = 8.sp, color = NeonMagenta, fontWeight = FontWeight.Bold)
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun getAvatarIcon(id: String): ImageVector {
    return when(id) {
        "avatar_neon_1" -> Icons.Default.AutoAwesome
        "avatar_neon_2" -> Icons.Default.Hub
        "avatar_neon_3" -> Icons.Default.Settings
        "avatar_neon_4" -> Icons.Default.Palette
        else -> Icons.Default.Person
    }
}

fun getComposeImage(bitmap: Bitmap): androidx.compose.ui.graphics.ImageBitmap {
    return bitmap.asImageBitmap()
}

fun neonLight(ratio: Float): Color {
    return Color(0xFFFFB300).copy(alpha = ratio)
}

// Custom simple formatted Toast triggers for clean Bengali instructions
object Toast {
    fun formatToast(msg: String, context: android.content.Context) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
