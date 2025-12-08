package com.rcsi.wellby.resourcesTab
// view displayed when a specific resource topic is selected.
// this includes a list of videos and image previews related to the topic
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.resourcesTab.imageViews.ImagesList
import com.rcsi.wellby.resourcesTab.videoViews.VideosList
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun TopicView(resourceManager: ResourceManager, navController: NavController, userManager: AuthManager) {

    val videos = resourceManager.videos.collectAsState().value
    val images = resourceManager.images.collectAsState().value
    val selectedTopic = resourceManager.selectedTopic.collectAsState().value

    LaunchedEffect(Unit) {
        userManager.viewDidAppear("Resource Topic")
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedTopic != null) {
            Text(
                text = selectedTopic.displayName,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        SectionTitle(title = "Videos")

        VideosList(
            videos = videos,
            resourceManager = resourceManager,
            onVideoClick =  {
                navController.navigate("resourceDetail") },
            videoHeight = 150.dp
        )

        SectionTitle(title = "Images")

        ImagesList(resourceManager, navController, images)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Start
    )
}