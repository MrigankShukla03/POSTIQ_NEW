package com.example.model

enum class UserTier(val displayName: String, val price: String, val maxPlatforms: Int) {
    FREE("Free Starter", "$0", 1),
    CREATOR("Creator Pro", "$29/mo", 7),
    PRO("Agency Elite", "$99/mo", 7)
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val tier: UserTier,
    val createdAt: String
)

enum class ContentFormat(val displayName: String, val iconName: String) {
    VIDEO("Video / Reel / Short", "video"),
    IMAGE("Single Image", "image"),
    CAROUSEL("Multi-Slide Carousel", "carousel"),
    TEXT("Text / Thought Post", "text")
}

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val defaultMaxChars: Int,
    val recommendedTags: Int
) {
    YOUTUBE("youtube", "YouTube", "Shorts & Long-form", 100, 3),
    INSTAGRAM("instagram", "Instagram", "Reels, Carousels & Feed", 2200, 7),
    TIKTOK("tiktok", "TikTok", "Short-form & FYP", 300, 5),
    X("x", "X / Twitter", "Posts & Threads", 280, 2),
    LINKEDIN("linkedin", "LinkedIn", "Articles & Professional", 3000, 4),
    FACEBOOK("facebook", "Facebook", "Reels & Page Feed", 5000, 4),
    THREADS("threads", "Threads", "Conversational Text & Media", 500, 2);

    companion object {
        fun fromId(id: String): SocialPlatform = entries.find { it.id.equals(id, ignoreCase = true) } ?: YOUTUBE
    }
}

enum class Severity {
    LOW, MEDIUM, HIGH
}

data class Finding(
    val id: String,
    val dimension: String,
    val severity: Severity,
    val message: String,
    val suggestion: String
)

data class PlatformReport(
    val platform: SocialPlatform,
    val scoreOverall: Int,
    val scoresBreakdown: Map<String, Int>,
    val findings: List<Finding>,
    val adaptedCaption: String,
    val adaptedHashtags: String,
    val aiFeedback: String,
    val estimatedViews: Long,
    val followerCount: Long
)

data class Audit(
    val id: String,
    val contentType: ContentFormat,
    val caption: String,
    val description: String,
    val rawHashtags: String,
    val niche: String,
    val targetAudience: String,
    val createdAt: String,
    val results: List<PlatformReport>
)

data class PerformanceLog(
    val id: String,
    val auditId: String,
    val platform: SocialPlatform,
    val views: Long,
    val likes: Long,
    val comments: Long,
    val shares: Long,
    val saves: Long,
    val watchTimePct: Float,
    val loggedAt: String
)

data class Calibration(
    val id: String,
    val platform: SocialPlatform,
    val niche: String,
    val dimension: String,
    val multiplier: Float,
    val updatedAt: String
)

data class TrendPoint(
    val auditId: String,
    val dateLabel: String,
    val scoreOverall: Int,
    val views: Long,
    val engagementRate: Float
)
