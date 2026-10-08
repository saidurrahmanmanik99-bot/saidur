package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.MadrasaViewModel
import com.example.ui.screens.ScreenTab
import com.example.ui.screens.documents.DocumentCertificateScreen
import com.example.ui.screens.exams.ExamManagementScreen
import com.example.ui.screens.home.DashboardScreen
import com.example.ui.screens.jamats.JamatManagementScreen
import com.example.ui.screens.questions.QuestionPaperScreen
import com.example.ui.screens.results.ResultManagementScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.students.StudentManagementScreen
import com.example.ui.screens.teachers.TeacherManagementScreen
import com.example.ui.theme.MadrasaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MadrasaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MadrasaTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MadrasaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Primary 5 tabs for bottom navigation
    val bottomTabs = listOf(
        ScreenTab.HOME to Icons.Default.Home,
        ScreenTab.STUDENTS to Icons.Default.School,
        ScreenTab.JAMATS to Icons.Default.Class,
        ScreenTab.RESULTS to Icons.Default.Grade,
        ScreenTab.SETTINGS to Icons.Default.Settings
    )

    // Complete navigation items for Drawer
    val allNavItems = listOf(
        ScreenTab.HOME to Icons.Default.Home,
        ScreenTab.STUDENTS to Icons.Default.School,
        ScreenTab.TEACHERS to Icons.Default.People,
        ScreenTab.JAMATS to Icons.Default.Class,
        ScreenTab.EXAMS to Icons.Default.Assignment,
        ScreenTab.RESULTS to Icons.Default.Grade,
        ScreenTab.QUESTIONS to Icons.Default.Description,
        ScreenTab.DOCUMENTS to Icons.Default.CardMembership,
        ScreenTab.SETTINGS to Icons.Default.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Text(
                        text = "মাদ্রাসা ম্যানেজমেন্ট",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "সম্পূর্ণ অফলাইন ইসলামিক মাদ্রাসা সফটওয়্যার",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                allNavItems.forEach { (tab, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentTab == tab,
                        onClick = {
                            viewModel.setTab(tab)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                ScreenTab.HOME -> "মাদ্রাসা ম্যানেজমেন্ট"
                                else -> currentTab.title
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "মেনু")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomTabs.forEach { (tab, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            selected = currentTab == tab,
                            onClick = { viewModel.setTab(tab) },
                            modifier = Modifier.testTag("bottom_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateTab = { tab -> viewModel.setTab(tab) }
                    )
                    ScreenTab.STUDENTS -> StudentManagementScreen(viewModel = viewModel)
                    ScreenTab.TEACHERS -> TeacherManagementScreen(viewModel = viewModel)
                    ScreenTab.JAMATS -> JamatManagementScreen(viewModel = viewModel)
                    ScreenTab.EXAMS -> ExamManagementScreen(
                        viewModel = viewModel,
                        onNavigateToResults = { viewModel.setTab(ScreenTab.RESULTS) }
                    )
                    ScreenTab.RESULTS -> ResultManagementScreen(viewModel = viewModel)
                    ScreenTab.QUESTIONS -> QuestionPaperScreen(viewModel = viewModel)
                    ScreenTab.DOCUMENTS -> DocumentCertificateScreen(viewModel = viewModel)
                    ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
