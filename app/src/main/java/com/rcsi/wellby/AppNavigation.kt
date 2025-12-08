package com.rcsi.wellby
// app navigation with main app navigation controller between screens within tabs and decides the tab icons

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rcsi.wellby.chatTab.aiChat.AiChatInfo
import com.rcsi.wellby.chatTab.aiChat.AiChatScreen
import com.rcsi.wellby.chatTab.aiChat.AiMessagesManager
import com.rcsi.wellby.chatTab.aiChat.FurtherResourcesView
import com.rcsi.wellby.hrvTab.BiofeedbackTab
import com.rcsi.wellby.hrvTab.viewModels.BluetoothController
import com.rcsi.wellby.hrvTab.DeviceConnectionScreen
import com.rcsi.wellby.hrvTab.helpers.BreathTempo
import com.rcsi.wellby.hrvTab.helpers.HRVInfoScreen
import com.rcsi.wellby.chatTab.views.ChatScreen
import com.rcsi.wellby.chatTab.views.CoachingGuideView
import com.rcsi.wellby.chatTab.views.MainMessageView
import com.rcsi.wellby.chatTab.views.MessageAllScreen
import com.rcsi.wellby.hrvTab.HRVSummaryScreen
import com.rcsi.wellby.hrvTab.PostRecordingScreen
import com.rcsi.wellby.hrvTab.RecordingScreen
import com.rcsi.wellby.hrvTab.PreRecordingScreen
import com.rcsi.wellby.hrvTab.helpers.PermissionHandler
import com.rcsi.wellby.hrvTab.viewModels.BLEViewModelFactory
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import com.rcsi.wellby.resourcesTab.ResourceManager
import com.rcsi.wellby.resourcesTab.ResourceTopicsView
import com.rcsi.wellby.resourcesTab.SavedResourcesView
import com.rcsi.wellby.resourcesTab.TopicView
import com.rcsi.wellby.resourcesTab.imageViews.DetailView
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.toolkitTab.HomeScreen
import com.rcsi.wellby.toolkitTab.SettingsScreen
import com.rcsi.wellby.toolkitTab.toDoList.ToDoRepository
import com.rcsi.wellby.toolkitTab.toDoList.ToDoScreen
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModel
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModelFactory
import com.rcsi.wellby.toolkitTab.calendar.WeekView
import com.rcsi.wellby.toolkitTab.checkIn.CheckInTracker

@Composable
fun BottomNavGraph(navController: NavHostController, authManager: AuthManager, toDoRepository: ToDoRepository, permissionHandler: PermissionHandler) {
    val resourceManager = viewModel<ResourceManager>()

    val toDoViewModelFactory = ToDoViewModelFactory(toDoRepository)
    val toDoManager = viewModel<ToDoViewModel>(factory = toDoViewModelFactory)

    val hrvDataManager: HRVDataManager = viewModel()

    val context = LocalContext.current

    val bluetoothController: BluetoothController = viewModel(
        factory = BLEViewModelFactory(hrvDataManager, context)
    )

    NavHost(navController = navController, startDestination = TabOptions.Home.route) {
        composable(TabOptions.Home.route) { HomeScreen(authManager, navController, toDoManager)}
        composable(TabOptions.Resources.route) { ResourceTopicsView(resourceManager, navController, authManager) }
        composable(TabOptions.Biofeedback.route) { BiofeedbackTab(bluetoothController, navController, hrvDataManager, authManager, permissionHandler) }
        composable(TabOptions.Messages.route) { MainMessageView(authManager, navController) }

        // Chat nav
        composable("chatScreen") { ChatScreen(authManager) }
        composable("coachingGuide") { CoachingGuideView() }
        composable("messageAll") { MessageAllScreen(authManager, navController)}

        // Resource nav
        composable("topic") { TopicView(resourceManager, navController, authManager) }
        composable("resourceDetail") { DetailView(resourceManager, authManager) }
        composable("savedResources") { SavedResourcesView(resourceManager, navController, authManager) }

        // Toolkit Nav
        composable("checkinTracker") { CheckInTracker(authManager) }
        composable("settings") { SettingsScreen(authManager)}

        // Calendar To Do
        composable("weekView") { WeekView(navController, toDoViewModel = toDoManager) }
        composable("toDoScreen") { ToDoScreen(viewModel = toDoManager, authManager) }
        composable("aiChat") {
            val aiMessagesManager = remember {
                AiMessagesManager(userId = authManager.currentUser.value?.id ?: "")
            }
            AiChatScreen(aiMessagesManager, authManager)

        }
        composable("furtherResources") { FurtherResourcesView(authManager) }
        composable("aiChatInfo") { AiChatInfo(authManager) }


        // HRV
        composable("breathPacer") { BreathTempo(authManager) }
        composable("hrvInfo") { HRVInfoScreen(authManager) }
        composable("deviceConnectionScreen") { DeviceConnectionScreen(bluetoothController) }
        composable("sessionSummary") { HRVSummaryScreen(hrvDataManager, authManager) }
        composable("prerecording") { PreRecordingScreen(navController, bluetoothController, hrvDataManager) }
        composable("recordingScreen") { RecordingScreen(bluetoothController, hrvDataManager, navController) }
        composable("postrecording") { PostRecordingScreen(navController, hrvDataManager, authManager, bluetoothController) }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation (
        backgroundColor = MaterialTheme.colorScheme.onSecondary
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route


        val items = listOf(
            TabOptions.Home,
            TabOptions.Resources,
            TabOptions.Biofeedback,
            TabOptions.Messages
        )

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute == item.route) {
                        // Only pop back if not already at the root
                        navController.popBackStack()
                    } else {
                        // Navigate to a different tab's root
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationRoute ?: item.route) {
                                saveState = true
                            }
                            launchSingleTop = true  // Avoid multiple instances of the same destination
                            restoreState = true  // Restore state when navigating to a previously selected tab
                        }
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
