package com.rcsi.wellby.resourcesTab.helpers
// youtube api service for retrieving a list of videos for each resource topic
import com.rcsi.wellby.resourcesTab.YTResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String,
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int,
        @Query("key") apiKey: String
    ): Response<YTResponse> // Ensure this return type matches your YouTube response model
}