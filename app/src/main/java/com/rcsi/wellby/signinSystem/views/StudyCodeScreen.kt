package com.rcsi.wellby.signinSystem.views
// study code to enter when creating an account to allow only authorised users to create accounts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.components.EntryField
import com.rcsi.wellby.components.NextButton
import com.rcsi.wellby.signinSystem.AuthManager
import kotlinx.coroutines.launch

@Composable
fun StudyCodeScreen(navController: NavController, authManager: AuthManager) {
    var code by remember { mutableStateOf("") }
    val codeStatus = authManager.codeStatus.collectAsState().value
    var navigateToOnboarding by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (navigateToOnboarding) {
        LaunchedEffect(Unit) {
            navController.navigate("onBoarding")
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Enter a study code to create an account:",
                style = MaterialTheme.typography.headlineMedium
            )

            EntryField(
                label = "Study Code",
                text = code,
                onTextChange = { code = it }
            )

            NextButton(
                title = "Verify Study Code",
                onClick = {
                    coroutineScope.launch {
                        authManager.checkStudyCode(code) { codeExists ->
                            if (codeExists) {
                                navigateToOnboarding = true
                            } else {
                                println("code is incorrect, display message")
                            }
                        }
                    }
                }
            )

            if (codeStatus != null) {
                Text(
                    text = codeStatus,
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