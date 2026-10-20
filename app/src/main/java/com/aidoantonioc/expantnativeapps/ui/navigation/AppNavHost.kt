package com.aidoantonioc.expantnativeapps.ui.navigation


import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aidoantonioc.expantnativeapps.ui.scan.ScanRoute
import com.aidoantonioc.expantnativeapps.ui.transactions.TransactionDetailRoute
import com.aidoantonioc.expantnativeapps.ui.transactions.TransactionListRoute

object Routes {
    const val ARG_ID = "transactionId"
    const val LIST = "transactions"
    const val DETAIL = "transactions/{$ARG_ID}"

    const val SCAN = "scan"

    fun detail(id: String) = "transactions/${Uri.encode(id)}"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            TransactionListRoute(
                onAddClick = { navController.navigate(Routes.SCAN) },
                onTransactionClick = { id -> navController.navigate(Routes.detail(id)) }
            )
        }

        composable(Routes.SCAN) {
            ScanRoute(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) {
            TransactionDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}