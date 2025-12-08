package com.rcsi.wellby.resourcesTab
// view of resources that the user has specifically saved for themselves.
// This list is stored in Firebase and the saved resources can be accessed in the home screen.
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rcsi.wellby.resourcesTab.imageViews.ImagesList
import com.rcsi.wellby.resourcesTab.videoViews.VideosList
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun SavedResourcesView(resourceManager: ResourceManager, navController: NavController, userManager: AuthManager) {
    val userId = userManager.currentUser.collectAsState().value?.id ?: ""

    LaunchedEffect(key1 = resourceManager) {
        resourceManager.loadSavedImages(userId)
        resourceManager.loadSavedVideos(userId)
        userManager.viewDidAppear("Saved Resources")
    }

    val videos = resourceManager.videos.collectAsState().value
    val images = resourceManager.images.collectAsState().value

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Saved Resources",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        SectionTitle(title = "Videos")

        VideosList(
            videos = videos,
            resourceManager = resourceManager,
            onVideoClick =  {
                navController.navigate("resourceDetail") },
            videoHeight = 180.dp
        )

        SectionTitle(title = "Images")

        ImagesList(resourceManager, navController, images)
    }
}