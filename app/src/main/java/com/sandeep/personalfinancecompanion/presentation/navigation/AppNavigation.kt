package com.sandeep.personalfinancecompanion.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sandeep.personalfinancecompanion.analytics.AnalyticsEvent
import com.sandeep.personalfinancecompanion.analytics.AnalyticsParam
import com.sandeep.personalfinancecompanion.analytics.AnalyticsTracker
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.goal.GoalScreen
import com.sandeep.personalfinancecompanion.presentation.home.HomeScreen
import com.sandeep.personalfinancecompanion.presentation.insights.InsightsScreen
import com.sandeep.personalfinancecompanion.presentation.profile.ProfileScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.AddEditTransactionScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.TransactionListScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.TransactionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    modifier: Modifier = Modifier,
    analyticsTracker: AnalyticsTracker
) {
    val transactionViewModel: TransactionViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(500)) + 
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, 
                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500)) + 
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, 
                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500)) + 
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, 
                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(500)) + 
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, 
                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                innerPadding = innerPadding,
                onNavigateToTransactions = {
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.NAV_ITEM_SELECTED,
                        mapOf(
                            AnalyticsParam.SOURCE to "home_category_breakdown",
                            AnalyticsParam.ROUTE to Screen.Transactions.route
                        )
                    )
                    navController.navigate(Screen.Transactions.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddIncome = {
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.ADD_TRANSACTION_CLICKED,
                        mapOf(AnalyticsParam.SOURCE to "home_income", AnalyticsParam.TYPE to "income")
                    )
                    navController.navigate(Screen.AddTransaction.createRoute("INCOME"))
                },
                onAddExpense = {
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.ADD_TRANSACTION_CLICKED,
                        mapOf(AnalyticsParam.SOURCE to "home_expense", AnalyticsParam.TYPE to "expense")
                    )
                    navController.navigate(Screen.AddTransaction.createRoute("EXPENSE"))
                },
                onNavigateToDebt = {
                    analyticsTracker.trackEvent(AnalyticsEvent.DEBT_OPENED)
                    navController.navigate(Screen.Debt.route)},
                onBudgetDialogOpened = {
                    analyticsTracker.trackEvent(AnalyticsEvent.BUDGET_DIALOG_OPENED)
                },
                onBudgetSaved = {
                    analyticsTracker.trackEvent(AnalyticsEvent.BUDGET_SAVED)
                },
                onCategoryBreakdownSelected = { category ->
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.CATEGORY_SELECTED,
                        mapOf(AnalyticsParam.CATEGORY to category.name.lowercase())
                    )
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionListScreen(
                innerPadding = innerPadding,
                snackbarHostState = snackbarHostState,
                onAddTransaction = {
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.ADD_TRANSACTION_CLICKED,
                        mapOf(AnalyticsParam.SOURCE to "transactions")
                    )
                    navController.navigate(Screen.AddTransaction.createRoute())
                },
                onEditTransaction = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                },
                viewModel = transactionViewModel
            )
        }

        composable(Screen.Goals.route) {
            GoalScreen(innerPadding = innerPadding)
        }

        composable(Screen.Insights.route) {
            InsightsScreen(innerPadding = innerPadding)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                innerPadding = innerPadding,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Debt.route) {
            val debtViewModel: com.sandeep.personalfinancecompanion.presentation.debt.DebtManagementViewModel = hiltViewModel()
            com.sandeep.personalfinancecompanion.presentation.debt.DebtManagementScreen(
                innerPadding = innerPadding,
                onPersonClick = { personId ->
                    navController.navigate(Screen.Passbook.createRoute(personId))
                },
                viewModel = debtViewModel
            )
        }

        composable(
            route = Screen.Passbook.route,
            arguments = listOf(
                navArgument("personId") {
                    type = NavType.StringType
                }
            )
        ) {
            val passbookViewModel: com.sandeep.personalfinancecompanion.presentation.debt.PassbookViewModel = hiltViewModel()
            com.sandeep.personalfinancecompanion.presentation.debt.PassbookScreen(
                innerPadding = innerPadding,
                onBack = { navController.popBackStack() },
                viewModel = passbookViewModel
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                },
                navArgument("amount") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("notes") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val amountArg = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull()
            val notesArg = backStackEntry.arguments?.getString("notes")

            val initialType = try {
                TransactionType.valueOf(typeArg)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }

            androidx.compose.runtime.LaunchedEffect(initialType) {
                analyticsTracker.trackEvent(
                    AnalyticsEvent.TRANSACTION_ADD_STARTED,
                    mapOf(AnalyticsParam.TYPE to initialType.name.lowercase())
                )
            }

            AddEditTransactionScreen(
                innerPadding = innerPadding,
                initialType = initialType,
                prefilledAmount = amountArg,
                prefilledNotes = notesArg,
                onSave = { transaction ->
                    transactionViewModel.addTransaction(
                        transaction.amount,
                        transaction.type,
                        transaction.category,
                        transaction.notes,
                        transaction.date,
                        transaction.peerName
                    )
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.TRANSACTION_ADD_SAVED,
                        mapOf(
                            AnalyticsParam.TYPE to transaction.type.name.lowercase(),
                            AnalyticsParam.CATEGORY to transaction.category.name.lowercase()
                        )
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                viewModel = transactionViewModel
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")

            androidx.compose.runtime.LaunchedEffect(transactionId) {
                analyticsTracker.trackEvent(AnalyticsEvent.TRANSACTION_EDIT_OPENED)
            }

            AddEditTransactionScreen(
                innerPadding = innerPadding,
                transactionId = transactionId,
                onSave = { transaction ->
                    transactionViewModel.updateTransaction(transaction)
                    analyticsTracker.trackEvent(
                        AnalyticsEvent.TRANSACTION_EDIT_SAVED,
                        mapOf(
                            AnalyticsParam.TYPE to transaction.type.name.lowercase(),
                            AnalyticsParam.CATEGORY to transaction.category.name.lowercase()
                        )
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                viewModel = transactionViewModel
            )
        }
    }
}
