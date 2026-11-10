package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareViewScreen(
    auditId: String,
    viewModel: PostIQViewModel,
    modifier: Modifier = Modifier
) {
    val audits by viewModel.audits.collectAsState()
    val audit = audits.find { it.id == auditId }

    if (audit == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                Text("Return to Dashboard")
            }
        }
        return
    }

    val bestReport = remember(audit) {
        audit.results.maxByOrNull { it.scoreOverall } ?: audit.results.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Platform Comparison",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            "${audit.results.size} platforms evaluated",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        modifier = Modifier.testTag("btn_back_compare")
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
            // Recommendation Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Purple500.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(Purple900.copy(alpha = 0.3f), Slate900)
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Purple500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                "OPTIMAL PLATFORM FIT",
                                color = Purple300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            "${bestReport.platform.displayName} is your highest-leverage channel",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )

                        Text(
                            "This post scored ${bestReport.scoreOverall}/100 on ${bestReport.platform.displayName} with an estimated reach of ${String.format("%,d", bestReport.estimatedViews)} views. The hook and copy align with the current feed algorithm.",
                            color = Slate300,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Platform Cards List
            item {
                Text(
                    "CROSS-PLATFORM SCOREBOARD",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            items(audit.results, key = { it.platform.id }) { report ->
                val isBest = report.platform == bestReport.platform
                val score = report.scoreOverall

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(Screen.Report(audit.id, report.platform))
                        }
                        .testTag("compare_card_${report.platform.id}"),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isBest) 1.5.dp else 1.dp,
                        if (isBest) Purple500 else Slate800
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = Slate850,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            report.platform.displayName.take(2).uppercase(),
                                            color = Purple300,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            report.platform.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (isBest) {
                                            Surface(
                                                color = Purple500.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "BEST",
                                                    color = Purple400,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        report.platform.subtitle,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Score Badge
                            Surface(
                                color = if (score >= 80) Cyan500.copy(alpha = 0.15f) else if (score >= 60) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (score >= 80) Cyan500.copy(alpha = 0.4f) else if (score >= 60) Emerald500.copy(alpha = 0.4f) else Amber500.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    "$score/100",
                                    color = if (score >= 80) Cyan400 else if (score >= 60) Emerald400 else Amber400,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate850, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EST. VIEWS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    String.format("%,d", report.estimatedViews),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FINDINGS", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${report.findings.size} issues",
                                    color = if (report.findings.isEmpty()) Emerald400 else Amber400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TAGS OPTIMIZED", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${report.adaptedHashtags.split(' ').count { it.startsWith('#') }} tags",
                                    color = Cyan400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Preview Adapted Caption
                        Text(
                            report.adaptedCaption,
                            color = Slate300,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "View Full Platform Report",
                                color = Purple400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Purple400,
                                modifier = Modifier.size(16.dp)
                            )
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
