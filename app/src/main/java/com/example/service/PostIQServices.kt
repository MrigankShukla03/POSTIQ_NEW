package com.example.service

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object PostIQServices {

    // Default weights per platform
    private val platformWeights = mapOf(
        SocialPlatform.YOUTUBE to mapOf("hook" to 0.25f, "pacing" to 0.15f, "captions" to 0.15f, "hashtags" to 0.10f, "seo" to 0.25f, "cta" to 0.10f),
        SocialPlatform.INSTAGRAM to mapOf("hook" to 0.30f, "pacing" to 0.15f, "captions" to 0.20f, "hashtags" to 0.15f, "cta" to 0.10f, "interactivity" to 0.10f),
        SocialPlatform.TIKTOK to mapOf("hook" to 0.35f, "pacing" to 0.20f, "loop" to 0.15f, "captions" to 0.10f, "hashtags" to 0.10f, "cta" to 0.10f),
        SocialPlatform.X to mapOf("hook" to 0.30f, "captions" to 0.30f, "interactivity" to 0.20f, "hashtags" to 0.10f, "cta" to 0.10f),
        SocialPlatform.LINKEDIN to mapOf("hook" to 0.25f, "captions" to 0.35f, "interactivity" to 0.20f, "cta" to 0.10f, "hashtags" to 0.10f),
        SocialPlatform.FACEBOOK to mapOf("hook" to 0.25f, "captions" to 0.25f, "pacing" to 0.15f, "cta" to 0.15f, "hashtags" to 0.10f, "interactivity" to 0.10f),
        SocialPlatform.THREADS to mapOf("hook" to 0.30f, "captions" to 0.35f, "interactivity" to 0.20f, "cta" to 0.10f, "hashtags" to 0.05f)
    )

    fun scoreContent(
        platform: SocialPlatform,
        contentType: ContentFormat,
        caption: String,
        description: String,
        rawHashtags: String,
        niche: String,
        followerCount: Long,
        userCalibrations: List<Calibration>
    ): PlatformReport {
        val findings = mutableListOf<Finding>()
        val dimensionScores = mutableMapOf<String, Int>()

        val baseWeights = platformWeights[platform] ?: mapOf("hook" to 0.3f, "captions" to 0.4f, "cta" to 0.3f)
        for (dim in baseWeights.keys) {
            dimensionScores[dim] = 100
        }

        // Evaluate platform rules
        val cleanCaption = caption.trim()
        val cleanDesc = description.trim()
        val extractedTags = extractTags(cleanCaption, cleanDesc, rawHashtags)

        when (platform) {
            SocialPlatform.YOUTUBE -> {
                // Title length
                if (cleanCaption.length < 15 || cleanCaption.length > 60) {
                    applyPenalty(dimensionScores, "seo", 20)
                    findings.add(
                        Finding(
                            id = "yt_title_length",
                            dimension = "seo",
                            severity = Severity.MEDIUM,
                            message = "Title length (${cleanCaption.length} chars) is outside the 15–60 char sweet spot.",
                            suggestion = "Keep YouTube titles between 20 and 60 characters to avoid truncation in mobile search and suggested feeds."
                        )
                    )
                }
                // Description SEO
                val descWords = cleanDesc.split("\\s+".toRegex()).count { it.isNotEmpty() }
                if (descWords < 20) {
                    applyPenalty(dimensionScores, "seo", 25)
                    findings.add(
                        Finding(
                            id = "yt_desc_seo",
                            dimension = "seo",
                            severity = Severity.HIGH,
                            message = "Description contains only $descWords words, missing search keywords.",
                            suggestion = "Provide at least 30–100 words with target search queries, timestamps, and chapters to index in YouTube Search."
                        )
                    )
                }
                // Hook check
                if (cleanCaption.length < 10) {
                    applyPenalty(dimensionScores, "hook", 25)
                    findings.add(
                        Finding(
                            id = "yt_hook",
                            dimension = "hook",
                            severity = Severity.HIGH,
                            message = "Weak initial title hook.",
                            suggestion = "Place an immediate emotional or curiosity hook in the first 30 characters (e.g. 'How I...', 'Stop doing this!')."
                        )
                    )
                }
                // CTA check
                val ctaKeywords = listOf("subscribe", "sub", "link", "channel", "join", "below", "comment")
                val combined = "$cleanCaption $cleanDesc".lowercase()
                if (ctaKeywords.none { combined.contains(it) }) {
                    applyPenalty(dimensionScores, "cta", 20)
                    findings.add(
                        Finding(
                            id = "yt_cta",
                            dimension = "cta",
                            severity = Severity.MEDIUM,
                            message = "No subscription or engagement call-to-action found.",
                            suggestion = "Include an explicit CTA in your description prompting viewers to subscribe or check video links."
                        )
                    )
                }
                // Hashtag count
                if (extractedTags.isEmpty() || extractedTags.size > 5) {
                    applyPenalty(dimensionScores, "hashtags", 15)
                    findings.add(
                        Finding(
                            id = "yt_hashtags",
                            dimension = "hashtags",
                            severity = Severity.MEDIUM,
                            message = "Found ${extractedTags.size} tags. YouTube recommends exactly 3 focused hashtags.",
                            suggestion = "Use 3 relevant hashtags above the title or in the first line of description."
                        )
                    )
                }
            }

            SocialPlatform.INSTAGRAM -> {
                // First line hook
                val firstLine = cleanCaption.lines().firstOrNull() ?: ""
                if (firstLine.length > 125) {
                    applyPenalty(dimensionScores, "hook", 20)
                    findings.add(
                        Finding(
                            id = "ig_first_line",
                            dimension = "hook",
                            severity = Severity.MEDIUM,
                            message = "First line is too long (${firstLine.length} chars) and will get cut off before the '...more' fold.",
                            suggestion = "Keep the first line under 90 characters with a punchy teaser to compel users to tap '...more'."
                        )
                    )
                }
                // Line breaks
                val newlineCount = cleanCaption.count { it == '\n' }
                val wordCount = cleanCaption.split("\\s+".toRegex()).count { it.isNotEmpty() }
                if (wordCount > 30 && newlineCount < 2) {
                    applyPenalty(dimensionScores, "captions", 20)
                    findings.add(
                        Finding(
                            id = "ig_line_breaks",
                            dimension = "captions",
                            severity = Severity.MEDIUM,
                            message = "Caption is a dense wall of text lacking skimmable paragraph breaks.",
                            suggestion = "Add double line-breaks every 1–2 sentences for visual breathing room on mobile feeds."
                        )
                    )
                }
                // Hashtag count & mix
                if (extractedTags.size < 4 || extractedTags.size > 12) {
                    applyPenalty(dimensionScores, "hashtags", 20)
                    findings.add(
                        Finding(
                            id = "ig_tags",
                            dimension = "hashtags",
                            severity = Severity.MEDIUM,
                            message = "Tag count (${extractedTags.size}) is outside recommended 5–10 range.",
                            suggestion = "Instagram algorithm recommends 5–8 targeted hashtags balancing broad (#fitness) and micro-community (#hiitworkoutsathome)."
                        )
                    )
                }
                // CTA check
                val igCta = listOf("save", "share", "comment", "link in bio", "dm", "follow", "below")
                if (igCta.none { cleanCaption.lowercase().contains(it) }) {
                    applyPenalty(dimensionScores, "cta", 20)
                    findings.add(
                        Finding(
                            id = "ig_cta",
                            dimension = "cta",
                            severity = Severity.MEDIUM,
                            message = "Missing high-value engagement CTA (Save / Share).",
                            suggestion = "Prompt viewers to 'Save this post for later' or 'Share with a friend'—Saves and Shares are Instagram's #1 ranking signals."
                        )
                    )
                }
            }

            SocialPlatform.TIKTOK -> {
                // Short caption constraint
                if (cleanCaption.length > 150) {
                    applyPenalty(dimensionScores, "captions", 20)
                    findings.add(
                        Finding(
                            id = "tt_caption_length",
                            dimension = "captions",
                            severity = Severity.MEDIUM,
                            message = "Caption length (${cleanCaption.length} chars) covers too much screen area on TikTok FYP.",
                            suggestion = "Keep captions concise (under 100 characters) so they don't obstruct on-screen video elements."
                        )
                    )
                }
                // FYP / Trending tag check
                val hasFyp = extractedTags.any { it.contains("fyp") || it.contains("foryou") || it.contains("viral") }
                if (!hasFyp && extractedTags.size < 3) {
                    applyPenalty(dimensionScores, "hashtags", 15)
                    findings.add(
                        Finding(
                            id = "tt_tags",
                            dimension = "hashtags",
                            severity = Severity.LOW,
                            message = "Missing discovery & niche hashtag blend.",
                            suggestion = "Include 4–6 tags combining 1 category tag, 2 niche keywords, and 1 community challenge tag."
                        )
                    )
                }
                // Loop hint
                if (!cleanCaption.contains("wait till the end", ignoreCase = true) && !cleanCaption.contains("loop", ignoreCase = true)) {
                    applyPenalty(dimensionScores, "loop", 10)
                }
            }

            SocialPlatform.X -> {
                // Length check
                if (cleanCaption.length > 280) {
                    applyPenalty(dimensionScores, "captions", 30)
                    findings.add(
                        Finding(
                            id = "x_length",
                            dimension = "captions",
                            severity = Severity.HIGH,
                            message = "Post exceeds 280 characters (${cleanCaption.length} chars).",
                            suggestion = "Format as a threaded post (e.g. '1/5') or trim to 280 characters to allow quoting and reposts."
                        )
                    )
                }
                // Outbound links check
                val hasUrl = cleanCaption.contains("http://") || cleanCaption.contains("https://") || cleanCaption.contains(".com/")
                if (hasUrl) {
                    applyPenalty(dimensionScores, "interactivity", 25)
                    findings.add(
                        Finding(
                            id = "x_outbound_link",
                            dimension = "interactivity",
                            severity = Severity.HIGH,
                            message = "Direct outbound link in post body reduces algorithmic impression distribution.",
                            suggestion = "Post links in the first reply thread or invite users to DM for the link to avoid reach suppression."
                        )
                    )
                }
                // Hashtag saturation
                if (extractedTags.size > 2) {
                    applyPenalty(dimensionScores, "hashtags", 20)
                    findings.add(
                        Finding(
                            id = "x_tags",
                            dimension = "hashtags",
                            severity = Severity.MEDIUM,
                            message = "Too many hashtags (${extractedTags.size}). X algorithm penalizes hashtag clutter.",
                            suggestion = "Use 1 or at most 2 hashtags on X, or integrate keywords directly into natural sentences."
                        )
                    )
                }
            }

            SocialPlatform.LINKEDIN -> {
                // Professional tone check
                val slang = listOf("slay", "lit", "fire", "bruh", "fr fr", "no cap", "wig", "tea", "yolo")
                if (slang.any { cleanCaption.lowercase().contains(it) }) {
                    applyPenalty(dimensionScores, "captions", 25)
                    findings.add(
                        Finding(
                            id = "li_tone",
                            dimension = "captions",
                            severity = Severity.HIGH,
                            message = "Casual slang detected which harms professional authority in B2B feeds.",
                            suggestion = "Refactor colloquial terms into industry-standard business and leadership terminology."
                        )
                    )
                }
                // Sales keyword check
                val salesWords = listOf("buy now", "discount", "coupon", "limited time sale", "order today", "cheap")
                if (salesWords.any { cleanCaption.lowercase().contains(it) }) {
                    applyPenalty(dimensionScores, "captions", 25)
                    findings.add(
                        Finding(
                            id = "li_salesy",
                            dimension = "captions",
                            severity = Severity.HIGH,
                            message = "Direct hard-sell sales language detected.",
                            suggestion = "Frame your post around insights, case studies, or business lessons rather than promotional advertising."
                        )
                    )
                }
                // Line formatting
                if (!cleanCaption.contains("\n\n") && cleanCaption.length > 100) {
                    applyPenalty(dimensionScores, "hook", 15)
                    findings.add(
                        Finding(
                            id = "li_breaks",
                            dimension = "hook",
                            severity = Severity.MEDIUM,
                            message = "Missing paragraph breaks. The LinkedIn algorithm measures 'dwell time' which drops on unformatted blocks.",
                            suggestion = "Use concise 1–2 sentence paragraphs with empty lines in between."
                        )
                    )
                }
                // Hashtags
                if (extractedTags.size < 3 || extractedTags.size > 5) {
                    applyPenalty(dimensionScores, "hashtags", 15)
                    findings.add(
                        Finding(
                            id = "li_hashtags",
                            dimension = "hashtags",
                            severity = Severity.LOW,
                            message = "Tag count (${extractedTags.size}) is outside 3–5 professional standard.",
                            suggestion = "Include 3–4 targeted professional tags (e.g. #marketing #careers #leadership)."
                        )
                    )
                }
            }

            SocialPlatform.FACEBOOK -> {
                val fbWords = cleanCaption.split("\\s+".toRegex()).count { it.isNotEmpty() }
                if (fbWords < 10) {
                    applyPenalty(dimensionScores, "hook", 20)
                    findings.add(
                        Finding(
                            id = "fb_hook",
                            dimension = "hook",
                            severity = Severity.MEDIUM,
                            message = "Short or vague opening text.",
                            suggestion = "Add a context-rich hook sentence explaining why your community or family should read/watch."
                        )
                    )
                }
                if (extractedTags.size > 5) {
                    applyPenalty(dimensionScores, "hashtags", 15)
                    findings.add(
                        Finding(
                            id = "fb_tags",
                            dimension = "hashtags",
                            severity = Severity.LOW,
                            message = "Excessive hashtags on Facebook reduce readability.",
                            suggestion = "Keep Facebook posts to 2–4 broad community tags."
                        )
                    )
                }
            }

            SocialPlatform.THREADS -> {
                if (cleanCaption.length > 500) {
                    applyPenalty(dimensionScores, "captions", 25)
                    findings.add(
                        Finding(
                            id = "th_length",
                            dimension = "captions",
                            severity = Severity.MEDIUM,
                            message = "Text exceeds single Threads post length (500 chars).",
                            suggestion = "Condense main insight or format as a carousel of replies."
                        )
                    )
                }
                if (extractedTags.size > 1) {
                    applyPenalty(dimensionScores, "hashtags", 15)
                    findings.add(
                        Finding(
                            id = "th_tags",
                            dimension = "hashtags",
                            severity = Severity.LOW,
                            message = "Threads recommends single tag or inline hashtag topic.",
                            suggestion = "Use a single focused hashtag topic tag on Threads."
                        )
                    )
                }
            }
        }

        // Apply calibrations
        val adjustedWeights = baseWeights.toMutableMap()
        var weightSum = 0f
        for (dim in adjustedWeights.keys) {
            val cal = userCalibrations.find { it.platform == platform && it.niche.equals(niche, ignoreCase = true) && it.dimension.equals(dim, ignoreCase = true) }
            val mult = cal?.multiplier ?: 1.0f
            val w = adjustedWeights[dim]!! * mult
            adjustedWeights[dim] = w
            weightSum += w
        }
        for (dim in adjustedWeights.keys) {
            adjustedWeights[dim] = adjustedWeights[dim]!! / (if (weightSum > 0f) weightSum else 1.0f)
        }

        // Compute overall score
        var rawScore = 0f
        for ((dim, score) in dimensionScores) {
            val weight = adjustedWeights[dim] ?: (1f / dimensionScores.size)
            rawScore += score * weight
        }
        val scoreOverall = min(100, max(15, rawScore.roundToInt()))

        // Adapt caption & hashtags
        val (adaptedCaption, adaptedHashtags) = adaptContent(platform, cleanCaption, extractedTags, niche)

        // Estimated views calculation
        val estimatedViews = calculateEstimatedViews(platform, contentType, followerCount, scoreOverall)

        // AI expert feedback
        val aiFeedback = generateAiFeedback(platform, contentType, cleanCaption, cleanDesc, niche, followerCount, scoreOverall)

        return PlatformReport(
            platform = platform,
            scoreOverall = scoreOverall,
            scoresBreakdown = dimensionScores,
            findings = findings,
            adaptedCaption = adaptedCaption,
            adaptedHashtags = adaptedHashtags,
            aiFeedback = aiFeedback,
            estimatedViews = estimatedViews,
            followerCount = followerCount
        )
    }

    private fun applyPenalty(scores: MutableMap<String, Int>, dimension: String, penalty: Int) {
        val current = scores[dimension] ?: 100
        scores[dimension] = max(10, current - penalty)
    }

    private fun extractTags(caption: String, description: String, rawHashtags: String): List<String> {
        val list = mutableListOf<String>()
        val regex = "#([A-Za-z0-9_-]+)".toRegex()
        regex.findAll("$caption $description").forEach { match ->
            list.add(match.groupValues[1].lowercase())
        }
        if (rawHashtags.isNotEmpty()) {
            rawHashtags.split("[\\s,#]+".toRegex()).forEach {
                val clean = it.trim().removePrefix("#").lowercase()
                if (clean.isNotEmpty()) list.add(clean)
            }
        }
        return list.distinct()
    }

    private fun adaptContent(
        platform: SocialPlatform,
        caption: String,
        tags: List<String>,
        niche: String
    ): Pair<String, String> {
        val baseCaption = if (caption.isNotBlank()) caption else "Discover the ultimate insights for $niche"
        val cleanTags = tags.toMutableList()

        return when (platform) {
            SocialPlatform.YOUTUBE -> {
                val title = if (baseCaption.length > 55) baseCaption.take(52) + "..." else baseCaption
                val ytTags = cleanTags.take(3).ifEmpty { listOf("shorts", niche.lowercase(), "creator") }
                title to ytTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.INSTAGRAM -> {
                val hooked = "🔥 STOP SCROLLING: $baseCaption\n\n📌 Save this for when you need it later!\n💬 Share your thoughts below."
                val igTags = (cleanTags + listOf("explorepage", "viralreels", niche.lowercase(), "contentstrategy")).distinct().take(8)
                hooked to igTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.TIKTOK -> {
                val short = if (baseCaption.length > 90) baseCaption.take(87) + "..." else baseCaption
                val ttTags = (cleanTags + listOf("fyp", "foryou", niche.lowercase(), "tiktokviral")).distinct().take(5)
                short to ttTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.X -> {
                val trimmed = if (baseCaption.length > 240) baseCaption.take(237) + "..." else baseCaption
                val xTags = cleanTags.take(1).ifEmpty { listOf(niche.lowercase()) }
                trimmed to xTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.LINKEDIN -> {
                val liBody = "🚀 Key Takeaway for Professionals:\n\n$baseCaption\n\nHow is your organization approaching this? Let's discuss in the comments."
                val liTags = (cleanTags + listOf("business", "leadership", "innovation", niche.lowercase())).distinct().take(4)
                liBody to liTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.FACEBOOK -> {
                val fb = "📢 Important Update:\n\n$baseCaption\n\nTag a friend who needs to see this today!"
                val fbTags = (cleanTags + listOf(niche.lowercase(), "trending")).distinct().take(3)
                fb to fbTags.joinToString(" ") { "#$it" }
            }
            SocialPlatform.THREADS -> {
                val th = if (baseCaption.length > 450) baseCaption.take(447) + "..." else baseCaption
                val thTags = cleanTags.take(1).ifEmpty { listOf(niche.lowercase()) }
                th to thTags.joinToString(" ") { "#$it" }
            }
        }
    }

    private fun calculateEstimatedViews(
        platform: SocialPlatform,
        contentType: ContentFormat,
        followerCount: Long,
        score: Int
    ): Long {
        val basePct = when (platform) {
            SocialPlatform.YOUTUBE -> 0.15f
            SocialPlatform.TIKTOK -> 0.22f
            SocialPlatform.INSTAGRAM -> 0.09f
            SocialPlatform.FACEBOOK -> 0.11f
            SocialPlatform.X -> 0.06f
            SocialPlatform.LINKEDIN -> 0.12f
            SocialPlatform.THREADS -> 0.07f
        }
        val formatBonus = if (contentType == ContentFormat.VIDEO) 1.5f else 1.0f
        val qualityFactor = (score / 75.0f).coerceIn(0.4f, 2.2f)

        val baselineViews = when (platform) {
            SocialPlatform.TIKTOK -> 250L
            SocialPlatform.YOUTUBE -> 180L
            SocialPlatform.INSTAGRAM -> 120L
            else -> 45L
        }

        val organicReach = (followerCount * basePct * formatBonus * qualityFactor).toLong()
        return max(baselineViews, organicReach + baselineViews)
    }

    private fun generateAiFeedback(
        platform: SocialPlatform,
        contentType: ContentFormat,
        caption: String,
        description: String,
        niche: String,
        followers: Long,
        score: Int
    ): String {
        val snippet = if (caption.isNotBlank()) caption.take(40) else "your draft"
        val coldStartAdvice = if (followers < 1000) {
            """
            |#### 📢 Cold-Start Distribution Protocol (< 1k Audience)
            |* **The Seed Bubble**: With ${followers} followers, ${platform.displayName}'s algorithm tests your post on a micro-group of 20–50 viewers before triggering recommendation feed placement.
            |* **Immediate Action Plan**:
            |  * Directly share the link with high-engagement close contacts within 15 minutes of posting.
            |  * Post in topical WhatsApp/Telegram communities or niche Reddit/Discord threads related to **$niche**.
            |  * Pin a high-curiosity comment asking a direct binary question (e.g., 'A or B?') to prompt early comment velocity.
            """.trimMargin()
        } else {
            """
            |#### 📈 Growth & Distribution Analysis
            |* With an existing audience of **${String.format("%,d", followers)}**, initial impressions will validate engagement velocity within the first 60 minutes.
            |* Strong retention in the initial seed wave will promote this piece to explore tabs and recommendation feeds.
            """.trimMargin()
        }

        return """
        |### 🧠 PostIQ Algorithm Audit for **${platform.displayName.uppercase()}**
        |
        |#### 1. Algorithmic Trend & Feed Dynamics
        |* **Current Distribution Paradigm**: On ${platform.displayName}, content in the **$niche** category is evaluated on completion rate, repeat views, and prompt saves.
        |* **Quality Rating**: Your content scored **$score/100**, placing it in the **${if (score >= 80) "Elite Viral Tier" else if (score >= 60) "High Reach Tier" else "Standard Baseline"}**.
        |
        |$coldStartAdvice
        |
        |#### 2. Visual Hook & First-Frame Architecture
        |* Current opening concept: *“$snippet...”*
        |* **Recommended 3-Second Hook Patterns**:
        |  * **Pattern Interrupt**: Rapid dynamic zoom-in within the first 0.8 seconds with bold sans-serif text overlay.
        |  * **Curiosity Gap**: “The one $niche mistake costing you hours (and how to fix it in 3 steps).”
        |  * **Contrarian Angle**: “Why conventional advice about $niche is completely backwards.”
        |
        |#### 3. Copywriting & High-Conversion Redrafts
        |* **Option A (Punchy & Direct)**:
        |  * *“Stop struggling with $niche. Here are 3 game-changing rules to implement today 🧵👇”*
        |* **Option B (Narrative Storytelling)**:
        |  * *“I tested this for 30 days so you don't have to. The results will surprise you...”*
        |
        |#### 4. Pacing & Audio Directives
        |* ${if (contentType == ContentFormat.VIDEO) "Keep cuts under 2.2 seconds. Add subtle whoosh sound effects on key text callouts." else "Ensure high-contrast imagery with bright focal points that stand out against dark mode feeds."}
        |
        |#### 5. Search & SEO Keyword Strategy
        |Add these 5 high-intent keywords in your description to capture organic search traffic:
        |`#${niche.lowercase()}` `#contentstrategy` `#algorithm` `#growthhacks` `#viral${platform.id}`
        """.trimMargin()
    }

    // Pearson Correlation for Feedback Loop Calibration
    fun calculateCorrelation(x: List<Float>, y: List<Float>): Float {
        val n = min(x.size, y.size)
        if (n < 2) return 0f

        val sumX = x.take(n).sum()
        val sumY = y.take(n).sum()
        val meanX = sumX / n
        val meanY = sumY / n

        var num = 0f
        var denX = 0f
        var denY = 0f

        for (i in 0 until n) {
            val diffX = x[i] - meanX
            val diffY = y[i] - meanY
            num += diffX * diffY
            denX += diffX * diffX
            denY += diffY * diffY
        }

        val denominator = sqrt((denX * denY).toDouble()).toFloat()
        return if (denominator == 0f) 0f else num / denominator
    }
}
