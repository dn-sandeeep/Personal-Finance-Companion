package com.sandeep.personalfinancecompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandeep.personalfinancecompanion.voiceagent.presentation.VoiceAgentDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sandeep.personalfinancecompanion.appfunctions.AgentLifecycleManager
import com.sandeep.personalfinancecompanion.presentation.navigation.AppNavigation
import com.sandeep.personalfinancecompanion.presentation.navigation.Screen
import com.sandeep.personalfinancecompanion.presentation.navigation.bottomNavItems
import com.sandeep.personalfinancecompanion.ui.theme.PersonalFinanceCompanionTheme
import com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent
import com.sandeep.personalfinancecompanion.voiceagent.presentation.VoiceAgentViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var agentLifecycleManager: AgentLifecycleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Triggering mainViewModel initialization to start observing and scheduling
        mainViewModel
        enableEdgeToEdge()
        setContent {
            // Register AI Agent capabilities for Android 16+ discovery
            agentLifecycleManager.registerAgentCapabilities(this)

            PersonalFinanceCompanionTheme { FinanceApp() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showVoiceAgent by remember { mutableStateOf(false) }
    val voiceViewModel: VoiceAgentViewModel = hiltViewModel()

    // Show bottom bar only on main tabs
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val topBarTitle =
            when {
                currentRoute == Screen.Home.route -> "Home"
                currentRoute == Screen.Transactions.route -> "History"
                currentRoute == Screen.Goals.route -> "Goals"
                currentRoute == Screen.Insights.route -> "Insights"
                currentRoute == Screen.Profile.route -> "Profile"
                currentRoute?.startsWith("add_transaction") == true -> "Add Transaction"
                else -> "Track Spend"
            }

    val canNavigateBack = currentRoute?.startsWith("add_transaction") == true

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
                                            contentDescription = "Back"
                                    )
                                }
                            }
                        },
                        actions = {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                            Modifier.padding(end = 12.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
//                                                        navController.navigate(
//                                                                Screen.Profile.route
//                                                        ) { launchSingleTop = true }
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
                                        text = "User",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Profile",
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

                            NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
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
                                                contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                                text = item.label,
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
                                showVoiceAgent = true 
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Agent",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Existing Add Transaction FAB
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(Screen.AddTransaction.createRoute())
                            },
                            containerColor = PrimaryAccent,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) { Icon(Icons.Default.Add, contentDescription = "Add Transaction") }
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
                modifier = Modifier.padding(innerPadding)
        )
    }
}
