package com.sandeep.personalfinancecompanion.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object Goals : Screen("goals")
    data object Insights : Screen("insights")
    data object AddTransaction : Screen("add_transaction/{type}") {
        fun createRoute(type: String = "EXPENSE") = "add_transaction/$type"
    }
    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        label = "History",
        route = Screen.Transactions.route,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavItem(
        label = "Goals",
        route = Screen.Goals.route,
        selectedIcon = Icons.Filled.FlagCircle,
        unselectedIcon = Icons.Outlined.FlagCircle
    ),
    BottomNavItem(
        label = "Insights",
        route = Screen.Insights.route,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
)
