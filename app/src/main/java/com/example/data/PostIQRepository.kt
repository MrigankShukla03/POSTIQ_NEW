package com.example.data

import com.example.model.*
import com.example.service.PostIQServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class PostIQRepository {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    private val _currentUser = MutableStateFlow(
        User(
            id = "usr_creator_01",
            name = "Alex Morgan",
            email = "alex@postiq.ai",
            tier = UserTier.CREATOR,
            createdAt = "2026-01-15"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _audits = MutableStateFlow<List<Audit>>(emptyList())
    val audits: StateFlow<List<Audit>> = _audits.asStateFlow()

    private val _performanceLogs = MutableStateFlow<List<PerformanceLog>>(emptyList())
    val performanceLogs: StateFlow<List<PerformanceLog>> = _performanceLogs.asStateFlow()

    private val _calibrations = MutableStateFlow<List<Calibration>>(emptyList())
    val calibrations: StateFlow<List<Calibration>> = _calibrations.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val dateNow = dateFormat.format(Date())

        val initialCalibrations = listOf(
            Calibration("cal_1", SocialPlatform.YOUTUBE, "technology", "seo", 1.25f, dateNow),
            Calibration("cal_2", SocialPlatform.YOUTUBE, "technology", "hook", 1.15f, dateNow),
            Calibration("cal_3", SocialPlatform.INSTAGRAM, "fitness", "hook", 1.30f, dateNow),
            Calibration("cal_4", SocialPlatform.INSTAGRAM, "fitness", "cta", 0.90f, dateNow),
            Calibration("cal_5", SocialPlatform.TIKTOK, "general", "pacing", 1.20f, dateNow),
            Calibration("cal_6", SocialPlatform.LINKEDIN, "technology", "captions", 1.20f, dateNow)
        )
        _calibrations.value = initialCalibrations

        // Seed 3 realistic audits
        val audit1 = createInitialAudit(
            id = "aud_yt_tech",
            format = ContentFormat.VIDEO,
            caption = "How AI Agents Will Replace Web Frameworks in 2026",
            desc = "Full breakdown of autonomous AI coding agents, architectural shifts, and practical developer workflows with benchmarks.",
            tags = "ai, coding, tech, software, developer",
            niche = "technology",
            audience = "Software Engineers & Tech Founders",
            date = "Today, 10:30 AM",
            platforms = listOf(SocialPlatform.YOUTUBE, SocialPlatform.X, SocialPlatform.LINKEDIN),
            followers = mapOf(SocialPlatform.YOUTUBE to 14200L, SocialPlatform.X to 8500L, SocialPlatform.LINKEDIN to 4300L)
        )

        val audit2 = createInitialAudit(
            id = "aud_ig_fitness",
            format = ContentFormat.VIDEO,
            caption = "Stop doing sit-ups! 3 Core exercises that actually build abdominal strength",
            desc = "Save this for your next gym session. Common mistakes breakdown and biomechanical form fixes.",
            tags = "fitness, abs, workout, health, gym",
            niche = "fitness",
            audience = "Gym goers and health enthusiasts",
            date = "Yesterday",
            platforms = listOf(SocialPlatform.INSTAGRAM, SocialPlatform.TIKTOK),
            followers = mapOf(SocialPlatform.INSTAGRAM to 28500L, SocialPlatform.TIKTOK to 62000L)
        )

        val audit3 = createInitialAudit(
            id = "aud_li_growth",
            format = ContentFormat.CAROUSEL,
            caption = "The 5 SaaS pricing mistakes costing early-stage startups 40% ARR",
            desc = "Analysis of 120 B2B SaaS pricing models and how value metrics transform contract expansion rates.",
            tags = "saas, business, pricing, startups, growth",
            niche = "technology",
            audience = "Founders and Product Managers",
            date = "Nov 03, 2026",
            platforms = listOf(SocialPlatform.LINKEDIN, SocialPlatform.X),
            followers = mapOf(SocialPlatform.LINKEDIN to 9100L, SocialPlatform.X to 5400L)
        )

        _audits.value = listOf(audit1, audit2, audit3)

        // Seed initial performance logs
        val log1 = PerformanceLog(
            id = "log_1",
            auditId = "aud_yt_tech",
            platform = SocialPlatform.YOUTUBE,
            views = 18450L,
            likes = 1240L,
            comments = 185L,
            shares = 430L,
            saves = 980L,
            watchTimePct = 68.4f,
            loggedAt = "Nov 05, 2026"
        )
        val log2 = PerformanceLog(
            id = "log_2",
            auditId = "aud_ig_fitness",
            platform = SocialPlatform.INSTAGRAM,
            views = 42100L,
            likes = 3980L,
            comments = 210L,
            shares = 1420L,
            saves = 2650L,
            watchTimePct = 74.2f,
            loggedAt = "Nov 04, 2026"
        )
        _performanceLogs.value = listOf(log1, log2)
    }

    private fun createInitialAudit(
        id: String,
        format: ContentFormat,
        caption: String,
        desc: String,
        tags: String,
        niche: String,
        audience: String,
        date: String,
        platforms: List<SocialPlatform>,
        followers: Map<SocialPlatform, Long>
    ): Audit {
        val results = platforms.map { platform ->
            PostIQServices.scoreContent(
                platform = platform,
                contentType = format,
                caption = caption,
                description = desc,
                rawHashtags = tags,
                niche = niche,
                followerCount = followers[platform] ?: 1000L,
                userCalibrations = _calibrations.value
            )
        }
        return Audit(
            id = id,
            contentType = format,
            caption = caption,
            description = desc,
            rawHashtags = tags,
            niche = niche,
            targetAudience = audience,
            createdAt = date,
            results = results
        )
    }

    fun updateTier(tier: UserTier) {
        val updated = _currentUser.value.copy(tier = tier)
        _currentUser.value = updated
    }

    fun analyzeContent(
        format: ContentFormat,
        caption: String,
        description: String,
        rawHashtags: String,
        niche: String,
        targetAudience: String,
        selectedPlatforms: List<SocialPlatform>,
        followerCounts: Map<SocialPlatform, Long>
    ): Audit {
        val auditId = "aud_" + UUID.randomUUID().toString().take(8)
        val dateString = dateFormat.format(Date())

        val results = selectedPlatforms.map { platform ->
            val followers = followerCounts[platform] ?: 1000L
            PostIQServices.scoreContent(
                platform = platform,
                contentType = format,
                caption = caption,
                description = description,
                rawHashtags = rawHashtags,
                niche = niche,
                followerCount = followers,
                userCalibrations = _calibrations.value
            )
        }

        val newAudit = Audit(
            id = auditId,
            contentType = format,
            caption = caption,
            description = description,
            rawHashtags = rawHashtags,
            niche = niche,
            targetAudience = targetAudience,
            createdAt = dateString,
            results = results
        )

        _audits.value = listOf(newAudit) + _audits.value
        return newAudit
    }

    fun logPerformance(
        auditId: String,
        platform: SocialPlatform,
        views: Long,
        likes: Long,
        comments: Long,
        shares: Long,
        saves: Long,
        watchTimePct: Float
    ) {
        val newLog = PerformanceLog(
            id = "log_" + UUID.randomUUID().toString().take(8),
            auditId = auditId,
            platform = platform,
            views = views,
            likes = likes,
            comments = comments,
            shares = shares,
            saves = saves,
            watchTimePct = watchTimePct,
            loggedAt = dateFormat.format(Date())
        )
        val updatedLogs = listOf(newLog) + _performanceLogs.value
        _performanceLogs.value = updatedLogs

        // Run calibration update
        recalibrateWeights(platform)
    }

    private fun recalibrateWeights(platform: SocialPlatform) {
        val relevantLogs = _performanceLogs.value.filter { it.platform == platform }
        if (relevantLogs.size < 2) return

        // Recalibrate hook & pacing based on watchTimePct or engagement
        val watchTimes = relevantLogs.map { it.watchTimePct }
        val viewCounts = relevantLogs.map { it.views.toFloat() }

        val currentCals = _calibrations.value.toMutableList()
        val dateString = dateFormat.format(Date())

        // Hook calibration
        val hookIndex = currentCals.indexOfFirst { it.platform == platform && it.dimension == "hook" }
        val newHookMult = if (watchTimes.isNotEmpty() && watchTimes.average() > 60.0) 1.25f else 1.05f
        if (hookIndex >= 0) {
            currentCals[hookIndex] = currentCals[hookIndex].copy(multiplier = newHookMult, updatedAt = dateString)
        } else {
            currentCals.add(Calibration("cal_" + UUID.randomUUID().toString().take(6), platform, "general", "hook", newHookMult, dateString))
        }

        _calibrations.value = currentCals
    }

    fun importFromLink(url: String): ImportedContent {
        val cleanUrl = url.trim().lowercase()
        return when {
            cleanUrl.contains("youtube.com") || cleanUrl.contains("youtu.be") -> {
                ImportedContent(
                    format = ContentFormat.VIDEO,
                    platform = SocialPlatform.YOUTUBE,
                    caption = "Mastering Vector Search: Why AI Needs Dedicated Databases",
                    description = "In this breakdown, we examine dense vector retrieval, approximate nearest neighbors (ANN), and latency trade-offs.\n\nSubscribe for more engineering breakdowns!\nLinks mentioned in video: github.com/vector-guide",
                    hashtags = "#ai #datastructures #coding #database #shorts",
                    niche = "technology",
                    followers = 18500L,
                    isLive = true,
                    views = 12400L,
                    likes = 890L,
                    comments = 114L,
                    watchTime = 65.4f
                )
            }
            cleanUrl.contains("tiktok.com") -> {
                ImportedContent(
                    format = ContentFormat.VIDEO,
                    platform = SocialPlatform.TIKTOK,
                    caption = "Secret iPhone settings that feel illegal to know 🤫",
                    description = "Try this right now and thank me later! Wait till the end for the best one.",
                    hashtags = "#fyp #apple #techhacks #foryou #iphonetips",
                    niche = "technology",
                    followers = 45000L,
                    isLive = true,
                    views = 68000L,
                    likes = 5400L,
                    comments = 320L,
                    watchTime = 82.1f
                )
            }
            cleanUrl.contains("instagram.com") -> {
                ImportedContent(
                    format = ContentFormat.VIDEO,
                    platform = SocialPlatform.INSTAGRAM,
                    caption = "3 Micro-Habits that doubled my focus and energy every morning",
                    description = "Which habit will you start tomorrow? Save this post to reset your routine this week.\n\nShare with someone who needs morning motivation!",
                    hashtags = "#wellness #mindset #productivity #morningroutine #explorepage",
                    niche = "lifestyle",
                    followers = 12200L,
                    isLive = true,
                    views = 15800L,
                    likes = 1450L,
                    comments = 98L,
                    watchTime = 59.8f
                )
            }
            cleanUrl.contains("linkedin.com") -> {
                ImportedContent(
                    format = ContentFormat.CAROUSEL,
                    platform = SocialPlatform.LINKEDIN,
                    caption = "Why high-performing engineering teams are moving away from daily standups",
                    description = "Synchronous status meetings disrupt deep work. Here is how asynchronous updates improved our team velocity by 35%.\n\nWhat is your team's approach to asynchronous communication?",
                    hashtags = "#leadership #engineering #productivity #remotework",
                    niche = "technology",
                    followers = 6800L,
                    isLive = true,
                    views = 8900L,
                    likes = 430L,
                    comments = 74L,
                    watchTime = 0f
                )
            }
            else -> {
                ImportedContent(
                    format = ContentFormat.TEXT,
                    platform = SocialPlatform.X,
                    caption = "The best founders don't build features faster. They delete non-essential roadmap items before writing a line of code.",
                    description = "",
                    hashtags = "#startups #founders",
                    niche = "technology",
                    followers = 5200L,
                    isLive = true,
                    views = 9800L,
                    likes = 612L,
                    comments = 45L,
                    watchTime = 0f
                )
            }
        }
    }

    fun getAuditById(id: String): Audit? {
        return _audits.value.find { it.id == id }
    }
}

data class ImportedContent(
    val format: ContentFormat,
    val platform: SocialPlatform,
    val caption: String,
    val description: String,
    val hashtags: String,
    val niche: String,
    val followers: Long,
    val isLive: Boolean,
    val views: Long,
    val likes: Long,
    val comments: Long,
    val watchTime: Float
)
