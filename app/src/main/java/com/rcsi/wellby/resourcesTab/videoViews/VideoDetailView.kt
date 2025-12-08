package com.rcsi.wellby.resourcesTab.videoViews
// view displayed when the video is selected which includes the video title and description

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.resourcesTab.ResourceManager
import com.rcsi.wellby.resourcesTab.ResourceType
import com.rcsi.wellby.signinSystem.AuthManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VideoDetailView(resourceManager: ResourceManager, video: ResourceType.Video, userManager: AuthManager) {
    var isSaved by remember { mutableStateOf(false) }
    val userId = userManager.currentUser.collectAsState().value?.id ?: ""

    // Date formatting
    val sdf = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = video.published.let { sdf.format(it) } ?: ""

    LaunchedEffect(video) {
        isSaved = resourceManager.isResourceSaved(video.videoId, userId, false)
        userManager.viewDidAppear("Video Detail")
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background)
//                )
//            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    isSaved = !isSaved
                    if (isSaved) {
                        resourceManager.saveVideo(video, userId)
                    } else {
                        resourceManager.deleteSavedResource(video.videoId, userId, false)
                    }
                }) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.CheckCircle else Icons.Filled.Add,
                        contentDescription = if (isSaved) "Unsave" else "Save"
                    )
                }
            }

            Text(
                text = video.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )

            // Assuming YouTubePlayerCompose maintains aspect ratio; replace with your video player composable
            YouTubePlayerCompose(
                videoId = video.videoId,
                onVideoClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(8.dp)
            )

            Box(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
