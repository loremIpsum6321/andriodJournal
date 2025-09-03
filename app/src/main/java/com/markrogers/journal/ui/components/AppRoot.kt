package com.markrogers.journal.ui.components

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.markrogers.journal.data.prefs.PreferencesRepository
import com.markrogers.journal.data.prefs.ThemeMode
import com.markrogers.journal.ui.analyze.AnalyzeScreen
import com.markrogers.journal.ui.calendar.CalendarScreen
import com.markrogers.journal.ui.editor.EditorScreen
import com.markrogers.journal.ui.metrics.MetricsScreen
import com.markrogers.journal.ui.settings.SettingsScreen
import com.markrogers.journal.ui.theme.JournalTheme
import com.markrogers.journal.ui.timeline.TimelineScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    JOURNAL("tab/journal", "Journal", Icons.Filled.List),
    METRICS("tab/metrics", "Metrics", Icons.Filled.Analytics),
    CALENDAR("tab/calendar", "Calendar", Icons.Filled.CalendarMonth),
    ANALYZE("tab/analyze", "Analyze", Icons.Filled.Assessment),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(appContext: Context) {
    val prefsRepo = remember { PreferencesRepository(appContext) }
    val prefs by prefsRepo.prefsFlow.collectAsState(initial = null)

    val useDark = when (prefs?.theme ?: ThemeMode.SYSTEM) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    JournalTheme(useDarkTheme = useDark) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        val onSettings = currentRoute == "settings"

        var currentTab by remember { mutableStateOf(Tab.JOURNAL) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (onSettings) "Settings" else currentTab.label) },
                    navigationIcon = {
                        if (onSettings) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        if (!onSettings) {
                            IconButton(onClick = { nav.navigate("settings") }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!onSettings) {
                    NavigationBar {
                        listOf(Tab.JOURNAL, Tab.METRICS, Tab.CALENDAR, Tab.ANALYZE).forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { pad ->
            NavHost(
                navController = nav,
                startDestination = Tab.JOURNAL.route,
                modifier = Modifier.fillMaxSize().padding(pad)
            ) {
                composable(Tab.JOURNAL.route) { TimelineScreen(onNewEntry = { nav.navigate("editor") }) }
                composable(Tab.METRICS.route) { MetricsScreen() }
                composable(Tab.CALENDAR.route) { CalendarScreen() }
                composable(Tab.ANALYZE.route) { AnalyzeScreen(prefsRepo = prefsRepo) }
                composable("settings") { SettingsScreen(prefsRepo) }
                composable("editor") { EditorScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}