package com.rcsi.wellby.signinSystem
// navigation overview for the signin system
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rcsi.wellby.signinSystem.views.ForgotPasswordScreen
import com.rcsi.wellby.toolkitTab.HomeScreen
import com.rcsi.wellby.signinSystem.views.OnboardingScreen
import com.rcsi.wellby.signinSystem.views.SignInScreen
import com.rcsi.wellby.signinSystem.views.StudyCodeScreen
import com.rcsi.wellby.toolkitTab.toDoList.ToDoRepository
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModel
import com.rcsi.wellby.toolkitTab.toDoList.ToDoViewModelFactory

@Composable
fun SignInNavigation(authManager: AuthManager, toDoRepository: ToDoRepository) {
    val navController = rememberNavController()

    val toDoViewModelFactory = ToDoViewModelFactory(toDoRepository)
    val toDoManager = viewModel<ToDoViewModel>(factory = toDoViewModelFactory)

    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") { SignInScreen(navController, authManager) }
        composable("home") { HomeScreen(authManager, navController, toDoManager) }
        composable("studyCode") { StudyCodeScreen(navController, authManager) }
        composable("forgotPassword") { ForgotPasswordScreen(navController, authManager) }
        composable("onBoarding") { OnboardingScreen(navController, authManager) }
    }
}