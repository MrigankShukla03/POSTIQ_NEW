package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PostIQRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
    data object Dashboard : Screen()
    data object Analyzer : Screen()
    data class Report(val auditId: String, val platform: SocialPlatform) : Screen()
    data class Compare(val auditId: String) : Screen()
    data object Calibration : Screen()
    data object Pricing : Screen()
}

class PostIQViewModel(
    val repository: PostIQRepository = PostIQRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val currentUser = repository.currentUser
    val audits = repository.audits
    val calibrations = repository.calibrations
    val performanceLogs = repository.performanceLogs

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterFormat = MutableStateFlow<ContentFormat?>(null)
    val selectedFilterFormat: StateFlow<ContentFormat?> = _selectedFilterFormat.asStateFlow()

    // Analysis Loading State
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterFormat(format: ContentFormat?) {
        _selectedFilterFormat.value = format
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun updateTier(tier: UserTier) {
        repository.updateTier(tier)
        showMessage("Upgraded account plan to ${tier.displayName}!")
    }

    fun runAnalysis(
        format: ContentFormat,
        caption: String,
        description: String,
        rawHashtags: String,
        niche: String,
        targetAudience: String,
        selectedPlatforms: List<SocialPlatform>,
        followerCounts: Map<SocialPlatform, Long>,
        isAlreadyPosted: Boolean,
        postViews: Long,
        postLikes: Long,
        postComments: Long,
        postShares: Long,
        postSaves: Long,
        postWatchTime: Float
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisProgress.value = 0.15f
            delay(350)
            _analysisProgress.value = 0.50f
            delay(300)
            _analysisProgress.value = 0.85f
            delay(250)

            val audit = repository.analyzeContent(
                format = format,
                caption = caption,
                description = description,
                rawHashtags = rawHashtags,
                niche = niche,
                targetAudience = targetAudience,
                selectedPlatforms = selectedPlatforms,
                followerCounts = followerCounts
            )

            // If already posted, log performance
            if (isAlreadyPosted) {
                val primaryPlatform = selectedPlatforms.firstOrNull() ?: SocialPlatform.YOUTUBE
                repository.logPerformance(
                    auditId = audit.id,
                    platform = primaryPlatform,
                    views = postViews,
                    likes = postLikes,
                    comments = postComments,
                    shares = postShares,
                    saves = postSaves,
                    watchTimePct = postWatchTime
                )
            }

            _analysisProgress.value = 1.0f
            delay(200)
            _isAnalyzing.value = false

            if (audit.results.size == 1) {
                navigateTo(Screen.Report(audit.id, audit.results.first().platform))
            } else {
                navigateTo(Screen.Compare(audit.id))
            }
        }
    }

    fun logPostPerformance(
        auditId: String,
        platform: SocialPlatform,
        views: Long,
        likes: Long,
        comments: Long,
        shares: Long,
        saves: Long,
        watchTime: Float
    ) {
        repository.logPerformance(
            auditId = auditId,
            platform = platform,
            views = views,
            likes = likes,
            comments = comments,
            shares = shares,
            saves = saves,
            watchTimePct = watchTime
        )
        showMessage("Logged performance metrics! Algorithmic weights calibrated.")
    }
}
