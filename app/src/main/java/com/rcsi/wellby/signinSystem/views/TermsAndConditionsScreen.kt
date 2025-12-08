package com.rcsi.wellby.signinSystem.views
// terms and conditions displayed when first opening the app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TermsAndConditionsScreen(onAccepted: () -> Unit) {
    val scrollState = rememberScrollState()
    val accepted = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text("Terms and Conditions", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Introduction", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Welcome to Wellby: Your Well-being Buddy. The app is designed to provide high school students with lifestyle resources, access to health coaches, and tools such as breath pacers for enhancing their well-being.") // Summarize the content appropriately

            Spacer(modifier = Modifier.height(8.dp))

            Text("1. Acceptance of Terms", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("You must agree to these Terms before using Wellby. If you are under the age of 18, you must have your parent or guardian read and agree to these Terms on your behalf.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("2. Use of the App", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("The app is intended for educational and informational purposes only. You may not use the app for any illegal or unauthorized purpose.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("3. User-Generated Content", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("You may send messages provided that the content complies with our content guidelines. You agree not to send objectionable content, such as offensive, threatening, or sexually explicit material. We reserve the right to remove any content that violates these Terms.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("4. Chat Features", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("The chat feature allows you to communicate with health coaches. Be respectful and professional in your interactions. Do not share personal information within chat sessions. Users can report or block abusive users through the app’s reporting mechanisms.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("5. Intellectual Property", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("All content provided by the app, including resources and tools, is owned by us or our licensors and is protected by copyright and other intellectual property laws. You may not reproduce, distribute, or create derivative works from our content without express permission.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("6. Privacy", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your privacy is important to us. Please review our Privacy Policy to understand how we collect, use, and share your information.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("7. Disclaimers and Limitations of Liability", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("The app is provided on an “as is” basis. We do not guarantee its accuracy, completeness, or usefulness. We are not liable for any damages or loss resulting from your use of the app.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("8. Changes to Terms", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("We reserve the right to modify these Terms at any time. Your continued use of the app following any changes indicates your acceptance of the new Terms.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("9. Governing Law", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("These Terms are governed by the laws of Ireland. Any disputes related to these Terms will be subject to the jurisdiction of Irish courts.")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Contact Us", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("If you have any questions about these Terms, please contact us at justinlaiti22@rcsi.ie.")
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    accepted.value = true
                    onAccepted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Accept", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

