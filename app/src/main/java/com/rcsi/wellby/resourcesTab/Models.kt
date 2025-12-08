package com.rcsi.wellby.resourcesTab
// model for both types or resources, video and image

import java.text.SimpleDateFormat
import java.util.*

sealed class ResourceType {
    data class Video(
        var videoId: String = "",
        var title: String = "",
        var description: String = "",
        var thumbnail: String = "",
        var published: Date = Date() //will probably need to reformat this since it's handled differently than in iOS
    ) : ResourceType()

    data class ImageData(
        var imageData: String = "",
        var url: String = ""
    ) : ResourceType()
}

data class YTResponse(
    val items: List<Item>
)

data class Item(
    val snippet: Snippet
)

data class Snippet(
    val publishedAt: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val resourceId: ResourceId
)

data class Thumbnails(
    val high: High
)

data class High(
    val url: String
)

data class ResourceId(
    val videoId: String
)

// Convert this structure into your Video class for use in your app.
fun Item.toVideo(): ResourceType.Video {
    return ResourceType.Video(
        videoId = this.snippet.resourceId.videoId,
        title = this.snippet.title,
        description = this.snippet.description,
        thumbnail = this.snippet.thumbnails.high.url,
        published = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(this.snippet.publishedAt) ?: Date()
    )
}



