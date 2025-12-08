package com.rcsi.wellby.signinSystem.views
// sign in screen to enter username and password
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.rcsi.wellby.ui.theme.SignInSystemTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.R
import com.rcsi.wellby.components.EntryField
import com.rcsi.wellby.components.NextButton
import com.rcsi.wellby.signinSystem.AuthManager
import kotlinx.coroutines.delay

@Composable
fun SignInScreen(navController: NavController, authManager: AuthManager) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSigningIn by remember { mutableStateOf(false) }

    // Collect createAccountStatus as a state to observe changes
    //val userSession by authManager.userSession.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    val signInStatus by authManager.signInStatus.collectAsState()

    LaunchedEffect(signInStatus) {
        when {
            signInStatus != null -> {
                isSigningIn = false // Stop showing loading when there's a sign-in status update
                delay(5000) // Wait for 5 seconds before clearing status message
                authManager.signInStatus.value = null // Clear the status message
            }
        }
    }

    // Check if account creation was successful
    LaunchedEffect(currentUser) {
        if (currentUser != null) {

            navController.navigate("home") {
                // Pop up to the root of the navigation graph to avoid a back stack
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
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

        Image(
            painter = painterResource(id = R.drawable.cphs),
            contentDescription = null,
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        EntryField(
            label = "Email",
            text = email,
            onTextChange = { email = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EntryField(
            label = "Password",
            text = password,
            onTextChange = { password = it },
            isSecure = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        NextButton(
            title = "Sign In",
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    isSigningIn = true
                    authManager.signIn(email, password)
                }
            },
            enableStatus = !isSigningIn
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (signInStatus != null) {
            Text(
                text = signInStatus!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        TextButton(onClick = {
            navController.navigate("forgotPassword")
        }) {
            Text("Forgot Password?")
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = {
            navController.navigate("studyCode")
        }) {
            Text("Don't have an account? Create one here.")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignInPreview() {
    val navController = rememberNavController()

    SignInSystemTheme { // Make sure to wrap your preview content with your app's theme if you have one
        SignInScreen(navController = navController, authManager = AuthManager())
    }
}
