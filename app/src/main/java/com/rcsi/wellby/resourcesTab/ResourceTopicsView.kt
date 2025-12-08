package com.rcsi.wellby.resourcesTab
// main screen of the resources tab which includes the most recently viewed resource and the resource topics to browse
import com.rcsi.wellby.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.rcsi.wellby.resourcesTab.helpers.ResourceTopic
import com.rcsi.wellby.resourcesTab.videoViews.VideoPreview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun ResourceTopicsView (resourceManager: ResourceManager, navController: NavController, userManager: AuthManager) {

    val topicIcons = mapOf(
        ResourceTopic.STRESS to Icons.Filled.Spa, // Example, replace with actual icons
        ResourceTopic.SLEEP to Icons.Filled.NightlightRound,
        ResourceTopic.TIME_MANAGEMENT to Icons.Filled.Schedule,
        ResourceTopic.DIGITAL_WELLBEING to Icons.Filled.Devices,
        ResourceTopic.OTHER to Icons.Filled.More
    )

    LaunchedEffect(Unit) {
        userManager.viewDidAppear("Resources Overview")
    }

    Column (
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.White),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY  // This makes the gradient stretch to the bottom
                )
            )
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Resources",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recently Viewed:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 16.dp)) {
            RecentlyViewedResource(
                resourceManager = resourceManager,
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Explore Topics:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.weight(1.5f).fillMaxWidth()) {
            ResourceTopic.entries.forEach { topic ->
                val buttonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary, // Button background color
                    contentColor = MaterialTheme.colorScheme.onBackground // Text and icon color inside the button
                )

                Button(
                    onClick = {
                        resourceManager.selectTopic(topic)
                        resourceManager.getVideos(topic.playlistId)
                        resourceManager.getImages(topic.imageTag)
                        navController.navigate("topic")
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(250.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = buttonColors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = topicIcons[topic]
                                ?: Icons.Default.Info, // Default to a generic icon if none mapped
                            contentDescription = "${topic.displayName} icon",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = topic.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }


    }

}

@Composable
fun RecentlyViewedResource (resourceManager: ResourceManager, navController: NavController, modifier: Modifier = Modifier) {
    when (val selectedResource = resourceManager.selectedResource.collectAsState().value) {
        is ResourceType.Video -> {
            VideoPreview(
                video = selectedResource,
                onClick = { navController.navigate("resourceDetail") },
                videoHeight = 175.dp
            )
        }
        is ResourceType.ImageData -> {
            val painter = rememberAsyncImagePainter(selectedResource.imageData)
            Image(
                painter = painter,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { navController.navigate("resourceDetail") },
                contentScale = ContentScale.Fit
            )
        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.explore),
                contentDescription = "Static Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .size(150.dp)
            )
        }
    }
}
