package com.sandeep.personalfinancecompanion

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import com.sandeep.personalfinancecompanion.analytics.AnalyticsEvent
import com.sandeep.personalfinancecompanion.analytics.AnalyticsParam
import com.sandeep.personalfinancecompanion.analytics.AnalyticsTracker
import com.sandeep.personalfinancecompanion.appfunctions.AgentLifecycleManager
import com.sandeep.personalfinancecompanion.presentation.navigation.AppNavigation
import com.sandeep.personalfinancecompanion.presentation.navigation.Screen
import com.sandeep.personalfinancecompanion.presentation.navigation.bottomNavItems
import com.sandeep.personalfinancecompanion.ui.theme.PersonalFinanceCompanionTheme
import com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent
import com.sandeep.personalfinancecompanion.voiceagent.presentation.VoiceAgentDialog
import com.sandeep.personalfinancecompanion.voiceagent.presentation.VoiceAgentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var agentLifecycleManager: AgentLifecycleManager
    @Inject lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clarityConfig = ClarityConfig(
            projectId = BuildConfig.CLARITY_PROJECT_ID,
            logLevel = LogLevel.Info
        )
        Clarity.initialize(applicationContext, clarityConfig)
        
        // Apply saved language preference before UI is set
        runBlocking {
            val lang = mainViewModel.languageCode.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }

        // Triggering mainViewModel initialization to start observing and scheduling
        mainViewModel
        enableEdgeToEdge()
        setContent {
            // Register AI Agent capabilities for Android 16+ discovery
            agentLifecycleManager.registerAgentCapabilities(this)

            PersonalFinanceCompanionTheme { FinanceApp(analyticsTracker = analyticsTracker) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(analyticsTracker: AnalyticsTracker) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showVoiceAgent by remember { mutableStateOf(false) }
    val voiceViewModel: VoiceAgentViewModel = hiltViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Handle intent from SMS notification
    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        if (intent?.getBooleanExtra("FROM_SMS", false) == true) {
            val amount = intent.getDoubleExtra("EXTRA_AMOUNT", 0.0)
            val type = intent.getStringExtra("EXTRA_TYPE") ?: "EXPENSE"
            val merchant = intent.getStringExtra("EXTRA_MERCHANT") ?: ""
            
            navController.navigate(Screen.AddTransaction.createRoute(type, amount, merchant))
            intent.removeExtra("FROM_SMS") // Clear to avoid re-triggering
        }
    }

    // Show bottom bar only on main tabs
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            analyticsTracker.trackScreen(screenNameForRoute(route), route)
        }
    }

    val topBarTitle =
        when {
            currentRoute == Screen.Home.route -> stringResource(R.string.nav_home)
            currentRoute == Screen.Transactions.route -> stringResource(R.string.nav_history)
            currentRoute == Screen.Goals.route -> stringResource(R.string.nav_goals)
            currentRoute == Screen.Insights.route -> stringResource(R.string.nav_insights)
            currentRoute == Screen.Profile.route -> stringResource(R.string.nav_profile)
            currentRoute == Screen.Debt.route -> stringResource(R.string.title_lending_debts)
            currentRoute?.startsWith("add_transaction") == true -> stringResource(R.string.title_add_transaction)
            currentRoute?.startsWith("edit_transaction") == true -> stringResource(R.string.title_edit_transaction)
            else -> stringResource(R.string.app_name)
        }

    val canNavigateBack = currentRoute?.startsWith("add_transaction") == true ||
            currentRoute?.startsWith("edit_transaction") == true ||
            currentRoute == Screen.Debt.route ||
            currentRoute == Screen.Profile.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
//                                                        navController.navigate(
//                                                                Screen.Profile.route
//                                                        ) { launchSingleTop = true }
                                    analyticsTracker.trackEvent(AnalyticsEvent.PROFILE_OPENED)
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_user),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.cd_user_profile),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected =
                            navBackStackEntry?.destination?.hierarchy?.any {
                                it.route == item.route
                            } == true

                        val label = stringResource(item.labelResId)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                analyticsTracker.trackEvent(
                                    AnalyticsEvent.NAV_ITEM_SELECTED,
                                    mapOf(AnalyticsParam.ROUTE to item.route)
                                )
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector =
                                        if (isSelected) item.selectedIcon
                                        else item.unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight =
                                        if (isSelected) FontWeight.Bold
                                        else FontWeight.Normal
                                )
                            },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryAccent,
                                    selectedTextColor = PrimaryAccent,
                                    indicatorColor =
                                        PrimaryAccent.copy(alpha = 0.1f)
                                )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Voice Agent Mic FAB
                    FloatingActionButton(
                        onClick = {
                            voiceViewModel.clear()
                            analyticsTracker.trackEvent(AnalyticsEvent.VOICE_AGENT_OPENED)
                            showVoiceAgent = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.cd_voice_agent),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Existing Add Transaction FAB
                    FloatingActionButton(
                        onClick = {
                            analyticsTracker.trackEvent(
                                AnalyticsEvent.ADD_TRANSACTION_CLICKED,
                                mapOf(AnalyticsParam.SOURCE to "fab")
                            )
                            navController.navigate(Screen.AddTransaction.createRoute())
                        },
                        containerColor = PrimaryAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_transaction)) }
                }
            }
        }
    ) { innerPadding ->
        if (showVoiceAgent) {
            VoiceAgentDialog(
                viewModel = voiceViewModel,
                onDismiss = { showVoiceAgent = false }
            )
        }
        AppNavigation(
            navController = navController,
            snackbarHostState = snackbarHostState,
            innerPadding = innerPadding,
            analyticsTracker = analyticsTracker
        )
    }
}

private fun screenNameForRoute(route: String): String {
    return when {
        route == Screen.Home.route -> "home"
        route == Screen.Transactions.route -> "transactions"
        route == Screen.Goals.route -> "goals"
        route == Screen.Insights.route -> "insights"
        route == Screen.Profile.route -> "profile"
        route == Screen.Debt.route -> "debt"
        route.startsWith("add_transaction") -> "add_transaction"
        route.startsWith("edit_transaction") -> "edit_transaction"
        else -> route
    }
}
