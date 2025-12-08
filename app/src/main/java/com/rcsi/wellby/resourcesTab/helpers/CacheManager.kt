package com.rcsi.wellby.resourcesTab.helpers
// draft code to cache resources so that they dont have to be reloaded

import com.rcsi.wellby.resourcesTab.ResourceType

object CacheManager {

    // Video previews cache
    private var videoCache = mutableMapOf<String, ResourceType.Video>()

    fun setVideoCache(videoId: String, data: ResourceType.Video?) {
        data?.let {
            videoCache[videoId] = it
        }
    }

    fun getVideoCache(videoId: String): ResourceType.Video? {
        return videoCache[videoId]
    }

    // Image data cache
    private var imageCache = mutableMapOf<String, ResourceType.ImageData>()

    fun setImageCache(path: String, imageData: ResourceType.ImageData) {
        imageCache[path] = imageData
    }

    fun getImageCache(path: String): ResourceType.ImageData? {
        return imageCache[path]
    }
}
