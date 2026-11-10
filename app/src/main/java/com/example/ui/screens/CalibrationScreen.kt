package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.PostIQViewModel
import com.example.ui.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    viewModel: PostIQViewModel,
    modifier: Modifier = Modifier
) {
    val calibrations by viewModel.calibrations.collectAsState()
    val audits by viewModel.audits.collectAsState()
    val performanceLogs by viewModel.performanceLogs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Multipliers, 1: Log Performance, 2: Trends
    val tabTitles = listOf("Multipliers", "Log Performance", "Trends")

    var selectedPlatformFilter by remember { mutableStateOf(SocialPlatform.YOUTUBE) }

    // Form inputs for logging performance
    var selectedAuditId by remember { mutableStateOf(audits.firstOrNull()?.id ?: "") }
    var selectedLogPlatform by remember { mutableStateOf(SocialPlatform.YOUTUBE) }
    var viewsText by remember { mutableStateOf("") }
    var likesText by remember { mutableStateOf("") }
    var commentsText by remember { mutableStateOf("") }
    var sharesText by remember { mutableStateOf("") }
    var savesText by remember { mutableStateOf("") }
    var watchTimeText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Calibration Feedback Loop",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        modifier = Modifier.testTag("btn_back_calibration")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        containerColor = Slate950
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null, tint = Cyan400)
                            Text(
                                "Continuous Learning Engine",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            "PostIQ uses Pearson correlation between your audit scores and real post outcomes to tune dimension weights specifically for your audience.",
                            color = Slate300,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Tabs Selector
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Slate900,
                    contentColor = Purple400,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    // TAB 0: Active Multipliers
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SocialPlatform.entries.toTypedArray()) { platform ->
                                val isSelected = selectedPlatformFilter == platform
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPlatformFilter = platform },
                                    label = { Text(platform.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Purple900,
                                        selectedLabelColor = Purple300,
                                        containerColor = Slate900,
                                        labelColor = Slate300
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = Slate800)
                                )
                            }
                        }
                    }

                    val filteredCals = calibrations.filter { it.platform == selectedPlatformFilter }
                    if (filteredCals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "No custom calibrations for ${selectedPlatformFilter.displayName} yet.",
                                        color = Slate400,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Log real performance metrics in the 'Log Performance' tab to calibrate weights.",
                                        color = Slate400,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredCals) { cal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            cal.dimension.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Niche: ${cal.niche} • Updated ${cal.updatedAt}",
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        color = if (cal.multiplier >= 1.0f) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (cal.multiplier >= 1.0f) Emerald500.copy(alpha = 0.3f) else Amber500.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Text(
                                            String.format("%.2fx weight", cal.multiplier),
                                            color = if (cal.multiplier >= 1.0f) Emerald400 else Amber400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Log Performance
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "SELECT AUDITED POST",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(audits) { auditItem ->
                                        val isSelected = selectedAuditId == auditItem.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedAuditId = auditItem.id },
                                            label = {
                                                Text(
                                                    auditItem.caption.take(24) + "...",
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Purple500,
                                                selectedLabelColor = Color.White,
                                                containerColor = Slate850,
                                                labelColor = Slate300
                                            )
                                        )
                                    }
                                }

                                Text(
                                    "PLATFORM",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(SocialPlatform.entries.toTypedArray()) { p ->
                                        FilterChip(
                                            selected = selectedLogPlatform == p,
                                            onClick = { selectedLogPlatform = p },
                                            label = { Text(p.displayName) }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = viewsText,
                                        onValueChange = { viewsText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Views") },
                                        modifier = Modifier.weight(1f).testTag("input_log_views"),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = likesText,
                                        onValueChange = { likesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Likes") },
                                        modifier = Modifier.weight(1f).testTag("input_log_likes"),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = commentsText,
                                        onValueChange = { commentsText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Comments") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = sharesText,
                                        onValueChange = { sharesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Shares") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = savesText,
                                        onValueChange = { savesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Saves") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = watchTimeText,
                                        onValueChange = { watchTimeText = it },
                                        label = { Text("Watch Time %") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.logPostPerformance(
                                            auditId = selectedAuditId,
                                            platform = selectedLogPlatform,
                                            views = viewsText.toLongOrNull() ?: 1000L,
                                            likes = likesText.toLongOrNull() ?: 100L,
                                            comments = commentsText.toLongOrNull() ?: 20L,
                                            shares = sharesText.toLongOrNull() ?: 10L,
                                            saves = savesText.toLongOrNull() ?: 30L,
                                            watchTime = watchTimeText.toFloatOrNull() ?: 50.0f
                                        )
                                        activeTab = 0
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_submit_performance"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Purple500)
                                ) {
                                    Text("Recalibrate Algorithmic Weights", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Performance Trends
                    item {
                        Text(
                            "LOGGED POST PERFORMANCE HISTORY",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (performanceLogs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No performance logs recorded yet.", color = Slate400)
                                }
                            }
                        }
                    } else {
                        items(performanceLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            log.platform.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            log.loggedAt,
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate850, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("VIEWS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(String.format("%,d", log.views), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("LIKES", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(String.format("%,d", log.likes), color = Pink400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("COMMENTS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(String.format("%,d", log.comments), color = Cyan400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("WATCH TIME", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("${log.watchTimePct}%", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
