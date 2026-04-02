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
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.goal.GoalScreen
import com.sandeep.personalfinancecompanion.presentation.home.HomeScreen
import com.sandeep.personalfinancecompanion.presentation.insights.InsightsScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.AddEditTransactionScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.TransactionListScreen
import com.sandeep.personalfinancecompanion.presentation.transactions.TransactionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val transactionViewModel: TransactionViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddIncome = {
                    navController.navigate(Screen.AddTransaction.createRoute("INCOME"))
                },
                onAddExpense = {
                    navController.navigate(Screen.AddTransaction.createRoute("EXPENSE"))
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionListScreen(
                snackbarHostState = snackbarHostState,
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.createRoute())
                },
                onEditTransaction = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                },
                viewModel = transactionViewModel
            )
        }

        composable(Screen.Goals.route) {
            GoalScreen()
        }

        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(400)
                )
            }
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val initialType = try {
                TransactionType.valueOf(typeArg)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }

            AddEditTransactionScreen(
                initialType = initialType,
                onSave = { transaction ->
                    transactionViewModel.addTransaction(
                        transaction.amount,
                        transaction.type,
                        transaction.category,
                        transaction.notes,
                        transaction.date
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
                    tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(400)
                )
            }
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")

            AddEditTransactionScreen(
                transactionId = transactionId,
                onSave = { transaction ->
                    transactionViewModel.updateTransaction(transaction)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                viewModel = transactionViewModel
            )
        }
    }
}
