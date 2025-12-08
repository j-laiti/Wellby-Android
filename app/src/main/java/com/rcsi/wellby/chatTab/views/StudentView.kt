package com.rcsi.wellby.chatTab.views
// View specific to students which shows the option to chat with a health coach and enter
// the chat screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rcsi.wellby.R
import com.rcsi.wellby.chatTab.ChatManager
import com.rcsi.wellby.chatTab.ChatManagerFactory
import com.rcsi.wellby.chatTab.getTimeAgo
import com.rcsi.wellby.signinSystem.AuthManager
import com.rcsi.wellby.signinSystem.User

@Composable
fun StudentView(userManager: AuthManager, navController: NavController) {
    val chatManager: ChatManager = viewModel(
        factory = ChatManagerFactory(userManager)
    )
    // on launch, fetch coach
    LaunchedEffect(key1 = Unit) {
        userManager.fetchAssignedCoach()
        chatManager.fetchRecentMessages()
        userManager.clickedOn("Coaching Chat")
    }

    val coach = userManager.assignedCoach.collectAsState().value
    val currentUser by userManager.currentUser.collectAsState()

    val currentUserAssignedCoach = currentUser?.assignedCoach

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.White
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY  // This makes the gradient stretch to the bottom
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Start a chat:",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display coach information
            if (currentUser?.isCoachingOptedIn == true) {
                if (coach != null && currentUserAssignedCoach != 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CoachInfoView(userManager, coach, navController, chatManager)
                        Spacer(modifier = Modifier.height(24.dp))
                        CoachingGuideButton(navController)
                    }
                } else {
                    Text("No assigned coach found")
                }
            }

            WellbyAutomatedChat(navController)

            FurtherButtonOptions(navController)

            if (currentUser?.isCoachingOptedIn == false) {
                ManageCoachingSettingsButton(navController)
            }
        }
    }

}

@Composable
fun CoachInfoView(userManager: AuthManager, coach: User, navController: NavController, chatManager: ChatManager) {
    val recentMessages = chatManager.recentMessages.collectAsState().value
    val firstMessage = recentMessages.firstOrNull()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val coachImageSize = screenWidth * 0.3f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surface,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    userManager.chatUser.value = coach
                    navController.navigate("chatScreen")
                    if (firstMessage != null && !firstMessage.viewed) {
                        chatManager.viewStatusTrue()
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coach Image
            Image(
                painter = painterResource(id = R.drawable.coach), // Update to your image resource
                contentDescription = "Coach Image",
                modifier = Modifier
                    .size(coachImageSize)
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = "Health Coach",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Coach Username
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coach.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                    )
                }

                // Coach Status
                if (coach.status.isNotEmpty()) {
                    Text(
                        text = "Coach's status: ${coach.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Recent Messages
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Unread Indicator
                    if (firstMessage != null && !firstMessage.viewed) {
                        Canvas(modifier = Modifier.size(10.dp), onDraw = {
                            drawCircle(
                                color = Color.Blue,
                                center = Offset(x = size.width / 2, y = size.height / 2),
                                radius = size.minDimension / 2
                            )
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Recent Message
                    if (firstMessage != null) {
                        Column {
                            Text(
                                text = "Recent message: ${firstMessage.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = getTimeAgo(firstMessage.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CoachingGuideButton(navController: NavController) {
    Surface(
        modifier = Modifier
            .width(250.dp)
            .padding(vertical = 16.dp)
            .clickable { navController.navigate("coachingGuide") },
        color = MaterialTheme.colorScheme.onSecondary,
        shape = RoundedCornerShape(15.dp),
        elevation = 4.dp // Subtle shadow
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coaching Guidelines",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Coach Guidelines",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun WellbyAutomatedChat(navController: NavController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val imageSize = screenWidth * 0.35f

    Surface(
        elevation = 6.dp, // Subtle shadow
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding for spacing
            .fillMaxWidth(), // Ensure it takes full width
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSecondary
    ) {
        Row(
            modifier = Modifier
                .clickable { navController.navigate("aiChat") }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image/Icon
            Image(
                painter = painterResource(id = R.drawable.chaticon), // Replace with your image name
                contentDescription = "AI Chat Icon",
                modifier = Modifier
                    .size(imageSize) // Dynamically sized image
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Wellby AI",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Ask questions related to your wellbeing goals to get automatic feedback",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Chevron Icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FurtherButtonOptions(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .clickable { navController.navigate("furtherResources") },
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.onSecondary,
            elevation = 8.dp // Adjust elevation here
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Further Resources",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Further",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Resources",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Surface(
            modifier = Modifier
                .padding(8.dp)
                .clickable { navController.navigate("aiChatInfo") },
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.onSecondary,
            elevation = 8.dp // Adjust elevation here
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QuestionAnswer,
                    contentDescription = "AI Information",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Wellby AI",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Info",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

    }
}


@Composable
fun ManageCoachingSettingsButton(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(horizontal = 24.dp)
            .clickable { navController.navigate("settings") },
        color = MaterialTheme.colorScheme.onSecondary,
        shape = RoundedCornerShape(15.dp),
        elevation = 4.dp // Subtle shadow
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opt-in to Coaching in Settings",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}



