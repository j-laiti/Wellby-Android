package com.rcsi.wellby.resourcesTab.imageViews
// grid layout for the image resources in each resource topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.rcsi.wellby.resourcesTab.ResourceManager
import com.rcsi.wellby.resourcesTab.ResourceType

@Composable
fun ImageItem(imageData: ResourceType.ImageData, onClick: () -> Unit) {
    val painter = rememberAsyncImagePainter(imageData.imageData)
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter, //way too confusing, but for now I named the firestore path image data as well
            contentDescription = null,
            modifier = Modifier
                .sizeIn(maxHeight = 400.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.FillWidth // Crop the image if necessary to fit the size
        )
    }
}


@Composable
fun ImagesList(
    resourceManager: ResourceManager,
    navController: NavController,
    images: List<ResourceType.ImageData>) {

    // TODO: this makes the columns scroll independently. Might need to change back to grid

    Row {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(images.chunked(2).mapNotNull { it.getOrNull(0) }) { imageData ->
                // TODO: add button w navigation to the detail view and set the selected image
                ImageItem(
                    imageData = imageData,
                    onClick = {
                        resourceManager.selectResource(imageData)
                        navController.navigate("resourceDetail")
                    }
                )
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(images.chunked(2).mapNotNull { it.getOrNull(1) }) { imageData ->
                ImageItem(
                    imageData = imageData,
                    onClick = {
                        resourceManager.selectResource(imageData)
                        navController.navigate("resourceDetail")
                    }
                )
            }
        }
    }

}
