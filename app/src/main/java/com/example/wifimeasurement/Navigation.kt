package com.example.wifimeasurement

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wifimeasurement.Destinations.MEASURE_ROUTE
import com.example.wifimeasurement.Destinations.REPORT_ROUTE

object Destinations {
    const val MEASURE_ROUTE = "measure"
    const val REPORT_ROUTE = "report/{positionName}"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MEASURE_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MEASURE_ROUTE) {
            MeasureScreen(
                navController = navController
            )
        }
        composable(
            REPORT_ROUTE,
            arguments = listOf(
                navArgument("positionName") {type = NavType.StringType},
            )
        ) { backStackEntry ->
            val positionName = backStackEntry.arguments?.getString("positionName")
            WifiMeasureReport(positionName)
        }
    }
}
