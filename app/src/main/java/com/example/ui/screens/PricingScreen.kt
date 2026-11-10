package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserTier
import com.example.ui.PostIQViewModel
import com.example.ui.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    viewModel: PostIQViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account Subscription Plans",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        modifier = Modifier.testTag("btn_back_pricing")
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
            item {
                Text(
                    "Scale your content across platforms with AI-driven calibrations and multi-platform reach optimization.",
                    color = Slate300,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Plan 1: Free Starter
            item {
                PlanCard(
                    tier = UserTier.FREE,
                    name = "Free Starter",
                    price = "$0",
                    period = "forever",
                    description = "Test the waters and optimize individual posts.",
                    features = listOf(
                        "Single platform per audit",
                        "Heuristic rubric scoring",
                        "Actionable findings and suggestions",
                        "Platform-adapted captions"
                    ),
                    isCurrent = user.tier == UserTier.FREE,
                    isPopular = false,
                    onSelect = { viewModel.updateTier(UserTier.FREE) }
                )
            }

            // Plan 2: Creator Pro
            item {
                PlanCard(
                    tier = UserTier.CREATOR,
                    name = "Creator Pro",
                    price = "$29",
                    period = "month",
                    description = "Cross-platform optimization with continuous feedback calibration.",
                    features = listOf(
                        "Audit all 7 platforms side-by-side",
                        "Continuous calibration feedback loop",
                        "Custom platform weight multipliers",
                        "AI cold-start & audience reach strategies",
                        "Estimated views & distribution modeling"
                    ),
                    isCurrent = user.tier == UserTier.CREATOR,
                    isPopular = true,
                    onSelect = { viewModel.updateTier(UserTier.CREATOR) }
                )
            }

            // Plan 3: Agency Elite
            item {
                PlanCard(
                    tier = UserTier.PRO,
                    name = "Agency Elite",
                    price = "$99",
                    period = "month",
                    description = "Complete suite for creators, brands, and agency portfolios.",
                    features = listOf(
                        "Unlimited multi-platform audits",
                        "Multi-niche advanced calibrations",
                        "Full algorithmic trend diagnostics",
                        "Continuous performance tracking",
                        "Priority AI copywriting variations"
                    ),
                    isCurrent = user.tier == UserTier.PRO,
                    isPopular = false,
                    onSelect = { viewModel.updateTier(UserTier.PRO) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PlanCard(
    tier: UserTier,
    name: String,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    isCurrent: Boolean,
    isPopular: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${tier.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isPopular) 1.5.dp else 1.dp,
            if (isPopular) Purple500 else Slate800
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        name,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        description,
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
                if (isPopular) {
                    Surface(
                        color = Purple500,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "MOST POPULAR",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    " / $period",
                    color = Slate400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Divider(color = Slate800, thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isPopular) Purple400 else Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            feat,
                            color = Slate300,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_select_tier_${tier.name.lowercase()}"),
                enabled = !isCurrent,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) Purple500 else Slate800,
                    disabledContainerColor = Slate850
                )
            ) {
                Text(
                    if (isCurrent) "Current Plan" else "Switch to $name",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Slate400 else Color.White
                )
            }
        }
    }
}
