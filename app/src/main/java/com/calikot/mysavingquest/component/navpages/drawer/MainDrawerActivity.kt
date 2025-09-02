package com.calikot.mysavingquest.component.navpages.drawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import com.calikot.mysavingquest.component.navpages.dashboard.DashboardScreen
import com.calikot.mysavingquest.component.navpages.actionneeded.ui.ActionNeededScreen
import com.calikot.mysavingquest.component.navpages.history.HistoryScreen
import com.calikot.mysavingquest.component.navpages.settings.SettingsScreen
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainDrawerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySavingQuestTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainDrawerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawerScreen(modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf("Dashboard", "Action Needed", "History", "Settings")
    val icons = listOf(
        Icons.Filled.Home,      // Dashboard
        Icons.Filled.Warning,   // Action Needed
        Icons.AutoMirrored.Filled.List, // History
        Icons.Filled.Settings   // Settings
    )
    val routes = NavRoutes.entries
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(routes[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = modifier.padding(WindowInsets.safeDrawing.asPaddingValues())
            ) {
                Spacer(Modifier.height(16.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        selected = currentRoute == routes[index],
                        onClick = {
                            currentRoute = routes[index]
                            navController.navigate(routes[index].route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(items[routes.indexOf(currentRoute)]) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.DASHBOARD.route
                ) {
                    composable(NavRoutes.DASHBOARD.route) { DashboardScreen(navController) }
                    composable(NavRoutes.ACTION_NEEDED.route) { ActionNeededScreen(navController) }
                    composable(NavRoutes.HISTORY.route) { HistoryScreen(navController) }
                    composable(NavRoutes.SETTINGS.route) { SettingsScreen(navController) }
                }
            }
        }
    }
}
