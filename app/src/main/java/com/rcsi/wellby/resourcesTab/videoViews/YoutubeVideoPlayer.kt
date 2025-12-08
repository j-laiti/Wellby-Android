package com.rcsi.wellby.resourcesTab.videoViews
// youtube player view for watching the retrieved and selected video
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayerCompose(
    videoId: String,
    modifier: Modifier = Modifier,
    onVideoClick: () -> Unit
) {
    AndroidView(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clickable { onVideoClick() },
        factory = { context ->
            YouTubePlayerView(context).apply {
                // Add the YouTubePlayer as a lifecycle observer to handle its lifecycle
                (context as? LifecycleOwner)?.lifecycle?.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                })
            }
        }
    )
}



