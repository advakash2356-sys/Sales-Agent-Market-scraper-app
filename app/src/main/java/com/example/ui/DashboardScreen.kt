package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.LeadEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showOnboarding by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

    // Observe State flows from ViewModel
    val currentPhase by viewModel.currentPhase.collectAsStateWithLifecycle()
    val nicheQuery by viewModel.queryNiche.collectAsStateWithLifecycle()
    val cityQuery by viewModel.queryCity.collectAsStateWithLifecycle()
    val limitCount by viewModel.limitCount.collectAsStateWithLifecycle()
    
    val apifyToken by viewModel.apifyToken.collectAsStateWithLifecycle()
    val pageSpeedKey by viewModel.pageSpeedKey.collectAsStateWithLifecycle()
    
    val isApifySystemSecretSet by viewModel.isApifySystemSecretSet.collectAsStateWithLifecycle()
    val isGeminiSystemSecretSet by viewModel.isGeminiSystemSecretSet.collectAsStateWithLifecycle()
    
    val isScraping by viewModel.isScraping.collectAsStateWithLifecycle()
    val isAuditing by viewModel.isAuditing.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifying.collectAsStateWithLifecycle()
    val isGeneratingWithAi by viewModel.isGeneratingWithAi.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    
    val historyFilters by viewModel.historyFilters.collectAsStateWithLifecycle()
    val leadsList by viewModel.leadsList.collectAsStateWithLifecycle()
    val selectedLead by viewModel.selectedLead.collectAsStateWithLifecycle()
    
    // Core Psychological Triggers Flows
    val totalDiscoveredLeakage by viewModel.totalDiscoveredLeakage.collectAsStateWithLifecycle()
    val auditedPercentage by viewModel.auditedPercentage.collectAsStateWithLifecycle()
    val momentumStreak by viewModel.momentumStreak.collectAsStateWithLifecycle()
    val hasScrapedToday by viewModel.hasScrapedToday.collectAsStateWithLifecycle()

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val demoLink by viewModel.demoLink.collectAsStateWithLifecycle()

    var showKeysPanel by remember { mutableStateOf(false) }
    var outreachLanguageIsHinglish by remember { mutableStateOf(false) }
    var showStreakTracker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var emailModalLead by remember { mutableStateOf<LeadEntity?>(null) }
    var isEmailModalOpen by remember { mutableStateOf(false) }

    // Observe snackbar notification events (Scraping complete, AI outreach generated, Clipboard copy)
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
    }

    // Clear Toast message whenever status is updated
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    // Filter leads list relative to currently selected niche & city to prevent pollution
    val filteredLeads = remember(leadsList, nicheQuery, cityQuery) {
        leadsList.filter { 
            it.queryNiche.equals(nicheQuery, ignoreCase = true) && 
            it.queryCity.equals(cityQuery, ignoreCase = true) 
        }
    }

    // Custom deep gradient for our Cosmic Dashboard Atmosphere
    val primaryBgGradients = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    if (showOnboarding) {
        OnboardingGuideModal(onDismiss = { showOnboarding = false })
    }

    if (isEmailModalOpen && emailModalLead != null) {
        OutreachEmailModal(
            lead = emailModalLead!!,
            userName = userName,
            demoLink = demoLink,
            isHinglish = outreachLanguageIsHinglish,
            onLanguageChange = { outreachLanguageIsHinglish = it },
            onDismiss = {
                isEmailModalOpen = false
                emailModalLead = null
            },
            onCopy = { text ->
                clipboardManager.setText(AnnotatedString(text))
                viewModel.postSnackbar("📋 Outreach email copied to clipboard!")
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_dashboard_layout"),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("dashboard_snackbar_host")
            )
        },
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Brand Header with Akash Sync Corporate signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Lead → Launch",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("app_brand_title")
                        )
                        Text(
                            text = "Client Finder & Digital Audit CRM",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // Configuration Options Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showKeysPanel = !showKeysPanel },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (showKeysPanel) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("toggle_keys_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Edit API Credentials",
                                tint = if (showKeysPanel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.resetToBandraDentists() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("reset_demo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restore Default Bandra Dentists",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Expandable API Keys Configuration Card (Elegant styling)
                AnimatedVisibility(
                    visible = showKeysPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("keys_configuration_panel"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔑 API Core Configurations",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Secure Sandbox",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Informational Notice regarding Secure System Secrets Panel
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Security Note",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "To configure secure keys without storing them in files, use the 'Secrets' panel in the Google AI Studio UI. They are compiled securely into encrypted memory on device and are never kept in language model context.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // --- Apify Token Configuration ---
                            OutlinedTextField(
                                value = apifyToken,
                                onValueChange = { viewModel.setApifyToken(it) },
                                label = { Text("Apify Token (Google Maps Scraping)") },
                                placeholder = {
                                    if (isApifySystemSecretSet) {
                                        Text("🔒 Active (Using Secret Panel Key)")
                                    } else {
                                        Text("apify_api_...")
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Apify Token") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apify_token_input"),
                                singleLine = true,
                                trailingIcon = {
                                    if (apifyToken.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setApifyToken("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                }
                            )

                            if (isApifySystemSecretSet) {
                                Text(
                                    text = "🟢 Active: Workspace APIFY_API_KEY detected in secure compile environment.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // --- Google PageSpeed API Key ---
                            OutlinedTextField(
                                value = pageSpeedKey,
                                onValueChange = { viewModel.setPageSpeedKey(it) },
                                label = { Text("Google PageSpeed Insights API Key") },
                                placeholder = { Text("AIza...") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "PageSpeed Key") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pagespeed_key_input"),
                                singleLine = true,
                                trailingIcon = {
                                    if (pageSpeedKey.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setPageSpeedKey("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // --- Gemini API Key Status Row ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Gemini API",
                                        tint = if (isGeminiSystemSecretSet) Color(0xFF9C27B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gemini AI Core Secret Status",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    color = if (isGeminiSystemSecretSet) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isGeminiSystemSecretSet) "🟢 ACTIVE SECRET" else "⚪ DEFAULT AGENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGeminiSystemSecretSet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Badge indicating status
                            val isApifyActive = apifyToken.trim().startsWith("apify_api_") || isApifySystemSecretSet
                            val isPageSpeedActive = pageSpeedKey.trim().isNotEmpty() && !pageSpeedKey.contains("xxx")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isApifyActive) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isApifyActive) "Apify Live Scraper ACTIVE" else "Apify Simulator Fallback",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isPageSpeedActive) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPageSpeedActive) "PageSpeed Live API ACTIVE" else "PageSpeed Simulated Auditing",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Phase Stepper - High fidelity layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val phasesList = listOf(
                        Triple(1, "1. Scrape", Icons.Default.Place),
                        Triple(2, "2. Audit", Icons.Default.Search),
                        Triple(3, "3. Rank", Icons.Default.Star),
                        Triple(4, "4. Build", Icons.Default.Build),
                        Triple(5, "5. Contact", Icons.Default.Email)
                    )

                    phasesList.forEach { (phaseNum, label, icon) ->
                        val isSelected = currentPhase == phaseNum
                        val isPassed = currentPhase > phaseNum
                        
                        val containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            isPassed -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                        
                        val contentColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            isPassed -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            onClick = { viewModel.setPhase(phaseNum) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("stepper_tab_$phaseNum"),
                            color = containerColor,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(16.dp),
                                    tint = contentColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BrandedLegalFooter()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(primaryBgGradients)
        ) {
            // --- CRM Habit Retention & Opportunity Framing Header Banner ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Discovered Income Opportunity Pool (Loss Aversion Mechanism)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Leakage Alert",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MONETIZABLE OPPORTUNITY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "₹" + "%,d".format(totalDiscoveredLeakage) + " / mo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Right: Habit Loop Consecutive Streak (Consistency Trigger & Sunk Cost)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                            .border(
                                width = 0.7.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { showStreakTracker = !showStreakTracker }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔥 Streak: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$momentumStreak Days ▾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (showStreakTracker) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GrowthStreakTrackerWidget(
                        streakDays = momentumStreak,
                        hasScrapedToday = hasScrapedToday,
                        onClose = { showStreakTracker = false }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar (Zeigarnik Effect: Innate human urge to complete unfinished processes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Category Auditing Progress",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(auditedPercentage * 100).toInt()}% Completed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { auditedPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Top Global Loading & Status Banner for Live Async Workflows
            TopGlobalLoadingBanner(
                isScraping = isScraping,
                isGeneratingWithAi = isGeneratingWithAi,
                isAuditing = isAuditing,
                isVerifying = isVerifying,
                queryNiche = nicheQuery,
                queryCity = cityQuery
            )

            // Quick Scrape Action Component
            Spacer(modifier = Modifier.height(8.dp))
            ApifyScraperDashboardWidget(
                niche = nicheQuery,
                city = cityQuery,
                limit = limitCount,
                isScraping = isScraping,
                onNicheChange = { viewModel.setQueryNiche(it) },
                onCityChange = { viewModel.setQueryCity(it) },
                onLimitChange = { viewModel.setLimitCount(it) },
                onTriggerScrape = { 
                    viewModel.setPhase(1) 
                    viewModel.triggerScrape() 
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- SMART INTERACTIVE PIPELINE ASSISTANT (Idiot-Proof Step-by-Step Sequence) ---
            SmartGuidedPipelineWizard(
                viewModel = viewModel,
                currentPhase = currentPhase,
                leads = filteredLeads,
                selectedLead = selectedLead,
                isScraping = isScraping,
                isAuditing = isScraping || isAuditing,
                onNavigateToPhase = { viewModel.setPhase(it) }
            )

            // The main dynamic body of the Applet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentPhase,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "PhaseLayout"
                ) { phase ->
                when (phase) {
                    1 -> PhaseScrape(
                        viewModel = viewModel,
                        niche = nicheQuery,
                        city = cityQuery,
                        limit = limitCount,
                        leads = filteredLeads,
                        isScraping = isScraping,
                        historyFilters = historyFilters
                    )
                    2 -> PhaseAudit(
                        viewModel = viewModel,
                        leads = filteredLeads,
                        isAuditing = isAuditing,
                        isVerifying = isVerifying,
                        niche = nicheQuery,
                        city = cityQuery
                    )
                    3 -> PhaseRank(
                        viewModel = viewModel,
                        leads = filteredLeads,
                        selectedLead = selectedLead
                    )
                    4 -> PhaseBuild(
                        viewModel = viewModel,
                        selectedLead = selectedLead,
                        demoUrl = demoLink,
                        isGeneratingWithAi = isGeneratingWithAi
                    )
                    5 -> PhaseOutreach(
                        viewModel = viewModel,
                        selectedLead = selectedLead,
                        userName = userName,
                        demoLink = demoLink,
                        isHinglish = outreachLanguageIsHinglish,
                        onLanguageChange = { outreachLanguageIsHinglish = it },
                        isGeneratingWithAi = isGeneratingWithAi,
                        onOpenEmailModal = { lead ->
                            emailModalLead = lead
                            isEmailModalOpen = true
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
fun GrowthStreakTrackerWidget(
    streakDays: Int,
    hasScrapedToday: Boolean,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_streak")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (hasScrapedToday) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .testTag("growth_streak_tracker_panel"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (hasScrapedToday) 0.3f else 0.25f)
        ),
        border = BorderStroke(
            if (hasScrapedToday) 2.dp else 1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = if (hasScrapedToday) 0.8f else 0.25f)
        ),
        shape = RoundedCornerShape(12.dp)

    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 Growth Streak Tracker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasScrapedToday) "SECURED TODAY" else "ACTION PENDING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse Tracker",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Loss Aversion + Sunk Cost psychology trigger explanation
            Text(
                text = if (hasScrapedToday) {
                    "Superb! You secured today's streak! Each consecutive day you search for local prospects, you maintain momentum and guard ₹%d in leakage.".format(streakDays * 45000)
                } else {
                    "Keep the fire burning! Just 1 scraping query today preserves your history, safeguards your momentum, and locks in consistency."
                },
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7-day cyclical habit slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lastCompletedIndex = (streakDays + if (hasScrapedToday) 0 else -1) % 7
                for (dayIndex in 0 until 7) {
                    val dayNum = dayIndex + 1
                    val isPastActive = dayIndex < lastCompletedIndex
                    val isCurrentDay = dayIndex == lastCompletedIndex && hasScrapedToday
                    val isTodayPending = dayIndex == lastCompletedIndex && !hasScrapedToday
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    color = when {
                                        isPastActive || isCurrentDay -> MaterialTheme.colorScheme.primary
                                        isTodayPending -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                                .border(
                                    width = if (isTodayPending || isCurrentDay) 1.5.dp else 0.5.dp,
                                    color = when {
                                        isPastActive || isCurrentDay -> MaterialTheme.colorScheme.primary
                                        isTodayPending -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPastActive || isCurrentDay) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Day $dayNum Active",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 10.sp,
                                    fontWeight = if (isTodayPending) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTodayPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Day $dayNum",
                            fontSize = 8.sp,
                            fontWeight = if (isCurrentDay || isTodayPending) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCurrentDay -> MaterialTheme.colorScheme.primary
                                isTodayPending -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Habit Commitment Anchor Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val percentageStr = if (streakDays > 1) "94%" else "78%"
                Text(
                    text = "Gamified Commitment Loop: Users with $streakDays+ active days are $percentageStr more likely to lock-in client sales proposals. Success follows consistency.",
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// PHASE 1: SCRAPE BOARD
// ==========================================
@Composable
fun PhaseScrape(
    viewModel: DashboardViewModel,
    niche: String,
    city: String,
    limit: Int,
    leads: List<LeadEntity>,
    isScraping: Boolean,
    historyFilters: List<Pair<String, String>>
) {
    var viewMode by remember { mutableStateOf("table") }
    var searchQuery by remember { mutableStateOf("") }
    
    val displayedLeads = remember(leads, searchQuery) {
        if (searchQuery.isBlank()) {
            leads
        } else {
            leads.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.website.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val csvHeader = "Business Name,Website,Phone Number,City,Niche,Page Speed Score,SSL Valid,Google Verified,Conversion Score\n"
                    val csvRows = displayedLeads.joinToString("\n") { lead ->
                        val n = lead.name.replace(",", " ")
                        val w = lead.website.ifEmpty { "N/A" }
                        val p = lead.phone.replace(",", " ").ifEmpty { "N/A" }
                        val lcity = lead.queryCity.replace(",", " ")
                        val lniche = lead.queryNiche.replace(",", " ")
                        val pageSpeed = lead.pageSpeedScore?.toString() ?: "N/A"
                        val ssl = lead.hasSsl.toString()
                        val verified = lead.isGoogleVerified.toString()
                        val score = lead.conversionScore.toString()
                        "\"$n\",\"$w\",\"$p\",\"$lcity\",\"$lniche\",$pageSpeed,$ssl,$verified,$score"
                    }
                    outputStream.write((csvHeader + csvRows).toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "CSV exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_scrape_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Discovery",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phase 1: Local Lead Discovery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Discover hidden local businesses on Google Maps. Check their details and audit them.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            ApifyScraperDashboardWidget(
                niche = niche,
                city = city,
                limit = limit,
                isScraping = isScraping,
                onNicheChange = { viewModel.setQueryNiche(it) },
                onCityChange = { viewModel.setQueryCity(it) },
                onLimitChange = { viewModel.setLimitCount(it) },
                onTriggerScrape = { viewModel.triggerScrape() }
            )
        }

        // Historial Dropdown tag list
        if (historyFilters.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "📜 Recent Discovery Searches",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        historyFilters.forEach { (hNiche, hCity) ->
                            val isMatch = hNiche.equals(niche, ignoreCase = true) && hCity.equals(city, ignoreCase = true)
                            AssistChip(
                                onClick = {
                                    viewModel.setQueryNiche(hNiche)
                                    viewModel.setQueryCity(hCity)
                                },
                                label = { Text("$hNiche in $hCity") },
                                border = if (isMatch) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else AssistChipDefaults.assistChipBorder(true),
                                modifier = Modifier.testTag("history_tag_${hNiche}_${hCity}")
                            )
                        }
                    }
                }
            }
        }

        // Summary Cards Section
        if (displayedLeads.isNotEmpty()) {
            item {
                val totalLeadsCount = displayedLeads.size
                val highPotentialCount = displayedLeads.count { it.conversionScore >= 60 || (it.pageSpeedScore != null && it.pageSpeedScore < 60) }
                val activeCampaignsCount = displayedLeads.count { it.generatedPrompt.isNotBlank() || it.emailSubject.isNotBlank() || it.whatsappEnglish.isNotBlank() }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCardItem(
                        title = "Total Leads Found",
                        value = totalLeadsCount.toString(),
                        icon = Icons.Default.Groups,
                        color = MaterialTheme.colorScheme.primary,
                        tooltipText = "Total number of businesses scraped dynamically from local search results."
                    )
                    SummaryCardItem(
                        title = "High Potential Clients",
                        value = highPotentialCount.toString(),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50),
                        tooltipText = "Businesses with low mobile speed scores or no SSL—prime targets for your pitch."
                    )
                    SummaryCardItem(
                        title = "Active Campaigns",
                        value = activeCampaignsCount.toString(),
                        icon = Icons.Default.Campaign,
                        color = MaterialTheme.colorScheme.tertiary,
                        tooltipText = "Number of personalized outreach messages generated using AI."
                    )
                }
                
                PipelineBarChart(displayedLeads)
            }
        }

        // Search Results List Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 Prospects Found in Database (${displayedLeads.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (displayedLeads.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Table", "Cards").forEach { mode ->
                                val active = (mode == "Table" && viewMode == "table") || (mode == "Cards" && viewMode == "cards")
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { viewMode = if (mode == "Table") "table" else "cards" }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("toggle_view_${mode.lowercase()}")
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (leads.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).height(50.dp),
                            placeholder = { Text("Search by name or website...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        
                        Button(
                            onClick = {
                                exportLauncher.launch("Leads_Export.csv")
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Download CSV",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (displayedLeads.isNotEmpty() && viewMode == "table") {
                    Text(
                        text = "Swipe horizontally to inspect all table columns on smaller displays.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Live Scraping Progress Tracker and Skeleton Placeholders
        if (isScraping) {
            item {
                ApifyScrapingProgressTracker(niche = niche, city = city)
            }
            items(3) {
                ScrapedBusinessSkeletonCard()
            }
        }

        if (displayedLeads.isEmpty() && !isScraping) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (leads.isEmpty()) "🔒 Historical Leads Secure but Filtered" else "🔍 No Matching Leads Found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (leads.isEmpty()) "Changing the Niche or Location parameters above filters your view dynamically. Your previously scraped leads are fully safe in your encrypted local database! Tap on any 'Recent Discovery Searches' chip below to load them instantly." else "Try adjusting your search query. The leads you are looking for might exist in the database but do not match the current search filters.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else if (viewMode == "table" && displayedLeads.isNotEmpty()) {
            item {
                ScrapedBusinessTable(
                    leads = displayedLeads,
                    onSelect = { lead -> viewModel.selectLead(lead.id) },
                    onDelete = { lead -> viewModel.deleteLead(lead) }
                )
            }
        } else if (displayedLeads.isNotEmpty()) {
            items(displayedLeads, key = { it.id }) { lead ->
                LeadHorizontalCard(lead = lead, isSelected = false, onSelect = { viewModel.selectLead(lead.id) }, onDelete = { viewModel.deleteLead(lead) })
            }
        }
    }
}

// ==========================================
// PHASE 2: AUDIT BOARD
// ==========================================
@Composable
fun PhaseAudit(
    viewModel: DashboardViewModel,
    leads: List<LeadEntity>,
    isAuditing: Boolean,
    isVerifying: Boolean,
    niche: String,
    city: String
) {
    // 1. CALCULATE AUDIT & VERIFICATION STATISTICS FOR DASHBOARD
    val totalCount = leads.size
    val auditedCount = leads.count { it.pageSpeedScore != null }
    val verifiedCount = leads.count { it.isGoogleVerified }
    
    // Gap counts (among audited leads)
    val slowSpeedLeaks = leads.count { it.pageSpeedScore != null && it.pageSpeedScore!! < 50 }
    val insecureSslCerts = leads.count { it.pageSpeedScore != null && !it.hasSsl }
    val brokenMobileViews = leads.count { it.pageSpeedScore != null && !it.isMobileFriendly }
    val mapsDeactivated = leads.count { it.pageSpeedScore != null && !it.hasGoogleMyBusinessActive }
    val missingContactForms = leads.count { it.pageSpeedScore != null && !it.hasContactForm }
    val missingSocialLinks = leads.count { it.pageSpeedScore != null && !it.hasSocialMediaLinked }
    
    // Google search correctness counts
    val matchSuccessCount = leads.count { it.isGoogleVerified && it.googleVerificationStatus == "Verified Match" }
    val mismatchCount = leads.count { it.isGoogleVerified && it.googleVerificationStatus == "Verified Flagged (Mismatch)" }
    val failedVerifyCount = leads.count { it.isGoogleVerified && it.googleVerificationStatus == "Verification Failed (Not Found)" }
    
    val scrapeValidationAccuracy = if (verifiedCount == 0) 100 else ((matchSuccessCount.toFloat() / verifiedCount) * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_audit_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PHASE TITLE AND INTRO CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Audit Center",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phase 2: Digital Audit & Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Analyze performance & core technical gaps while cross-verifying lead veracity live with Google Search indices.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- CORE REGULATORY AUDIT & SEARCH VALIDATION INTEGRITY DASHBOARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Audit & Scrape Verification Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculates data veracity in $city. Outbox integrity falls to zero if first scraped listings are stale or invalid.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Main KPIs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Scraped
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Scraped Files", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        // Audited
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Audited SEO", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$auditedCount/$totalCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        // Google Verified
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Google Verified", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$verifiedCount/$totalCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF9C27B0))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrape Accuracy Rating (Verification progress)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                verifiedCount == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                scrapeValidationAccuracy >= 85 -> Color(0xFFE8F5E9).copy(alpha = 0.6f)
                                scrapeValidationAccuracy >= 60 -> Color(0xFFFFF3E0).copy(alpha = 0.6f)
                                else -> Color(0xFFFFEBEE).copy(alpha = 0.6f)
                            }
                        ),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = when {
                                verifiedCount == 0 -> MaterialTheme.colorScheme.outlineVariant
                                scrapeValidationAccuracy >= 85 -> Color(0xFF81C784)
                                scrapeValidationAccuracy >= 60 -> Color(0xFFFFB74D)
                                else -> Color(0xFFE57373)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scrape Authenticity Accuracy Index",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (verifiedCount == 0) "WAITING DATA" else "$scrapeValidationAccuracy% MATCH RATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        verifiedCount == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                        scrapeValidationAccuracy >= 85 -> Color(0xFF2E7D32)
                                        scrapeValidationAccuracy >= 60 -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { if (totalCount == 0) 0f else (verifiedCount.toFloat() / totalCount) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF9C27B0),
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Verification Progress: $verifiedCount of $totalCount properties cross-referenced on live Web index.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (mismatchCount > 0 || failedVerifyCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Mismatch warning",
                                        tint = if (failedVerifyCount > 0) Color(0xFFC62828) else Color(0xFFE65100),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Warning: Scraped database has flagged $mismatchCount mismatch/es and $failedVerifyCount invalid dead contact registries.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (failedVerifyCount > 0) Color(0xFFC62828) else Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Exactly audited gaps analytics
                    Text(
                        text = "🔎 Technical Deficiencies Discovered (Among Audited)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Mobile friendly layout errors: $brokenMobileViews sites", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("• No secure booking form contact: $missingContactForms sites", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("• Slow and lagging load screens (>5s): $slowSpeedLeaks sites", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Non-secure HTTP alert: $insecureSslCerts sites", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("• Google Maps profile unverified: $mapsDeactivated status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("• Missing social links: $missingSocialLinks profiles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Dashboard Control Center Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.auditAllLeads() },
                            enabled = !isAuditing && leads.isNotEmpty(),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("batch_audit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isAuditing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 1.5.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auditing...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SEO Audit Gaps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.verifyAllLeadsOnGoogle() },
                            enabled = !isVerifying && leads.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("batch_verify_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Searching...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify Google Indexes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "📊 Live Diagnostic Board",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (leads.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("empty_audit_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No prospects to verify",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please discover local contacts on Step 1 first. We will bring you back here to audit their performance once found.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.setPhase(1) },
                            modifier = Modifier.height(36.dp).testTag("go_to_step_1_button")
                        ) {
                            Text("➡️ Go to Step 1: Discover Clients", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(leads, key = { it.id }) { lead ->
                DiagnosticCard(
                    lead = lead,
                    onAuditSingle = { viewModel.auditSingleLead(lead.id) },
                    onVerifySingle = { viewModel.verifySingleLeadOnGoogle(lead.id) },
                    isAuditing = isAuditing,
                    isVerifying = isVerifying
                )
            }
        }
    }
}

// ==========================================
// PHASE 3: RANK BOARD
// ==========================================
@Composable
fun PhaseRank(
    viewModel: DashboardViewModel,
    leads: List<LeadEntity>,
    selectedLead: LeadEntity?
) {
    val sortedList = remember(leads) { leads.sortedByDescending { it.conversionScore } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_rank_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rank",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phase 3: Conversion Priority",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "We rank prospects based on system value vulnerability (lowest security, speeds, and missing website = highest priority).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (leads.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("empty_rank_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No prospects ranked yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Discover and audit contacts, and our AI pipeline will prioritize them here based on vulnerability score.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.setPhase(1) },
                            modifier = Modifier.height(36.dp).testTag("go_to_step_1_button_rank")
                        ) {
                            Text("➡️ Go to Step 1: Discover Clients", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Sort by conversionScore in Kotlin just to be 100% sure although Dao does it
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🏆 Focus Target Prospect",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (selectedLead != null) {
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text("Ranked Priority #${sortedList.indexOfFirst { it.id == selectedLead.id } + 1}") }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (selectedLead != null) {
                            Text(
                                text = selectedLead.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "🌐 ${selectedLead.website.ifEmpty { "No Website" }}  •  📞 ${selectedLead.phone.ifEmpty { "N/A" }}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Conversion Vulnerability", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${selectedLead.conversionScore}/100", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFF5722))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Estimated Business Loss", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹" + "%,d".format(selectedLead.estimatedLostRevenue) + "/mo", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFE91E63))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Root Gap: ${selectedLead.biggestGapOneLiner}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Select a business from the board below to set focus target and generate outreach copies.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 Leads Scoring List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap heart to select focus",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(sortedList, key = { it.id }) { lead ->
                val isSelected = selectedLead?.id == lead.id
                LeadHorizontalCard(
                    lead = lead,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectLead(lead.id) },
                    onDelete = { viewModel.deleteLead(lead) }
                )
            }
        }
    }
}

// ==========================================
// PHASE 4: BUILD (SITE PROMPT GENERATOR)
// ==========================================
@Composable
fun PhaseBuild(
    viewModel: DashboardViewModel,
    selectedLead: LeadEntity?,
    demoUrl: String,
    isGeneratingWithAi: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_build_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Build",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phase 4: Site-Builder Prompt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Copy the custom dynamic prompt below to Lovable.dev, Bolt.new, or Claude Code to build a responsive site design in seconds.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedLead == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Fokus target selected.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Please go back to Phase 3: Rank and select a business as focus target to generate bespoke prompt profiles.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            if (isGeneratingWithAi) {
                item {
                    GeminiPromptGenerationSkeleton()
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 Dynamic Lovable / Bolt Prompt for ${selectedLead.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                // AI Regenerate button with Gemini
                                OutlinedButton(
                                    onClick = { viewModel.generateAiOutreachAndPrompt(selectedLead.id) },
                                    enabled = !isGeneratingWithAi,
                                    modifier = Modifier.testTag("regenerate_prompt_with_ai_button")
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF9C27B0))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("✨ Gemini 3.5 AI", fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = "This prompt injects specific local parameters, niche services, and fixes the exact mobile gaps discovered during audit.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Displaying Prompt
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState())
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = selectedLead.generatedPrompt,
                                    style = LocalTextStyle.current.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(selectedLead.generatedPrompt))
                                    Toast.makeText(context, "Copied prompt build configuration to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("copy_prompt_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Code-Builder Prompt")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🚀 Enter Deployed Demo URL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Once you generate and build the demo site in Lovable or Bolt, deploy it (free with 1 click to Netlify/Vercel) and paste the link below. It will inject directly into Phase 5 outreach copies!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = demoUrl,
                            onValueChange = { viewModel.setDemoLink(it) },
                            label = { Text("Deployed Demo Link") },
                            placeholder = { Text("https://example-demo.vercel.app") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("demo_link_input"),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PHASE 5: OUTREACH
// ==========================================
@Composable
fun PhaseOutreach(
    viewModel: DashboardViewModel,
    selectedLead: LeadEntity?,
    userName: String,
    demoLink: String,
    isHinglish: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    isGeneratingWithAi: Boolean = false,
    onOpenEmailModal: (LeadEntity) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_outreach_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Outreach",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Phase 5: Smart Client Outreach",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Draft personalized high-conversion WhatsApp scripts and email proposals instantly styled.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedLead == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Fokus target selected.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Please go back to Phase 3: Rank and select a business as focus target to compile outreach folders.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Gemini AI Generation Action Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Gemini AI",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini 3.5 Flash Outreach AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6A1B9A)
                                )
                            }

                            if (isGeneratingWithAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }

                        Text(
                            text = "Generates value-first cold email hooks, Indian Hinglish scripts, and website improvement prompts tailored to ${selectedLead.name}'s verified gaps.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateAiOutreachAndPrompt(selectedLead.id) },
                                enabled = !isGeneratingWithAi,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gemini_generate_single_lead_button")
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGeneratingWithAi) "Generating..." else "✨ AI Craft Pitch",
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.generateAllAiOutreachAndPrompts() },
                                enabled = !isGeneratingWithAi,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gemini_generate_all_leads_button")
                            ) {
                                Text(
                                    text = "✨ Batch All Leads",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                        }
                    }
                }
            }

            // Variable Inputs card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✍️ Personalize Script Tokens",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { viewModel.setUserName(it) },
                                label = { Text("Your Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("user_name_input")
                            )

                            OutlinedTextField(
                                value = demoLink,
                                onValueChange = { viewModel.setDemoLink(it) },
                                label = { Text("Demo Site Link") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("outreach_demo_link_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Language Toggle Tab Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Select Language Pitch Style:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isHinglish) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { onLanguageChange(false) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🇬🇧 English",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isHinglish) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isHinglish) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { onLanguageChange(true) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🇮🇳 Hinglish",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHinglish) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isGeneratingWithAi) {
                item {
                    GeminiOutreachGenerationSkeleton()
                }
            }

            // Generate pitch representations
            val whatsappBody = if (isHinglish) {
                selectedLead.whatsappHinglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink)
            } else {
                selectedLead.whatsappEnglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink)
            }

            val emailBody = if (isHinglish) {
                selectedLead.emailBodyHinglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink).replace("[Your Agency Name / Akash Sync agent]", "Akash Sync agent")
            } else {
                selectedLead.emailBodyEnglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink).replace("[Your Agency Name / Akash Sync agent]", "Akash Sync agent")
            }

            val followUpBody = if (isHinglish) {
                selectedLead.followUpHinglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink)
            } else {
                selectedLead.followUpEnglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink)
            }

            // 1. WhatsApp Fold
            item {
                OutreachBlock(
                    title = "📱 Rapid WhatsApp Hook",
                    body = whatsappBody,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(whatsappBody))
                        Toast.makeText(context, "WhatsApp pitch copied!", Toast.LENGTH_SHORT).show()
                    },
                    tag = "whatsapp"
                )
            }

            // 2. Email Fold with Expandable Card & Modal View
            item {
                ExpandableOutreachEmailCard(
                    leadName = selectedLead.name,
                    subject = selectedLead.emailSubject,
                    body = emailBody,
                    recipientEmail = selectedLead.website,
                    onCopyEmail = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        viewModel.postSnackbar("📋 Outreach email copied to clipboard!")
                        Toast.makeText(context, "Subject + Email pitch copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    onCopySubject = { subj ->
                        clipboardManager.setText(AnnotatedString(subj))
                        viewModel.postSnackbar("📋 Email subject copied to clipboard!")
                        Toast.makeText(context, "Subject copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    onOpenModal = {
                        onOpenEmailModal(selectedLead)
                    }
                )
            }

            // 3. Day-3 Follow-up Fold
            item {
                OutreachBlock(
                    title = "⏰ Day-3 Follow-Up Draft",
                    body = followUpBody,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(followUpBody))
                        Toast.makeText(context, "Follow-up pitch copied!", Toast.LENGTH_SHORT).show()
                    },
                    tag = "follow_up"
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

@Composable
fun LeadHorizontalCard(
    lead: LeadEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("lead_card_${lead.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, 
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lead.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (lead.isDemo) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("DEMO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    Text(
                        text = lead.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Conversion Score indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    lead.conversionScore >= 80 -> Color(0xFFFFEBEE)
                                    lead.conversionScore >= 50 -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE8F5E9)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${lead.conversionScore} Val",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                lead.conversionScore >= 80 -> Color(0xFFC62828)
                                lead.conversionScore >= 50 -> Color(0xFFEF6C00)
                                else -> Color(0xFF2E7D32)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_lead_${lead.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Lead", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gaps preview bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pageSpeed = lead.pageSpeedScore
                    if (pageSpeed != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        pageSpeed >= 90 -> Color(0xFFE8F5E9)
                                        pageSpeed >= 50 -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ Speed: $pageSpeed", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    pageSpeed >= 90 -> Color(0xFF2E7D32)
                                    pageSpeed >= 50 -> Color(0xFFEF6C00)
                                    else -> Color(0xFFC62828)
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ Speed: --", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Checks
                    CheckIndicator(label = "SSL", passed = lead.hasSsl)
                    CheckIndicator(label = "Mobile", passed = lead.isMobileFriendly)
                    CheckIndicator(label = "Web", passed = lead.website.isNotEmpty())
                }

                if (lead.estimatedLostRevenue > 0) {
                    Text(
                        text = "Leak: ₹" + "%,d".format(lead.estimatedLostRevenue) + "/mo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }
            }
        }
    }
}

@Composable
fun DiagnosticCard(
    lead: LeadEntity,
    onAuditSingle: () -> Unit,
    onVerifySingle: () -> Unit,
    isAuditing: Boolean,
    isVerifying: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diagnostic_card_${lead.id}"),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = lead.website.ifEmpty { "No Website Domain Linked" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAuditSingle,
                    enabled = !isAuditing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("audit_lead_${lead.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Detailed Diagnostic Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left metrics: Speed representation
                Column(modifier = Modifier.weight(1f)) {
                    Text("Performance score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val score = lead.pageSpeedScore
                        val scoreText = score?.toString() ?: "--"
                        val scoreColor = when {
                            score == null -> MaterialTheme.colorScheme.onSurfaceVariant
                            score >= 90 -> Color(0xFF4CAF50)
                            score >= 50 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }

                        Text(
                            text = scoreText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(scoreColor)
                            )
                            Text(
                                text = "Index: ${lead.speedLoadTime}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right checklists: Elements gaps
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DiagnosticCheckRow(label = "SSL Security Protocol", passed = lead.hasSsl)
                    DiagnosticCheckRow(label = "Mobile Optimization", passed = lead.isMobileFriendly)
                    DiagnosticCheckRow(label = "Google Maps Profile", passed = lead.hasGoogleMyBusinessActive)
                    DiagnosticCheckRow(label = "Customer Contact Form", passed = lead.hasContactForm)
                    DiagnosticCheckRow(label = "Social Media Hyperlinks", passed = lead.hasSocialMediaLinked)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🚨 Audit Catch: ${lead.biggestGapOneLiner}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // --- Google search validation strip ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when (lead.googleVerificationStatus) {
                            "Verified Match" -> Color(0xFFE8F5E9).copy(alpha = 0.6f)
                            "Verified Flagged (Mismatch)" -> Color(0xFFFFF3E0).copy(alpha = 0.6f)
                            "Verification Failed (Not Found)" -> Color(0xFFFFEBEE).copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = when (lead.googleVerificationStatus) {
                            "Verified Match" -> Color(0xFF81C784)
                            "Verified Flagged (Mismatch)" -> Color(0xFFFFB74D)
                            "Verification Failed (Not Found)" -> Color(0xFFE57373)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Verification",
                            tint = when (lead.googleVerificationStatus) {
                                "Verified Match" -> Color(0xFF2E7D32)
                                "Verified Flagged (Mismatch)" -> Color(0xFFE65100)
                                "Verification Failed (Not Found)" -> Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Google Search Verification",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Progress or status badge
                    Surface(
                        color = when (lead.googleVerificationStatus) {
                            "Verified Match" -> Color(0xFF2E7D32)
                            "Verified Flagged (Mismatch)" -> Color(0xFFE65100)
                            "Verification Failed (Not Found)" -> Color(0xFFC62828)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = lead.googleVerificationStatus.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lead.googleVerificationStatus == "Unverified") MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!lead.isGoogleVerified) {
                    Text(
                        text = "Scrape correctness is unverified. Running an automated Google verification performs a background query to inspect active web registries for legitimacy.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onVerifySingle,
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("verify_search_${lead.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Querying Google...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify with Google Search Grounding", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Show report
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Grounding Integrity Match: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${lead.googleConfidenceScore}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    lead.googleConfidenceScore >= 85 -> Color(0xFF2E7D32)
                                    lead.googleConfidenceScore >= 60 -> Color(0xFFE65100)
                                    else -> Color(0xFFC62828)
                                }
                            )
                        }

                        LinearProgressIndicator(
                            progress = { lead.googleConfidenceScore.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = when {
                                lead.googleConfidenceScore >= 85 -> Color(0xFF2E7D32)
                                lead.googleConfidenceScore >= 60 -> Color(0xFFE65100)
                                else -> Color(0xFFC62828)
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Google Grounding Report:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lead.googleVerificationNotes,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // If mismatch, highlight differences beautifully
                        if (lead.googleVerificationStatus == "Verified Flagged (Mismatch)") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Detected Discrepancies:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (lead.phone != lead.verifiedPhone && lead.verifiedPhone.isNotEmpty()) {
                                        Text("• Phone mismatch: Scraped [${lead.phone}] vs Google Search [${lead.verifiedPhone}]", fontSize = 9.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                    if (lead.website != lead.verifiedWebsite && lead.verifiedWebsite.isNotEmpty()) {
                                        Text("• Website mismatch: Scraped [${lead.website}] vs Google Search [${lead.verifiedWebsite}]", fontSize = 9.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Re-verify button
                        OutlinedButton(
                            onClick = onVerifySingle,
                            enabled = !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-verify Grounding", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutreachBlock(
    title: String,
    subject: String? = null,
    body: String,
    onCopy: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outreach_block_$tag"),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("copy_outreach_$tag")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copy text", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (subject != null) {
                Text(
                    text = "Subject: $subject",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = body,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ExpandableOutreachEmailCard(
    leadName: String,
    subject: String,
    body: String,
    recipientEmail: String = "",
    onCopyEmail: (String) -> Unit,
    onCopySubject: (String) -> Unit,
    onOpenModal: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expandable_outreach_email_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and expand/collapse trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .testTag("email_card_header_toggle"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📧 Outreach Email Proposal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tailored pitch for $leadName",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "EXPANDED" else "COLLAPSED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.testTag("expand_collapse_email_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse email" else "Expand email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Subject Line Section
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Subject:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = subject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { onCopySubject(subject) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_subject_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy Subject",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expandable Body & Actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = body,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(12.dp)
                                .testTag("email_body_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val composite = "Subject: $subject\n\n$body"
                                onCopyEmail(composite)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_email_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Full Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenModal,
                            modifier = Modifier
                                .testTag("open_email_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Modal View", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Direct email client intent button
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:")
                                if (recipientEmail.isNotEmpty()) {
                                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                                }
                                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                                putExtra(android.content.Intent.EXTRA_TEXT, body)
                            }
                            try {
                                context.startActivity(android.content.Intent.createChooser(intent, "Send Email via..."))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_email_client_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (recipientEmail.isNotEmpty()) "Send to $recipientEmail" else "Launch Mail Client",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Collapsed teaser preview
            AnimatedVisibility(
                visible = !isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = body.take(120).replace("\n", " ") + "...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap to expand and copy email draft 👆",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun OutreachEmailModal(
    lead: LeadEntity,
    userName: String,
    demoLink: String,
    isHinglish: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    val context = LocalContext.current
    val emailSubject = lead.emailSubject
    val emailBody = if (isHinglish) {
        lead.emailBodyHinglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink).replace("[Your Agency Name / Akash Sync agent]", "Akash Sync agent")
    } else {
        lead.emailBodyEnglish.replace("[Your Name]", userName).replace("[your-demo-link]", demoLink).replace("[Your Agency Name / Akash Sync agent]", "Akash Sync agent")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outreach_email_modal"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Email Pitch Preview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lead.name,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close modal",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Language Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pitch Language:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Surface(
                            color = if (!isHinglish) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onLanguageChange(false) }
                        ) {
                            Text(
                                text = "🇬🇧 English",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isHinglish) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = if (isHinglish) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onLanguageChange(true) }
                        ) {
                            Text(
                                text = "🇮🇳 Hinglish",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHinglish) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Recipient Info
                if (lead.phone.isNotEmpty() || lead.website.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (lead.phone.isNotEmpty()) {
                                Text("📞 ${lead.phone}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (lead.website.isNotEmpty()) {
                                Text("🌐 ${lead.website}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                            }
                        }
                    }
                }

                // Subject Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SUBJECT LINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = emailSubject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Email Body Display
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = emailBody,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val composite = "Subject: $emailSubject\n\n$emailBody"
                    onCopy(composite)
                },
                modifier = Modifier.testTag("modal_copy_email_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Email to Clipboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(android.content.Intent.EXTRA_TEXT, emailBody)
                    }
                    try {
                        context.startActivity(android.content.Intent.createChooser(intent, "Send Email via..."))
                    } catch (e: Exception) {
                        Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("modal_send_email_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Mail App", fontSize = 11.5.sp)
            }
        }
    )
}

@Composable
fun CheckIndicator(label: String, passed: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label, 
                fontSize = 9.sp, 
                color = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828), 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DiagnosticCheckRow(label: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun BrandedLegalFooter() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF4CAF50))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AKASH SYNC AGENT ENGINE CONNECTED",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Regulatory Compliance Notice: This CRM operates under strict sandboxed prospecting guidelines. Contact details parsed from maps registries are subject to TRAI UCC Regulations (2018) and DND registers. Outboxes must comply with Sections 43A & 72A of the Indian IT Act (2000). Prior consent is mandatory before commercial outreach representations. By using this application, you agree strictly to the Akash Sync Data Usage Policy & Google Maps API terms of service. For demonstration purposes only.",
            fontSize = 9.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.96f)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "© 2026 Akash Sync Corp. Powered by Antigravity Agent. All rights reserved.",
            fontSize = 8.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun TooltippedTableHeaderCell(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    width: androidx.compose.ui.unit.Dp,
    tooltip: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .width(width)
            .clickable { Toast.makeText(context, tooltip, Toast.LENGTH_LONG).show() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
fun ScrapedBusinessTable(
    leads: List<LeadEntity>,
    onSelect: (LeadEntity) -> Unit,
    onDelete: (LeadEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scraped_leads_table"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            // Horizontal scroll wrapper to handle different screen sizes perfectly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.widthIn(min = 650.dp)
                ) {
                    // --- HEADER ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TooltippedTableHeaderCell(
                            text = "BUSINESS NAME",
                            icon = Icons.Default.Place,
                            width = 220.dp,
                            tooltip = "The official registered name of the business found on Google maps."
                        )

                        TooltippedTableHeaderCell(
                            text = "WEBSITE",
                            icon = Icons.Default.Info,
                            width = 180.dp,
                            tooltip = "The business's domain. Blank means they desperately need your services."
                        )

                        TooltippedTableHeaderCell(
                            text = "RANKING STATUS",
                            icon = Icons.Default.Star,
                            width = 150.dp,
                            tooltip = "Shows SSL Security status and Google mapping claims."
                        )

                        // Col 4: Actions (right-aligned, width 100.dp)
                        Box(
                            modifier = Modifier.width(100.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "ACTIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // --- DATA ROWS ---
                    leads.forEachIndexed { index, lead ->
                        val isAlternating = index % 2 == 1
                        val rowBg = if (isAlternating) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        } else {
                            Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable { onSelect(lead) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Col 1: Business name
                            Column(
                                modifier = Modifier.width(220.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = lead.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    if (lead.isDemo) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "DEMO",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = {
                                        if (lead.address.length > 32) lead.address.take(30) + "..." else lead.address
                                    }(),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            // Col 2: Website
                            Box(
                                modifier = Modifier.width(180.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (lead.website.isNotEmpty()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Website Link",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = lead.website.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(22),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No Website",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Col 3: Current ranking status
                            Box(
                                modifier = Modifier.width(150.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val pageSpeed = lead.pageSpeedScore
                                if (pageSpeed != null) {
                                    Surface(
                                        color = when {
                                            pageSpeed >= 90 -> Color(0xFFE8F5E9)
                                            pageSpeed >= 50 -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFFFEBEE)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Speed: $pageSpeed / Val: ${lead.conversionScore}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                pageSpeed >= 90 -> Color(0xFF2E7D32)
                                                pageSpeed >= 50 -> Color(0xFFEF6C00)
                                                else -> Color(0xFFC62828)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Pending Audit (Val: ${lead.conversionScore})",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Col 4: Actions
                            Box(
                                modifier = Modifier.width(100.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onSelect(lead) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Select Lead",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDelete(lead) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Lead",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartGuidedPipelineWizard(
    viewModel: DashboardViewModel,
    currentPhase: Int,
    leads: List<LeadEntity>,
    selectedLead: LeadEntity?,
    isScraping: Boolean,
    isAuditing: Boolean,
    onNavigateToPhase: (Int) -> Unit
) {
    val totalLeads = leads.size
    val auditedLeads = leads.count { it.pageSpeedScore != null }

    // Automatic next best logical action determination
    val recommendedNextPhase = when {
        totalLeads == 0 -> 1
        currentPhase == 1 && totalLeads > 0 -> 2
        currentPhase == 2 && auditedLeads < totalLeads && !isAuditing -> 2
        currentPhase == 2 && auditedLeads > 0 && selectedLead == null -> 3
        currentPhase == 3 && selectedLead == null -> 3
        currentPhase == 3 && selectedLead != null -> 4
        currentPhase == 4 && selectedLead != null -> 5
        else -> currentPhase
    }

    // Infinite breathing transition for the high-impact glowing autopilot button
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("smart_guided_pipeline_wizard"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Co-pilot Title & Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SALES AUTOMATION COPILOT",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live Process Guard Active",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "100% IDIOT-PROOF SEQ",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator Bubbles (Progress Map)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 1..5) {
                    val isCurrent = currentPhase == step
                    val isPast = currentPhase > step
                    val isRecommended = recommendedNextPhase == step

                    val stepColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isPast -> Color(0xFF4CAF50)
                        isRecommended -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val textColor = when {
                        isCurrent -> MaterialTheme.colorScheme.onPrimary
                        isPast -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToPhase(step) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(stepColor)
                                .border(
                                    width = if (isRecommended) 2.5.dp else 1.dp,
                                    color = if (isRecommended) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPast) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = textColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = when (step) {
                                        1 -> "A"
                                        2 -> "B"
                                        3 -> "C"
                                        4 -> "D"
                                        else -> "E"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when(step) {
                                1 -> "A: Discover"
                                2 -> "B: Audit"
                                3 -> "C: Choose"
                                4 -> "D: Demo"
                                else -> "E: Close"
                            },
                            fontSize = 8.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    if (step < 5) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(
                                    if (currentPhase > step) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Co-Pilot Tip Guidance Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 0.75.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                val stepTitle = when (recommendedNextPhase) {
                    1 -> "STEP A: SCRAPE TARGET CLIENTS"
                    2 -> "STEP B: ASSESS THEIR GAPS (SEO SPEED)"
                    3 -> "STEP C: CHOOSE YOUR TARGET CLIENT"
                    4 -> "STEP D: ACTIVATE PERSONALIZED DEMO"
                    5 -> "STEP E: INITIATE MASSIVE CONVERSIONS OUTREACH"
                    else -> "PROCESS CONCLUDED"
                }

                val stepInstructions = when {
                    totalLeads == 0 -> "Your Lead database in the current niche is blank. Type in your local niche & location parameter above, then press the pulsing button to scrape and discover target business websites automatically."
                    recommendedNextPhase == 2 && currentPhase != 2 -> "Superb! $totalLeads prospects discovered! Moving to Step B (Audit) is recommended to run active speed checks."
                    recommendedNextPhase == 2 && isAuditing -> "The Speed Scanner is active. Estimating page response times and client money loss dynamic metrics... Please wait."
                    recommendedNextPhase == 2 -> "Run SEO and speed audits. Spot response delays, missing links, or map defects to prove lost patient/client income."
                    recommendedNextPhase == 3 && currentPhase != 3 -> "Audits finished! Double tap Step C (Profile) to selection."
                    recommendedNextPhase == 3 -> "Select a slow prospect from the spreadsheet list below. Tip: Pick one flagged with speed flaws to trigger immediate client curiosity!"
                    recommendedNextPhase == 4 && currentPhase != 4 -> "Lead locked in! Go to Step D (Demo) to configure their campaign preview site."
                    recommendedNextPhase == 4 -> "Click the autopilot button below to automatically spin up a gorgeous campaign demo site demonstrating their exact loading flaws."
                    recommendedNextPhase == 5 && currentPhase != 5 -> "Campaign URL generated! Jump to Step E (Outreach) to begin corresponding."
                    recommendedNextPhase == 5 -> "Closing templates configured! Simply hit WhatsApp or draft Email to launch dynamic proposals showing exactly how much they save."
                    else -> "All steps completed! Maintain consistency to secure your steak."
                }

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Guidance Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stepTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stepInstructions,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- THE GLOWING AUTOPILOT ACTION BUTTON (IDIOT-PROOF SINGLE TRIGGER) ---
            val isButtonDisabled = (recommendedNextPhase == 2 && isAuditing) || isScraping

            Button(
                onClick = {
                    when {
                        // Case A: No leads, need to discover
                        totalLeads == 0 -> {
                            if (currentPhase != 1) {
                                onNavigateToPhase(1)
                            } else {
                                viewModel.triggerScrape()
                            }
                        }
                        // Case B: Has leads, some have no speed audits
                        recommendedNextPhase == 2 -> {
                            if (currentPhase != 2) {
                                onNavigateToPhase(2)
                            } else {
                                viewModel.auditAllLeads()
                            }
                        }
                        // Case C: Audits done, need selection
                        recommendedNextPhase == 3 -> {
                            if (currentPhase != 3) {
                                onNavigateToPhase(3)
                            } else {
                                val slowestLead = leads.minByOrNull { it.pageSpeedScore ?: 100 }
                                slowestLead?.let { viewModel.selectLead(it.id) }
                            }
                        }
                        // Case D: Chosen, need demo URL
                        recommendedNextPhase == 4 -> {
                            if (currentPhase != 4) {
                                onNavigateToPhase(4)
                            } else {
                                val customDemo = "https://aistudio-client-preview.vercel.app/speed-test/${selectedLead?.id ?: 101}"
                                viewModel.setDemoLink(customDemo)
                                onNavigateToPhase(5)
                            }
                        }
                        // Case E: Ready for closing outreach
                        recommendedNextPhase == 5 -> {
                            if (currentPhase != 5) {
                                onNavigateToPhase(5)
                            } else {
                                // Navigate to outreach tab
                            }
                        }
                    }
                },
                enabled = !isButtonDisabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (!isButtonDisabled) pulseScale else 1f)
                    .height(48.dp)
                    .testTag("autopilot_magic_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonDisabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isButtonDisabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val (btnText, btnIcon) = when {
                        totalLeads == 0 -> {
                            if (currentPhase != 1) "🗺️ Go to Step A: Search Prospects" to Icons.Default.Search
                            else "🔎 Auto-Launch: Scrape Local Leads" to Icons.Default.Search
                        }
                        recommendedNextPhase == 2 -> {
                            if (currentPhase != 2) "⚙️ Navigate to Step B: Audit Flaws" to Icons.Default.ArrowForward
                            else "⚡ Auto-Scan: Launch Mobile Speed Audit" to Icons.Default.PlayArrow
                        }
                        recommendedNextPhase == 3 -> {
                            if (currentPhase != 3) "📊 Navigate to Step C: Profile Lead" to Icons.Default.ArrowForward
                            else "🎯 Auto-Select: Pick Slowest Offender" to Icons.Default.Adjust
                        }
                        recommendedNextPhase == 4 -> {
                            if (currentPhase != 4) "🎨 Navigate to Step D: Build Demo" to Icons.Default.ArrowForward
                            else "🏗️ Auto-Generator: Seed Campaign URL" to Icons.Default.CheckCircle
                        }
                        recommendedNextPhase == 5 -> {
                            if (currentPhase != 5) "✉️ Navigate to Step E: Closing Outreach" to Icons.Default.ArrowForward
                            else "🚀 Ready to Close: Click Contact Tab" to Icons.Default.Send
                        }
                        else -> "🎉 Sales Pipeline Process Concluded" to Icons.Default.Check
                    }

                    Icon(
                        imageVector = btnIcon,
                        contentDescription = "Autopilot Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = btnText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingGuideModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Instant Pipeline Guide", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Welcome to the Autobuilder! Follow this 4-step sequence to scrape and close clients:", fontSize = 13.sp)
                Text("🅰️ A: Discover - Search a niche & city to pull local prospects map data.", fontSize = 12.sp)
                Text("🅱️ B: Audit - Auto-scan pages to expose their slow load times and SSL flaws.", fontSize = 12.sp)
                Text("©️ C: Profile - Select a failing lead from the spreadsheet target list.", fontSize = 12.sp)
                Text("↩️ D: Demo - Generate a custom loss-aversion campaign preview link.", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Look for the pulsing autopilot button to guide you through these automatically!", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It! Let's Go")
            }
        }
    )
}

@Composable
fun PipelineBarChart(leads: List<LeadEntity>) {
    val total = leads.size
    if (total == 0) return
    val sslValid = leads.count { it.hasSsl }
    val sslMissing = total - sslValid
    
    val highSpeed = leads.count { (it.pageSpeedScore ?: 0) >= 80 }
    val slowSpeed = total - highSpeed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pipeline Status Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(14.dp))
            
            Text("SSL Security", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))) {
                if (sslValid > 0) Box(modifier = Modifier.weight(sslValid.toFloat()).fillMaxHeight().background(Color(0xFF4CAF50)))
                if (sslMissing > 0) Box(modifier = Modifier.weight(sslMissing.toFloat()).fillMaxHeight().background(MaterialTheme.colorScheme.error))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Secure: $sslValid", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("Vulnerable (Targets): $sslMissing", fontSize = 9.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Text("Mobile Speed Scores", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))) {
                 if (highSpeed > 0) Box(modifier = Modifier.weight(highSpeed.toFloat()).fillMaxHeight().background(Color(0xFF4CAF50)))
                 if (slowSpeed > 0) Box(modifier = Modifier.weight(slowSpeed.toFloat()).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Passing (80+): $highSpeed", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("Failing (Targets): $slowSpeed", fontSize = 9.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun ApifyScraperDashboardWidget(
    niche: String,
    city: String,
    limit: Int,
    isScraping: Boolean,
    onNicheChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onLimitChange: (Int) -> Unit,
    onTriggerScrape: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🕷️ Apify API Lead Scraper Setup",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Inputs
            OutlinedTextField(
                value = niche,
                onValueChange = onNicheChange,
                label = { Text("Business Category (e.g. Salon, Dentist, Plumber)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("niche_query_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("Target Location (e.g. Austin, Texas)") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_query_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Limit count slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lead Extraction Limit: $limit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = limit.toFloat(),
                onValueChange = { onLimitChange(it.toInt()) },
                valueRange = 5f..25f,
                steps = 4,
                modifier = Modifier.testTag("leads_limit_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrape Trigger Button
            Button(
                onClick = onTriggerScrape,
                enabled = !isScraping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scrape_action_button")
            ) {
                if (isScraping) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Apify Scraping in Progress...", fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scrape Google Maps via Apify", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isScraping) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🌐 Scraping live Google Maps coordinates, phone numbers, and web domains via Apify...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SummaryCardItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    tooltipText: String
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { Toast.makeText(context, tooltipText, Toast.LENGTH_LONG).show() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

// ==========================================
// VISUAL PROGRESS & LOADING SKELETON ENGINE
// ==========================================

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
    highlightColor: Color? = null
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val shineColor = highlightColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, shineColor, baseColor),
        start = Offset(x = translateAnim - 300f, y = translateAnim - 300f),
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun TopGlobalLoadingBanner(
    isScraping: Boolean,
    isGeneratingWithAi: Boolean,
    isAuditing: Boolean,
    isVerifying: Boolean,
    queryNiche: String,
    queryCity: String
) {
    val isAnyActive = isScraping || isGeneratingWithAi || isAuditing || isVerifying

    AnimatedVisibility(
        visible = isAnyActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val (bannerTitle, bannerSubtitle, bannerColor, bannerIcon) = when {
            isScraping -> Quadruple(
                "🕷️ Apify Crawler Active",
                "Extracting live Google Maps records for '$queryNiche' in '$queryCity'...",
                MaterialTheme.colorScheme.primary,
                Icons.Default.Place
            )
            isGeneratingWithAi -> Quadruple(
                "✨ Gemini 3.5 Flash Generating",
                "Synthesizing customized outreach pitches, Hinglish hooks, & site prompts...",
                Color(0xFF9C27B0),
                Icons.Default.Star
            )
            isAuditing -> Quadruple(
                "⚡ PageSpeed & Diagnostics",
                "Running automated mobile performance & SSL vulnerability audit...",
                Color(0xFFE65100),
                Icons.Default.Speed
            )
            else -> Quadruple(
                "🔍 Google Search Grounding",
                "Verifying business web presence with Google search grounding...",
                Color(0xFF2E7D32),
                Icons.Default.Search
            )
        }

        Surface(
            color = bannerColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("global_loading_status_banner")
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = bannerColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bannerTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = bannerColor
                        )
                    }

                    Surface(
                        color = bannerColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "LIVE TASK",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = bannerColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bannerSubtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = bannerColor,
                    trackColor = bannerColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ApifyScrapingProgressTracker(
    niche: String,
    city: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scraping_step")
    val stepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stage_anim"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apify_progress_tracker")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Apify Actor Crawler In Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Targeting local '$niche' businesses around $city on Google Maps...",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Stage Progress Visualizer
            val stages = listOf(
                "1. Initializing Cloud Crawler",
                "2. Scraping Maps Coordinates",
                "3. Parsing Phone & Websites",
                "4. Writing to Local Database"
            )

            stages.forEachIndexed { index, stageText ->
                val isCompleted = stepProgress > (index + 0.9f)
                val isCurrent = stepProgress in (index.toFloat())..(index + 0.9f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Color(0xFF4CAF50)
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stageText,
                        fontSize = 10.5.sp,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isCompleted -> Color(0xFF2E7D32)
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun ScrapedBusinessSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                ShimmerBox(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(26.dp)
                )
            }
        }
    }
}

@Composable
fun GeminiPromptGenerationSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_prompt_skeleton"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.06f)
        ),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ Gemini 3.5 AI Generating Prompt...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                }

                Surface(
                    color = Color(0xFF9C27B0).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "SYNTHESIZING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Shimmering prompt code skeleton lines
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.3f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(14.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.3f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.3f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.3f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                highlightColor = Color(0xFF9C27B0).copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
fun GeminiOutreachGenerationSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_outreach_skeleton"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.06f)
        ),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ Crafting Personalized Multi-Channel Pitches...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                }

                Surface(
                    color = Color(0xFF9C27B0).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "AI STREAMING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Shimmering Email Subject Line
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp),
                highlightColor = Color(0xFF9C27B0).copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Shimmering Cold Proposal Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(12.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.25f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(12.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.25f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.25f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(12.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.25f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Shimmering WhatsApp Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    highlightColor = Color(0xFF25D366).copy(alpha = 0.35f)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    highlightColor = Color(0xFF9C27B0).copy(alpha = 0.35f)
                )
            }
        }
    }
}



