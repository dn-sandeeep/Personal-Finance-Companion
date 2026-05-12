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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

import com.sandeep.personalfinancecompanion.R

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object Goals : Screen("goals")
    data object Insights : Screen("insights")
    data object AddTransaction : Screen("add_transaction/{type}?amount={amount}&notes={notes}") {
        fun createRoute(type: String = "EXPENSE", amount: Double? = null, notes: String? = null): String {
            var route = "add_transaction/$type"
            if (amount != null || notes != null) {
                route += "?"
                if (amount != null) route += "amount=$amount"
                if (notes != null) {
                    if (amount != null) route += "&"
                    route += "notes=$notes"
                }
            }
            return route
        }
    }
    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }
    data object Profile : Screen("profile")
    data object Debt : Screen("debt")
    data object Passbook : Screen("passbook/{personId}") {
        fun createRoute(personId: String) = "passbook/$personId"
    }
}

data class BottomNavItem(
    val labelResId: Int,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        labelResId = R.string.nav_home,
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        labelResId = R.string.nav_history,
        route = Screen.Transactions.route,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavItem(
        labelResId = R.string.nav_goals,
        route = Screen.Goals.route,
        selectedIcon = Icons.Filled.FlagCircle,
        unselectedIcon = Icons.Outlined.FlagCircle
    ),
    BottomNavItem(
        labelResId = R.string.nav_insights,
        route = Screen.Insights.route,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    BottomNavItem(
        labelResId = R.string.nav_profile,
        route = Screen.Profile.route,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)
