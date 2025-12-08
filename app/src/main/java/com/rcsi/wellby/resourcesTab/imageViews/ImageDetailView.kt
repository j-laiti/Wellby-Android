package com.rcsi.wellby.resourcesTab.imageViews
// detail view for the selected video or image which includes the video and description or the image
// and associated URL

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.rcsi.wellby.resourcesTab.ResourceManager
import com.rcsi.wellby.resourcesTab.ResourceType
import com.rcsi.wellby.resourcesTab.videoViews.VideoDetailView
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun DetailView(resourceManager: ResourceManager, userManager: AuthManager) {
    when (val selectedResource = resourceManager.selectedResource.collectAsState().value) {
        is ResourceType.Video -> {
            VideoDetailView(resourceManager, selectedResource, userManager)
        }
        is ResourceType.ImageData -> {
            ImageDetailView(resourceManager, selectedResource, userManager)
        }
        else -> {
            Text("No selected resource")
        }
    }
}

@Composable
fun ImageDetailView(resourceManager: ResourceManager, image: ResourceType.ImageData, userManager: AuthManager) {
    val context = LocalContext.current
    val userId = userManager.currentUser.collectAsState().value?.id ?: ""

    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(image) {
        isSaved = resourceManager.isResourceSaved(image.imageData, userId, true)
        userManager.viewDidAppear("Image Detail")
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ){

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    if (isSaved) {
                        resourceManager.deleteSavedResource(image.imageData, userId, true)
                    } else {
                        resourceManager.saveImage(image, userId)
                    }
                    isSaved = !isSaved
                }) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.CheckCircle else Icons.Filled.Add,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }


            // display the selected image
            val painter = rememberAsyncImagePainter(image.imageData)
            Image(
                painter = painter,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    //navigate to website
                    image.url.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Open website")
            }

            Spacer(modifier = Modifier.height(100.dp))

        }
    }

}