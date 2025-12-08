package com.rcsi.wellby.signinSystem.views
// onboarding screen completed upon initial download of the app and signing up for an account
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.components.EntryField
import com.rcsi.wellby.components.NextButton
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun OnboardingScreen(navController: NavController, authManager: AuthManager) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }

    // Collect createAccountStatus as a state to observe changes
    val userSession by authManager.userSession.collectAsState()

    // Check if account creation was successful
    LaunchedEffect(userSession) {
        if (userSession != null) {

            navController.navigate("home") {
                // Pop up to the root of the navigation graph to avoid a back stack
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true // Avoid multiple instances if re-launched
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Please complete the information below to finish creating an account.")

        Spacer(modifier = Modifier.height(16.dp))

        EntryField(
            label = "First Name",
            text = firstName,
            onTextChange = { firstName = it }
        )

        EntryField(
            label = "Surname",
            text = surname,
            onTextChange = { surname = it }
        )

        EntryField(
            label = "Username",
            text = username,
            onTextChange = { username = it },
            info = true,
            infoMessage = "This is the only information that will be available to the coaches on the" +
                    " app, so please choose something that doesn't reveal your first name."
        )

        EntryField(
            label = "Email",
            text = email,
            onTextChange = { email = it },
            info = true,
            infoMessage = "Your email will only be used for password resets."
        )

        EntryField(
            label = "Password",
            text = password,
            onTextChange = { password = it },
            isSecure = true,
            info = true,
            infoMessage = "Password must be at least 6 characters long"
        )

        EntryField(
            label = "Confirm Password",
            text = passwordConfirmation,
            onTextChange = { passwordConfirmation = it },
            isSecure = true
        )

        if (passwordConfirmation.isNotEmpty()) {
            Text(
                text = if (password == passwordConfirmation) "Passwords match" else "Passwords do not match",
                color = if (password == passwordConfirmation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isButtonEnabled = password.isNotEmpty() && password == passwordConfirmation
        NextButton(
            title = "Create Account",
            onClick = {
                authManager.createAccount(email, password, firstName, surname, username)
            },
            enableStatus = isButtonEnabled
        )

        val accountStatus by authManager.createAccountStatus.collectAsState()
        if (accountStatus != null) {
            Text(
                text = accountStatus!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("signIn")
        }) {
            Text("Back to Sign In")
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}