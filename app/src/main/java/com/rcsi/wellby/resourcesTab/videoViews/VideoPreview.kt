package com.rcsi.wellby.resourcesTab.videoViews
// view preview of a video displayed in the overall summary of resources for a specific topic.
// this includes just the thumbnail image for the video and can be selected for the detail view
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.rcsi.wellby.resourcesTab.ResourceManager
import com.rcsi.wellby.resourcesTab.ResourceType

@Composable
fun VideosList(
    videos: List<ResourceType.Video>,
    resourceManager: ResourceManager,
    onVideoClick: (ResourceType.Video) -> Unit,
    videoHeight: Dp = 200.dp ) {
    LazyRow {
        items(videos) { video ->
            VideoPreview(video = video, onClick = {
                resourceManager.selectResource(video)
                onVideoClick(video)
            }, videoHeight = videoHeight)
        }
    }
}

@Composable
fun VideoPreview(video: ResourceType.Video, onClick: () -> Unit, videoHeight: Dp) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .width(videoHeight * (16f/9f))
        ) {
            Image(
                painter = rememberAsyncImagePainter(video.thumbnail),
                contentDescription = "Video thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .width(videoHeight * (16f/9f))
                    .height(videoHeight)
                    .clip(RoundedCornerShape(10.dp)), // Standard aspect ratio for YouTube videos
                contentScale = ContentScale.Crop
            )

            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray), // Change text style and color
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
        }
    }
}
