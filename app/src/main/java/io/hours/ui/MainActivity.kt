package io.hours.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import io.hours.ui.home.HomeScreen
import io.hours.ui.dailydetail.DailyDetailScreen
import io.hours.ui.theme.UsageTrackerTheme
import io.hours.ui.timeline.DailyTimeLineScreen
import io.hours.ui.weeklydetail.WeeklyDetailScreen
import io.hours.utils.Constraint
import com.google.gson.JsonObject
import io.hours.ui.home.HomeScreenTabs

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UsageTrackerTheme {
                MainScreen {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            }
        }
    }

}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
    onGrantPermissionClick: () -> Unit,
) {
    val navController = rememberNavController()
    val context by rememberUpdatedState(LocalContext.current)
    var isGrant by remember { mutableStateOf(getGrantStatus(context)) }
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    Scaffold {
        DisposableEffect(lifecycleOwner) {
            val lifecycle = lifecycleOwner.lifecycle
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && !isGrant) {
                    isGrant = getGrantStatus(context)
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        if (!isGrant)
            Column {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp),
                        onClick = { onGrantPermissionClick.invoke() }) {
                        Text(text = "enable usage stats permission")
                    }
                }
            }
        else
            NavGraph(navController = navController)
    }
}

fun getGrantStatus(applicationContext: Context): Boolean {
    val appOps =
        applicationContext.getSystemService(ComponentActivity.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(), applicationContext.packageName
    )
    return if (mode == AppOpsManager.MODE_DEFAULT) {
        applicationContext.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    } else {
        mode == AppOpsManager.MODE_ALLOWED
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Constraint.SCREEN_HOME_ROUTE,
) {
    var tabSelected by remember { mutableStateOf(HomeScreenTabs.Daily) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Constraint.SCREEN_HOME_ROUTE) {
            HomeScreen(
                onDayItemsClick = { usageCell ->
                    val json = if (usageCell.id == 0L) {
                        JsonObject().apply {
                            addProperty("usageAvatar", usageCell.usageAvatar)
                            addProperty("totalUsage", usageCell.totalUsage)
                            addProperty("fromDate", usageCell.fromDate)
                            addProperty("toDate", usageCell.toDate)
                        }.toString()
                    } else " "
                    navController.navigate(Constraint.SCREEN_DAILY_DETAIL_ROUTE + "/${usageCell.id}/${json}")
                },
                onWeekAndMonthItemsClick = { usageCell, isWeekly ->
                    val json = if (usageCell.id == 0L) {
                        JsonObject().apply {
                            addProperty("usageAvatar", usageCell.usageAvatar)
                            addProperty("totalUsage", usageCell.totalUsage)
                            addProperty("fromDate", usageCell.fromDate)
                            addProperty("toDate", usageCell.toDate)
                            addProperty("isWeekly", isWeekly)
                        }.toString()
                    } else " "
                    navController.navigate(Constraint.SCREEN_WEEKLY_DETAIL_ROUTE + "/${usageCell.id}/${json}")
                },
                onTimelineClick = { navController.navigate(Constraint.SCREEN_DAILY_TIME_LINE_ROUTE) },
                tabSelected = tabSelected,
                onTabClick = { tabSelected = it })
        }
        composable(Constraint.SCREEN_DAILY_TIME_LINE_ROUTE) {
            DailyTimeLineScreen(onBackClick = { navController.navigateUp() })
        }
        composable(
            Constraint.SCREEN_DAILY_DETAIL_ROUTE + "/{usageCellId}/{usageCell}",
            arguments = listOf(
                navArgument("usageCellId") {
                    type = NavType.LongType
                }, navArgument("usageCell") {
                    type = NavType.StringType
                })
        ) {
            DailyDetailScreen(
                usageCellId = it.arguments?.getLong("usageCellId")!!,
                usageCellString = it.arguments?.getString("usageCell")!!,
                onBackClick = { navController.navigateUp() })
        }
        composable(
            Constraint.SCREEN_WEEKLY_DETAIL_ROUTE + "/{usageCellId}/{usageCell}",
            arguments = listOf(
                navArgument("usageCellId") {
                    type = NavType.LongType
                }, navArgument("usageCell") {
                    type = NavType.StringType
                })
        ) {
            WeeklyDetailScreen(
                usageCellId = it.arguments?.getLong("usageCellId")!!,
                usageCellString = it.arguments?.getString("usageCell")!!,
                onBackClick = { navController.navigateUp() },
                onDayClick = { usageCell ->
                    val json = if (usageCell.id == 0L) {
                        JsonObject().apply {
                            addProperty("usageAvatar", usageCell.usageAvatar)
                            addProperty("totalUsage", usageCell.totalUsage)
                            addProperty("fromDate", usageCell.fromDate)
                            addProperty("toDate", usageCell.toDate)
                        }.toString()
                    } else " "
                    navController.navigate(Constraint.SCREEN_DAILY_DETAIL_ROUTE + "/${usageCell.id}/${json}")
                },
            )

        }
    }
}