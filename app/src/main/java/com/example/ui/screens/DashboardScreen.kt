package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.PostIQViewModel
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: PostIQViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val audits by viewModel.audits.collectAsState()
    val calibrations by viewModel.calibrations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterFormat by viewModel.selectedFilterFormat.collectAsState()

    val filteredAudits = remember(audits, searchQuery, filterFormat) {
        audits.filter { audit ->
            val matchesQuery = searchQuery.isBlank() ||
                    audit.caption.contains(searchQuery, ignoreCase = true) ||
                    audit.niche.contains(searchQuery, ignoreCase = true)
            val matchesFormat = filterFormat == null || audit.contentType == filterFormat
            matchesQuery && matchesFormat
        }
    }

    val avgScore = remember(audits) {
        if (audits.isEmpty()) 0
        else audits.flatMap { it.results }.map { it.scoreOverall }.average().toInt()
    }

    Scaffold(
        containerColor = Slate950,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.Analyzer) },
                containerColor = Purple500,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_audit")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Audit")
                    Text("New Audit", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top App Banner / Brand Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Purple500, Pink500)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "IQ",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Post",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Text(
                                    "IQ",
                                    color = Purple400,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                            }
                            Text(
                                "AI Social Post Auditor",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // User Tier Pill
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .testTag("tier_pill")
                            .clickable { viewModel.navigateTo(Screen.Pricing) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (user.tier == UserTier.FREE) Slate400 else Purple400)
                            )
                            Text(
                                user.tier.displayName,
                                color = Purple300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Metric Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "TOTAL AUDITS",
                        value = "${audits.size}",
                        accentColor = Purple400
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "AVG QUALITY",
                        value = "$avgScore/100",
                        accentColor = Cyan400
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "CALIBRATIONS",
                        value = "${calibrations.size}",
                        accentColor = Emerald400
                    )
                }
            }

            // Quick Nav Chips (Calibration Loop, Pricing Plans)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { viewModel.navigateTo(Screen.Calibration) },
                        label = { Text("Calibration Feedback Loop", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = Purple400,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate900,
                            labelColor = Slate200
                        ),
                        border = AssistChipDefaults.assistChipBorder(borderColor = Slate800, enabled = true),
                        modifier = Modifier.testTag("btn_nav_calibration")
                    )
                    AssistChip(
                        onClick = { viewModel.navigateTo(Screen.Pricing) },
                        label = { Text("Account Tiers", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Pink400,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate900,
                            labelColor = Slate200
                        ),
                        border = AssistChipDefaults.assistChipBorder(borderColor = Slate800, enabled = true),
                        modifier = Modifier.testTag("btn_nav_pricing")
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_audits"),
                    placeholder = { Text("Search post title or niche...", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedBorderColor = Purple500,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Format Filter Horizontal List
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterFormat == null,
                            onClick = { viewModel.setFilterFormat(null) },
                            label = { Text("All Formats") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple900,
                                selectedLabelColor = Purple300,
                                containerColor = Slate900,
                                labelColor = Slate400
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterFormat == null, borderColor = Slate800)
                        )
                    }
                    items(ContentFormat.entries.toTypedArray()) { format ->
                        val isSelected = filterFormat == format
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setFilterFormat(if (filterFormat == format) null else format)
                            },
                            label = { Text(format.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple900,
                                selectedLabelColor = Purple300,
                                containerColor = Slate900,
                                labelColor = Slate400
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = Slate800)
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Audit History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${filteredAudits.size} posts",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            }

            // Audit Cards
            if (filteredAudits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Slate700,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No audits found",
                                color = Slate400,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { viewModel.navigateTo(Screen.Analyzer) },
                                colors = ButtonDefaults.buttonColors(containerColor = Purple500)
                            ) {
                                Text("Create First Audit")
                            }
                        }
                    }
                }
            } else {
                items(filteredAudits, key = { it.id }) { audit ->
                    AuditCard(
                        audit = audit,
                        onClick = {
                            if (audit.results.size == 1) {
                                viewModel.navigateTo(Screen.Report(audit.id, audit.results.first().platform))
                            } else {
                                viewModel.navigateTo(Screen.Compare(audit.id))
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                value,
                color = accentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun AuditCard(
    audit: Audit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topReport = remember(audit) {
        audit.results.maxByOrNull { it.scoreOverall } ?: audit.results.firstOrNull()
    }
    val score = topReport?.scoreOverall ?: 70

    val scoreBadgeColor = when {
        score >= 85 -> Cyan500
        score >= 70 -> Emerald500
        score >= 50 -> Amber500
        else -> Red500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("audit_card_${audit.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Format badge, Date, and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            audit.contentType.name,
                            color = Purple300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            audit.niche.replaceFirstChar { it.uppercase() },
                            color = Slate300,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Overall Score Badge
                Surface(
                    color = scoreBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scoreBadgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        "$score/100",
                        color = scoreBadgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Caption Text
            Text(
                audit.caption,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Platforms analysed & date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    audit.results.forEach { r ->
                        Surface(
                            color = Slate850,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Text(
                                r.platform.displayName.take(2).uppercase(),
                                color = Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        audit.createdAt,
                        color = Slate400,
                        fontSize = 12.sp
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
