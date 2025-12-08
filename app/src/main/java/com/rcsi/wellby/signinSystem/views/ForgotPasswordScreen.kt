package com.rcsi.wellby.signinSystem.views
// screen for entering an email to retrieve a forgotten password through google firebase
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun ForgotPasswordScreen(navController: NavController, authManager: AuthManager) {
    var email by remember { mutableStateOf("") }
    val resetStatus by authManager.resetEmailStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium
            )

            Text("Enter your email below to get a link to reset your password.")

            EntryField(
                label = "Email",
                text = email,
                onTextChange = { email = it }
            )

            NextButton(
                title = "Email a reset link",
                onClick = {
                    authManager.resetPassword(email)
                }
            )

            if (resetStatus != null) {
                Text(
                    text = resetStatus!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                    navController.navigate("signIn")
            }) {
                Text("Back to Sign In")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }

}