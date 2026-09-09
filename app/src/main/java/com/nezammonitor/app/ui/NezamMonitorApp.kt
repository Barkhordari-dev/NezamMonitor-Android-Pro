package com.nezammonitor.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTitle by remember { mutableStateOf("خانه") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "سیستم مانیتور",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "نظام مهندسی",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider()
                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Text(screen.icon) },
                        label = { Text(screen.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                            currentTitle = screen.title
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("سیستم مانیتور نظام مهندسی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("ساخته شده توسط مهندس برخورداری", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
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
                        onNavigateToImport = { navController.navigate(Screen.Import.route) }
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
                    val caseNumber = backStackEntry.arguments?.getString("caseNumber") ?: ""
                    LaunchedEffect(caseNumber) { viewModel.loadCaseDetail(caseNumber) }
                    CaseDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Engineers.route) {
                    EngineersScreen(viewModel = viewModel)
                }
                composable(Screen.Fees.route) {
                    FeesScreen(viewModel = viewModel)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }
                composable(Screen.FollowUp.route) {
                    FollowUpScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(Screen.Import.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToImport = { }
                    )
                }
            }
        }
    }
}
