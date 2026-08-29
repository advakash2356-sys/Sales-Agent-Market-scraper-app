package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.LeadEntity
import com.example.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = LeadRepository(database.leadDao())

    // Tabs / Phases
    private val _currentPhase = MutableStateFlow(1) // 1 to 5
    val currentPhase: StateFlow<Int> = _currentPhase.asStateFlow()

    // Search Query state
    private val _queryNiche = MutableStateFlow("Dentist")
    val queryNiche: StateFlow<String> = _queryNiche.asStateFlow()

    private val _queryCity = MutableStateFlow("Bandra, Mumbai")
    val queryCity: StateFlow<String> = _queryCity.asStateFlow()

    private val _limitCount = MutableStateFlow(12)
    val limitCount: StateFlow<Int> = _limitCount.asStateFlow()

    // API Keys (UI editable, persistence can be done via SharedPreferences)
    private val sharedPrefs = application.getSharedPreferences("lead_to_launch_prefs", Context.MODE_PRIVATE)

    private val _apifyToken = MutableStateFlow(sharedPrefs.getString("apify_token", "") ?: "")
    val apifyToken: StateFlow<String> = _apifyToken.asStateFlow()

    private val _pageSpeedKey = MutableStateFlow(sharedPrefs.getString("pagespeed_key", "") ?: "")
    val pageSpeedKey: StateFlow<String> = _pageSpeedKey.asStateFlow()

    // Detect if keys are securely loaded into the compiled application through background environment variables
    private val _isApifySystemSecretSet = MutableStateFlow(
        try {
            com.example.BuildConfig.APIFY_API_KEY.isNotEmpty() && com.example.BuildConfig.APIFY_API_KEY != "MY_APIFY_API_KEY"
        } catch (e: Exception) {
            false
        }
    )
    val isApifySystemSecretSet: StateFlow<Boolean> = _isApifySystemSecretSet.asStateFlow()

    private val _isGeminiSystemSecretSet = MutableStateFlow(
        try {
            com.example.BuildConfig.GEMINI_API_KEY.isNotEmpty() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }
    )
    val isGeminiSystemSecretSet: StateFlow<Boolean> = _isGeminiSystemSecretSet.asStateFlow()

    /**
     * Retrieves the active Apify key. Manual entry from local SharedPreferences takes priority.
     * Otherwise, falls back to the system environment secret in compile-time configuration securely.
     */
    fun getEffectiveApifyToken(): String {
        val manualToken = _apifyToken.value.trim()
        if (manualToken.isNotEmpty()) return manualToken
        return try {
            val systemToken = com.example.BuildConfig.APIFY_API_KEY
            if (systemToken.isNotEmpty() && systemToken != "MY_APIFY_API_KEY") systemToken else ""
        } catch (e: Exception) {
            ""
        }
    }

    // Loading / Progress metrics
    private val _isScraping = MutableStateFlow(false)
    val isScraping: StateFlow<Boolean> = _isScraping.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _isGeneratingWithAi = MutableStateFlow(false)
    val isGeneratingWithAi: StateFlow<Boolean> = _isGeneratingWithAi.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 6)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun postSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    // Dynamic Leads list mapping
    val leadsList: StateFlow<List<LeadEntity>> = repository.allLeads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactively derived historical categories (leaks-free, auto-refreshing!)
    val historyFilters: StateFlow<List<Pair<String, String>>> = leadsList
        .map { list ->
            val tags = list.map { it.queryNiche to it.queryCity }.distinct()
            if (tags.isEmpty()) listOf("Dentist" to "Bandra, Mumbai") else tags
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Dentist" to "Bandra, Mumbai")
        )

    // User variable customization
    private val _userName = MutableStateFlow("Akash")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _demoLink = MutableStateFlow("https://cosmic-smiles-demo.vercel.app")
    val demoLink: StateFlow<String> = _demoLink.asStateFlow()

    private val _selectedLeadId = MutableStateFlow<Int?>(null)
    val selectedLeadId: StateFlow<Int?> = _selectedLeadId.asStateFlow()

    // The reactively selected lead
    val selectedLead: StateFlow<LeadEntity?> = combine(leadsList, _selectedLeadId) { list, selectedId ->
        if (selectedId != null) {
            list.find { it.id == selectedId }
        } else {
            list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Psychological Tracker 1: Wealth Gap Under Management (Loss Aversion Engine)
    val totalDiscoveredLeakage: StateFlow<Int> = leadsList
        .map { list -> list.sumOf { it.estimatedLostRevenue } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Psychological Tracker 2: Completion Progress (Zeigarnik Effect Gauge)
    val auditedPercentage: StateFlow<Float> = leadsList
        .map { list ->
            if (list.isEmpty()) 0f
            else {
                val auditedCount = list.count { it.pageSpeedScore != null || it.isGoogleVerified }
                auditedCount.toFloat() / list.size.toFloat()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Psychological Tracker 3: Daily Prospecting Momentum Streak (Sunk Cost Habit Loop)
    private val _momentumStreak = MutableStateFlow(sharedPrefs.getInt("momentum_streak_v2", 1))
    val momentumStreak: StateFlow<Int> = _momentumStreak.asStateFlow()

    private val _hasScrapedToday = MutableStateFlow(false)
    val hasScrapedToday: StateFlow<Boolean> = _hasScrapedToday.asStateFlow()

    fun updateHasScrapedToday() {
        val lastActivity = sharedPrefs.getLong("last_streak_activity_millis_v2", 0L)
        val now = System.currentTimeMillis()
        _hasScrapedToday.value = (lastActivity > 0 && (now - lastActivity) < 22 * 60 * 60 * 1000L)
    }

    fun recordActivityForStreak() {
        val lastActivity = sharedPrefs.getLong("last_streak_activity_millis_v2", 0L)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        val daysDiff = (now - lastActivity) / oneDayMillis
        
        when {
            lastActivity == 0L -> {
                sharedPrefs.edit().putLong("last_streak_activity_millis_v2", now).putInt("momentum_streak_v2", 1).apply()
                _momentumStreak.value = 1
            }
            daysDiff < 1L -> {
                // Same day activity, maintain current streak silently
            }
            daysDiff in 1L..2L -> {
                // Active on consecutive days (up to 48 hours for nice weekend-friendly safety buffer)
                val current = sharedPrefs.getInt("momentum_streak_v2", 1) + 1
                sharedPrefs.edit().putLong("last_streak_activity_millis_v2", now).putInt("momentum_streak_v2", current).apply()
                _momentumStreak.value = current
            }
            else -> {
                // Streak broken, auto-restart on day 1
                sharedPrefs.edit().putLong("last_streak_activity_millis_v2", now).putInt("momentum_streak_v2", 1).apply()
                _momentumStreak.value = 1
            }
        }
        updateHasScrapedToday()
    }

    init {
        updateHasScrapedToday()
        // One-time secure database preheating. Prevents key/seed resource collisions.
        viewModelScope.launch {
            val preheated = sharedPrefs.getBoolean("is_database_preheated_v2", false)
            if (!preheated) {
                _isScraping.value = true
                repository.populateDemoBandraDentists()
                sharedPrefs.edit().putBoolean("is_database_preheated_v2", true).apply()
                _isScraping.value = false
            }
        }
    }

    private fun updateHistoryTags() {
        // No-op: Obsolescent imperative emitter replaced by reactive mapped Flow for peak thread safety
    }

    fun setPhase(phase: Int) {
        if (phase in 1..5) {
            _currentPhase.value = phase
        }
    }

    fun setQueryNiche(niche: String) {
        _queryNiche.value = niche
    }

    fun setQueryCity(city: String) {
        _queryCity.value = city
    }

    fun setLimitCount(count: Int) {
        _limitCount.value = count
    }

    fun setApifyToken(token: String) {
        _apifyToken.value = token
        sharedPrefs.edit().putString("apify_token", token).apply()
    }

    fun setPageSpeedKey(key: String) {
        _pageSpeedKey.value = key
        sharedPrefs.edit().putString("pagespeed_key", key).apply()
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setDemoLink(link: String) {
        _demoLink.value = link
    }

    fun selectLead(id: Int) {
        _selectedLeadId.value = id
    }

    /**
     * Delete a single lead
     */
    fun deleteLead(lead: LeadEntity) {
        viewModelScope.launch {
            repository.deleteLead(lead)
            if (_selectedLeadId.value == lead.id) {
                _selectedLeadId.value = null
            }
        }
    }

    /**
     * Triggers either live Google Maps API crawling or the intelligent localized generator.
     */
    fun triggerScrape() {
        viewModelScope.launch {
            _isScraping.value = true
            _statusMessage.value = "Initiating Scraper..."
            
            val result = repository.scrapeLeads(
                niche = _queryNiche.value,
                city = _queryCity.value,
                limit = _limitCount.value,
                apifyToken = getEffectiveApifyToken(),
                pageSpeedKey = _pageSpeedKey.value
            )

            when (result) {
                "SUCCESS_LIVE" -> {
                    _statusMessage.value = "Successfully scraped live Google Maps leads!"
                    _snackbarMessage.emit("🎉 Lead scraping complete! Loaded live prospects for ${_queryNiche.value} in ${_queryCity.value}.")
                    recordActivityForStreak()
                    _currentPhase.value = 2 // Progress to Audit Phase after scraping
                }
                "SUCCESS_SIMULATION" -> {
                    _statusMessage.value = "Demo simulation active! Created realistic localized prospects."
                    _snackbarMessage.emit("🎉 Lead scraping complete! Generated demo prospects for ${_queryNiche.value} in ${_queryCity.value}.")
                    recordActivityForStreak()
                    _currentPhase.value = 2 // Progress to Audit Phase after scraping
                }
                else -> {
                    _statusMessage.value = "Error: $result"
                    _snackbarMessage.emit("⚠️ Scraping notice: $result")
                }
            }
            updateHistoryTags()
            _isScraping.value = false
        }
    }

    /**
     * Run website audit for the entire current filtered list of leads sequentially
     */
    fun auditAllLeads() {
        viewModelScope.launch {
            val list = leadsList.value.filter { 
                it.queryNiche.equals(_queryNiche.value, ignoreCase = true) && 
                it.queryCity.equals(_queryCity.value, ignoreCase = true) 
            }
            if (list.isEmpty()) {
                _statusMessage.value = "No leads found in this category to audit."
                return@launch
            }

            _isAuditing.value = true
            _statusMessage.value = "Auditing websites..."
            
            var completed = 0
            for (lead in list) {
                _statusMessage.value = "Auditing (${completed + 1}/${list.size}): ${lead.name}"
                repository.auditLead(lead.id, _pageSpeedKey.value)
                completed++
            }

            _statusMessage.value = "Digital audit complete for $completed sites!"
            if (completed > 0) recordActivityForStreak()
            _currentPhase.value = 3 // Move to ranking phase
            _isAuditing.value = false
        }
    }

    /**
     * Audits an individual lead website
     */
    fun auditSingleLead(leadId: Int) {
        viewModelScope.launch {
            _isAuditing.value = true
            val success = repository.auditLead(leadId, _pageSpeedKey.value)
            if (success) {
                _statusMessage.value = "Website audit completed."
                recordActivityForStreak()
            } else {
                _statusMessage.value = "Audit execution failed."
            }
            _isAuditing.value = false
        }
    }

    /**
     * Runs Google verification on a single lead using search grounding
     */
    fun verifySingleLeadOnGoogle(leadId: Int) {
        viewModelScope.launch {
            _isVerifying.value = true
            _statusMessage.value = "Initiating Google Search verification..."
            val geminiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
            val success = repository.verifyLeadOnGoogle(leadId, geminiKey)
            if (success) {
                _statusMessage.value = "Google verification completed successfully."
                recordActivityForStreak()
            } else {
                _statusMessage.value = "Google verification failed. Check networks or credentials."
            }
            _isVerifying.value = false
        }
    }

    /**
     * Runs Google verification in batch across all currently filtered leads.
     */
    fun verifyAllLeadsOnGoogle() {
        viewModelScope.launch {
            _isVerifying.value = true
            val currentFilteredList = leadsList.value.filter {
                it.queryNiche.equals(_queryNiche.value, ignoreCase = true) &&
                it.queryCity.equals(_queryCity.value, ignoreCase = true)
            }
            if (currentFilteredList.isEmpty()) {
                _statusMessage.value = "No leads found to verify."
                _isVerifying.value = false
                return@launch
            }

            var verifiedCount = 0
            val geminiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            for (lead in currentFilteredList) {
                _statusMessage.value = "Searching Google for '${lead.name}'..."
                val success = repository.verifyLeadOnGoogle(lead.id, geminiKey)
                if (success) verifiedCount++
            }

            _statusMessage.value = "Finished Google verification for $verifiedCount leads!"
            if (verifiedCount > 0) recordActivityForStreak()
            _isVerifying.value = false
        }
    }

    /**
     * Triggers Gemini 3.5 Flash to analyze the scraped business data and generate custom outreach emails
     * and website improvement prompts for a specific lead.
     */
    fun generateAiOutreachAndPrompt(leadId: Int) {
        viewModelScope.launch {
            _isGeneratingWithAi.value = true
            _statusMessage.value = "Interfacing with Gemini 3.5 Flash to synthesize custom pitches and prompt..."
            val geminiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            val success = repository.generateOutreachAndPromptsWithGemini(
                leadId = leadId,
                geminiApiKey = geminiKey,
                userName = _userName.value,
                demoLink = _demoLink.value
            )

            if (success) {
                val leadName = leadsList.value.find { it.id == leadId }?.name ?: "Business"
                _statusMessage.value = "✨ Generated custom pitches and website improvement prompt with Gemini 3.5 Flash!"
                _snackbarMessage.emit("✨ Outreach email generated successfully for $leadName!")
                recordActivityForStreak()
            } else {
                _statusMessage.value = "Gemini generation could not complete. Check networks."
                _snackbarMessage.emit("⚠️ Could not generate outreach email. Please check network/keys.")
            }
            _isGeneratingWithAi.value = false
        }
    }

    /**
     * Batch synthesizes custom outreach copy and website prompts for all leads in the current query using Gemini.
     */
    fun generateAllAiOutreachAndPrompts() {
        viewModelScope.launch {
            val currentFilteredList = leadsList.value.filter {
                it.queryNiche.equals(_queryNiche.value, ignoreCase = true) &&
                it.queryCity.equals(_queryCity.value, ignoreCase = true)
            }
            if (currentFilteredList.isEmpty()) {
                _statusMessage.value = "No leads available to process."
                _snackbarMessage.emit("No leads available in this category to process.")
                return@launch
            }

            _isGeneratingWithAi.value = true
            var processed = 0
            val geminiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            for (lead in currentFilteredList) {
                _statusMessage.value = "Gemini AI crafting pitch (${processed + 1}/${currentFilteredList.size}): ${lead.name}..."
                val success = repository.generateOutreachAndPromptsWithGemini(
                    leadId = lead.id,
                    geminiApiKey = geminiKey,
                    userName = _userName.value,
                    demoLink = _demoLink.value
                )
                if (success) processed++
            }

            _statusMessage.value = "✨ Gemini 3.5 Flash generated custom pitches and prompts for $processed businesses!"
            if (processed > 0) {
                _snackbarMessage.emit("✨ Outreach emails generated successfully for $processed businesses!")
                recordActivityForStreak()
            } else {
                _snackbarMessage.emit("⚠️ Batch generation completed with 0 updates. Check API key.")
            }
            _isGeneratingWithAi.value = false
        }
    }

    /**
     * Reload/Reset to the original Bandra Dentists dataset
     */
    fun resetToBandraDentists() {
        viewModelScope.launch {
            _isScraping.value = true
            _statusMessage.value = "Resetting core lead pipeline to Bandra Dentists dataset..."
            repository.clearAll()
            repository.populateDemoBandraDentists()
            _queryNiche.value = "Dentist"
            _queryCity.value = "Bandra, Mumbai"
            _limitCount.value = 12
            _selectedLeadId.value = null
            _currentPhase.value = 1
            _statusMessage.value = "Restored default Bandra Dentists demo dataset successfully!"
            updateHistoryTags()
            _isScraping.value = false
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}

class DashboardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
