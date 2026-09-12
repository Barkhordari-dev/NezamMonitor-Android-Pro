package com.nezammonitor.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nezammonitor.app.ui.navigation.Screen
import com.nezammonitor.app.ui.navigation.drawerItems
import com.nezammonitor.app.ui.screens.cases.CasesScreen
import com.nezammonitor.app.ui.screens.cases.CaseDetailScreen
import com.nezammonitor.app.ui.screens.engineers.EngineersScreen
import com.nezammonitor.app.ui.screens.fees.FeesScreen
import com.nezammonitor.app.ui.screens.followup.FollowUpScreen
import com.nezammonitor.app.ui.screens.home.HomeScreen
import com.nezammonitor.app.ui.screens.reports.ReportsScreen
import com.nezammonitor.app.ui.screens.settings.SettingsScreen
import com.nezammonitor.app.ui.screens.export.ExportScreen
import com.nezammonitor.app.ui.screens.update.UpdateScreen
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "برنامه تخصصی مانیتور نظام مهندسی",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "مهندس برخورداری",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    drawerItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Text(screen.icon, fontSize = 18.sp) },
                            label = { Text(screen.title, fontSize = 14.sp) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("برنامه تخصصی مانیتور نظام مهندسی", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("مهندس برخورداری", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Screen.Update.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToImport = { navController.navigate(Screen.Import.route) },
                        onNavigateToCases = { navController.navigate(Screen.Cases.route) },
                        onNavigateToEngineers = { navController.navigate(Screen.Engineers.route) },
                        onNavigateToFees = { navController.navigate(Screen.Fees.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                        onNavigateToFollowUp = { navController.navigate(Screen.FollowUp.route) },
                        onNavigateToExport = { navController.navigate(Screen.Export.route) }
                    )
                }
                composable(Screen.Cases.route) {
                    CasesScreen(
                        viewModel = viewModel,
                        onCaseClick = { caseNumber ->
                            navController.navigate(Screen.CaseDetail.createRoute(caseNumber))
                        }
                    )
                }
                composable(
                    route = Screen.CaseDetail.route,
                    arguments = listOf(navArgument("caseNumber") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("caseNumber") ?: ""
                    val caseNumber = try { URLDecoder.decode(encoded, "UTF-8") } catch (e: Exception) { encoded }
                    LaunchedEffect(caseNumber) { viewModel.loadCaseDetail(caseNumber) }
                    CaseDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Engineers.route) {
                    EngineersScreen(
                        viewModel = viewModel,
                        onCaseClick = { caseNumber ->
                            navController.navigate(Screen.EngineerDetail.createRoute(caseNumber))
                        }
                    )
                }
                composable(
                    route = Screen.EngineerDetail.route,
                    arguments = listOf(navArgument("caseNumber") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("caseNumber") ?: ""
                    val caseNumber = try { URLDecoder.decode(encoded, "UTF-8") } catch (e: Exception) { encoded }
                    LaunchedEffect(caseNumber) { viewModel.loadCaseDetail(caseNumber) }
                    com.nezammonitor.app.ui.screens.engineers.EngineerDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Fees.route) {
                    FeesScreen(
                        viewModel = viewModel,
                        onCaseClick = { caseNumber ->
                            navController.navigate(Screen.FeeDetail.createRoute(caseNumber))
                        }
                    )
                }
                composable(
                    route = Screen.FeeDetail.route,
                    arguments = listOf(navArgument("caseNumber") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("caseNumber") ?: ""
                    val caseNumber = try { URLDecoder.decode(encoded, "UTF-8") } catch (e: Exception) { encoded }
                    LaunchedEffect(caseNumber) { viewModel.loadCaseDetail(caseNumber) }
                    com.nezammonitor.app.ui.screens.fees.FeeDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(
                        viewModel = viewModel,
                        onCaseClick = { caseNumber ->
                            navController.navigate(Screen.ReportDetail.createRoute(caseNumber))
                        }
                    )
                }
                composable(
                    route = Screen.ReportDetail.route,
                    arguments = listOf(navArgument("caseNumber") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("caseNumber") ?: ""
                    val caseNumber = try { URLDecoder.decode(encoded, "UTF-8") } catch (e: Exception) { encoded }
                    LaunchedEffect(caseNumber) { viewModel.loadCaseDetail(caseNumber) }
                    com.nezammonitor.app.ui.screens.reports.ReportDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.FollowUp.route) {
                    FollowUpScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
                composable(Screen.Import.route) {
                    HomeScreen(viewModel = viewModel)
                }
                composable(Screen.Export.route) {
                    ExportScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Update.route) {
                    UpdateScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
