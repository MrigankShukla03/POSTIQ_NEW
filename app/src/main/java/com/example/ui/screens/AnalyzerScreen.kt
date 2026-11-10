package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
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
fun AnalyzerScreen(
    viewModel: PostIQViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val progress by viewModel.analysisProgress.collectAsState()

    var contentType by remember { mutableStateOf(ContentFormat.VIDEO) }
    var caption by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawHashtags by remember { mutableStateOf("") }
    var niche by remember { mutableStateOf("technology") }
    var targetAudience by remember { mutableStateOf("") }
    var selectedPlatforms by remember { mutableStateOf(listOf(SocialPlatform.YOUTUBE)) }
    var followerCounts by remember { mutableStateOf(mutableMapOf<SocialPlatform, String>()) }

    // Link Import
    var importUrl by remember { mutableStateOf("") }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Already Posted
    var isAlreadyPosted by remember { mutableStateOf(false) }
    var viewsText by remember { mutableStateOf("") }
    var likesText by remember { mutableStateOf("") }
    var commentsText by remember { mutableStateOf("") }
    var sharesText by remember { mutableStateOf("") }
    var savesText by remember { mutableStateOf("") }
    var watchTimeText by remember { mutableStateOf("") }

    val niches = listOf(
        "general" to "General / Lifestyle",
        "technology" to "Technology & SaaS",
        "finance" to "Finance & Investing",
        "fitness" to "Fitness & Health",
        "education" to "Education & Careers",
        "comedy" to "Comedy & Entertainment",
        "cooking" to "Cooking & Food"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Post Audit",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        modifier = Modifier.testTag("btn_back_analyzer")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Import From Link Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Cyan400)
                            Text(
                                "Import Post from URL",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            "Paste a YouTube, TikTok, Instagram, or LinkedIn link to auto-extract captions, tags, and stats.",
                            color = Slate400,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = importUrl,
                                onValueChange = { importUrl = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_import_url"),
                                placeholder = { Text("https://youtube.com/shorts/...", color = Slate400, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = Purple500,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    if (importUrl.isNotBlank()) {
                                        val imported = viewModel.repository.importFromLink(importUrl)
                                        contentType = imported.format
                                        caption = imported.caption
                                        description = imported.description
                                        rawHashtags = imported.hashtags
                                        niche = imported.niche
                                        selectedPlatforms = listOf(imported.platform)
                                        followerCounts[imported.platform] = imported.followers.toString()
                                        if (imported.isLive) {
                                            isAlreadyPosted = true
                                            viewsText = imported.views.toString()
                                            likesText = imported.likes.toString()
                                            commentsText = imported.comments.toString()
                                            watchTimeText = imported.watchTime.toString()
                                        }
                                        importSuccessMessage = "Loaded post details from ${imported.platform.displayName}!"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                                modifier = Modifier.testTag("btn_import_link")
                            ) {
                                Text("Fetch", fontWeight = FontWeight.Bold, color = Slate950)
                            }
                        }

                        if (importSuccessMessage != null) {
                            Text(
                                importSuccessMessage!!,
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Content Format Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "CONTENT FORMAT",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContentFormat.entries.forEach { format ->
                            val isSelected = contentType == format
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { contentType = format }
                                    .testTag("format_card_${format.name.lowercase()}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Purple900.copy(alpha = 0.6f) else Slate900
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Purple500 else Slate800
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp, horizontal = 4.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        when (format) {
                                            ContentFormat.VIDEO -> Icons.Default.PlayCircle
                                            ContentFormat.IMAGE -> Icons.Default.Image
                                            ContentFormat.CAROUSEL -> Icons.Default.ViewCarousel
                                            ContentFormat.TEXT -> Icons.Default.Notes
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Purple300 else Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        format.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (isSelected) Color.White else Slate300,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Caption / Title Input
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "POST CAPTION / TITLE",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "${caption.length} chars",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_caption"),
                        placeholder = { Text("Enter your headline, hook or post title...", color = Slate400) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = Purple500,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Description / Body Text
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "DESCRIPTION / BODY COPY",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_description"),
                        placeholder = { Text("Detailed description, video chapters, or body text...", color = Slate400) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900,
                            focusedBorderColor = Purple500,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Hashtags & Niche Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "RAW HASHTAGS",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        OutlinedTextField(
                            value = rawHashtags,
                            onValueChange = { rawHashtags = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_hashtags"),
                            placeholder = { Text("#ai, #tech, #growth, #coding", color = Slate400) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedBorderColor = Purple500,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Niche Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "CONTENT NICHE",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(niches) { (key, label) ->
                                val isSelected = niche == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { niche = key },
                                    label = { Text(label, fontSize = 12.sp) },
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
                }
            }

            // Platform Multi-Select
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TARGET PLATFORMS",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (user.tier == UserTier.FREE) {
                            Text(
                                "Free tier: 1 platform max",
                                color = Amber400,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                "${selectedPlatforms.size} selected",
                                color = Cyan400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SocialPlatform.entries.toTypedArray()) { platform ->
                            val isSelected = selectedPlatforms.contains(platform)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (user.tier == UserTier.FREE) {
                                        selectedPlatforms = listOf(platform)
                                    } else {
                                        selectedPlatforms = if (isSelected) {
                                            if (selectedPlatforms.size > 1) selectedPlatforms - platform else selectedPlatforms
                                        } else {
                                            selectedPlatforms + platform
                                        }
                                    }
                                },
                                label = { Text(platform.displayName, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple500,
                                    selectedLabelColor = Color.White,
                                    containerColor = Slate900,
                                    labelColor = Slate300
                                ),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = Slate800),
                                modifier = Modifier.testTag("platform_chip_${platform.id}")
                            )
                        }
                    }

                    // Follower count inputs for selected platforms
                    selectedPlatforms.forEach { platform ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate900, RoundedCornerShape(12.dp))
                                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${platform.displayName} Followers:",
                                color = Slate200,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = followerCounts[platform] ?: "1000",
                                onValueChange = { newValue ->
                                    val map = followerCounts.toMutableMap()
                                    map[platform] = newValue.filter { it.isDigit() }
                                    followerCounts = map
                                },
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("input_followers_${platform.id}"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = Purple500,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Already Posted & Log Metrics Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
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
                                    "Already Posted Content?",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Log live metrics to calibrate algorithmic weights",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = isAlreadyPosted,
                                onCheckedChange = { isAlreadyPosted = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Purple500
                                ),
                                modifier = Modifier.testTag("toggle_already_posted")
                            )
                        }

                        AnimatedVisibility(visible = isAlreadyPosted) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = viewsText,
                                        onValueChange = { viewsText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Views", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = likesText,
                                        onValueChange = { likesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Likes", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = commentsText,
                                        onValueChange = { commentsText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Comments", fontSize = 11.sp) },
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
                                        value = sharesText,
                                        onValueChange = { sharesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Shares", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = savesText,
                                        onValueChange = { savesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Saves", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = watchTimeText,
                                        onValueChange = { watchTimeText = it },
                                        label = { Text("Watch Time %", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Run Audit Button & Progress
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isAnalyzing) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Purple500,
                            trackColor = Slate850
                        )
                        Text(
                            "Analyzing rubrics across ${selectedPlatforms.size} platforms...",
                            color = Purple300,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Button(
                        onClick = {
                            val activeCaption = if (caption.isNotBlank()) caption else "Untitled Social Post"
                            val convertedFollowers = selectedPlatforms.associateWith { p ->
                                (followerCounts[p]?.toLongOrNull() ?: 1000L)
                            }
                            viewModel.runAnalysis(
                                format = contentType,
                                caption = activeCaption,
                                description = description,
                                rawHashtags = rawHashtags,
                                niche = niche,
                                targetAudience = targetAudience,
                                selectedPlatforms = selectedPlatforms,
                                followerCounts = convertedFollowers,
                                isAlreadyPosted = isAlreadyPosted,
                                postViews = viewsText.toLongOrNull() ?: 0L,
                                postLikes = likesText.toLongOrNull() ?: 0L,
                                postComments = commentsText.toLongOrNull() ?: 0L,
                                postShares = sharesText.toLongOrNull() ?: 0L,
                                postSaves = savesText.toLongOrNull() ?: 0L,
                                postWatchTime = watchTimeText.toFloatOrNull() ?: 0f
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_run_audit"),
                        enabled = !isAnalyzing,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple500,
                            disabledContainerColor = Slate800
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                            Text(
                                if (isAnalyzing) "Auditing Post..." else "Run Multi-Platform Audit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
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
