package com.rcsi.wellby.resourcesTab
// view model which controls the functions associated with selecting, retrieving, loading, and saving resources
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcsi.wellby.PrivateKeys
import com.rcsi.wellby.resourcesTab.helpers.CacheManager
import com.rcsi.wellby.resourcesTab.helpers.Constants
import com.rcsi.wellby.resourcesTab.helpers.ResourceTopic
import com.rcsi.wellby.resourcesTab.helpers.RetrofitInstance
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ResourceManager: ViewModel() {
    private val _videos = MutableStateFlow<List<ResourceType.Video>>(emptyList()) // Private mutable state flow
    val videos = _videos.asStateFlow() // Public read-only state flow

    private val _images = MutableStateFlow<List<ResourceType.ImageData>>(emptyList()) // Same for images
    val images = _images.asStateFlow()

    private val db = Firebase.firestore

    private val _selectedResource = MutableStateFlow<ResourceType?>(null)
    var selectedResource = _selectedResource.asStateFlow()

    private val _selectedTopic = MutableStateFlow<ResourceTopic?>(null)
    var selectedTopic = _selectedTopic.asStateFlow()

    fun selectResource(resource: ResourceType) {
        _selectedResource.value = resource
    }

    fun selectTopic(topic: ResourceTopic) {
        _selectedTopic.value = topic
    }

    fun getVideos(playlistId: String) {
        retrieveAPIKey { apiKey ->
            if (apiKey.isNotBlank()) {
                viewModelScope.launch {
                    try {
                        val response = RetrofitInstance.youtubeApiService.getPlaylistItems(
                            part = "snippet",
                            playlistId = playlistId,
                            maxResults = 6,
                            apiKey = apiKey
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val videoList = response.body()!!.items.map { it.toVideo() }
                            _videos.value = videoList
                        } else {
                            Log.w("ResourceManager", "API call failed.")
                        }

                    } catch (e: Exception) {
                        Log.w("ResourceManager", "getVideos() failed.", e)
                    }
                }
            }
        }
    }

    /**
     * Retrieves YouTube API key with fallback options:
     * 1. First tries to get from Firebase (for production/shared deployments)
     * 2. Falls back to PrivateKeys.YOUTUBE_API_KEY (for local development)
     *
     * This allows developers to use their own API keys without modifying Firebase
     */
    fun retrieveAPIKey(onResult: (String) -> Unit) {
        db.document("key/youtubeAPI")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val apiKey = document.getString("youtubeAPIKey") ?: ""
                    if (apiKey.isNotBlank()) {
                        onResult(apiKey)
                        return@addOnSuccessListener
                    }
                }
                // Fallback to local PrivateKeys if Firebase doesn't have the key
                Log.d("retrieveAPIKey", "Using local API key from PrivateKeys")
                onResult(PrivateKeys.YOUTUBE_API_KEY)
            }
            .addOnFailureListener { exception ->
                Log.d("retrieveAPIKey", "Firebase fetch failed, using local API key: ${exception.message}")
                // Fallback to local PrivateKeys on Firebase error
                onResult(PrivateKeys.YOUTUBE_API_KEY)
            }
    }



    fun getImages(topic: String) {
        viewModelScope.launch {
            val imageList = mutableListOf<ResourceType.ImageData>()
            val resourcePath = "resources/${topic}/images"

            try {
                val querySnapshot = db.collection(resourcePath)
                    .limit(6)
                    .get()
                    .await()
                for (document in querySnapshot.documents) {
                    val imageData = document.toObject(ResourceType.ImageData::class.java)
                    imageData?.let {

                        CacheManager.getImageCache(it.imageData) ?: run {
                            // Get the storage reference
                            val storageRef = FirebaseStorage.getInstance().reference.child(it.imageData)
                            val url = storageRef.downloadUrl.await()
                            it.imageData = url.toString()

                            // add new image to cache
                            CacheManager.setImageCache(it.imageData, it)
                            imageList.add(it)
                        }
                    }
                }
                _images.value = imageList
            } catch (e: Exception) {
                Log.w("ResourceManager", "getImages() failed.", e)
            }
        }
    }

    fun saveImage(imageData: ResourceType.ImageData, userId: String) {
        viewModelScope.launch {
            val imageDataMap: Map<String, Any> = mapOf(
                "imageData" to imageData.imageData,
                "url" to imageData.url
            )

            try {
                db.collection("users").document(userId)
                    .collection("images").add(imageDataMap)
                Log.d("ResourceManager", "Resource saved!!")
            } catch (e: Exception) {
                Log.w("ResourceManager", "Error sending saved resources: ${e.message}")
            }
        }
    }

    fun saveVideo(videoData: ResourceType.Video, userId: String) {
        viewModelScope.launch {
            val videoDataMap: Map<String, Any> = mapOf(
                "videoId" to videoData.videoId,
                "title" to videoData.title,
                "description" to videoData.description,
                "thumbnail" to videoData.thumbnail,
                "published" to videoData.published
            )

            try {
                db.collection("users").document(userId)
                    .collection("videos").add(videoDataMap)
                Log.d("ResourceManager", "Resource saved!!")
            } catch (e: Exception) {
                Log.w("ResourceManager", "Error sending saved resources: ${e.message}")
            }
        }
    }

    fun loadSavedImages(userId: String) {
        viewModelScope.launch {
            val imageList = mutableListOf<ResourceType.ImageData>()
            try {
                val querySnapshot = db.collection("users").document(userId)
                    .collection("images").get().await()

                for (document in querySnapshot.documents) {
                    val imageData = document.toObject(ResourceType.ImageData::class.java)
                    imageData?.let { imageList.add(it) }
                }

                _images.value = imageList
            } catch (e: Exception) {
                Log.w("ResourceManager", "Error getting saved images: ${e.message}")
            }
        }
    }

    fun loadSavedVideos(userId: String) {
        viewModelScope.launch {
            val videoList = mutableListOf<ResourceType.Video>()
            try {
                val querySnapshot = db.collection("users").document(userId)
                    .collection("videos").get().await()

                for (document in querySnapshot.documents) {
                    val videoData = document.toObject(ResourceType.Video::class.java)
                    videoData?.let { videoList.add(it) }
                }

                _videos.value = videoList
            } catch (e: Exception) {
                Log.w("ResourceManager", "Error getting saved images: ${e.message}")
            }
        }
    }

    suspend fun isResourceSaved(id: String, userId: String, isImage: Boolean): Boolean {
        val resourceType = if (isImage) "images" else "videos"
        val fieldName = if (isImage) "imageData" else "videoId"

        return try {
            val querySnapshot = db.collection("users").document(userId)
                .collection(resourceType).whereEqualTo(fieldName, id).get().await()

            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.w("ResourceManager", "Error checking for saved resource: ${e.message}")
            false
        }
    }

    fun deleteSavedResource(id: String, userId: String, isImage: Boolean) {
        viewModelScope.launch {
            val resourceType = if (isImage) "images" else "videos"
            val fieldName = if (isImage) "path" else "videoId"

            try {
                val querySnapshot = db.collection("users").document(userId)
                    .collection(resourceType).whereEqualTo(fieldName, id).get().await()

                for (document in querySnapshot.documents) {
                    document.reference.delete()
                }
                Log.d("ResourceManager", "Resource deleted")
            } catch (e: Exception) {
                Log.w("ResourceManager", "Error deleting saved resource: ${e.message}")
            }
        }
    }


}