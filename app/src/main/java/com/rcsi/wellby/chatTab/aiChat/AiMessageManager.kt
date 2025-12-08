package com.rcsi.wellby.chatTab.aiChat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rcsi.wellby.PrivateKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AiMessagesManager(private val userId: String) : ViewModel() {

    private val db = Firebase.firestore

    // State variables
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(emptyList())
    val aiMessages = _aiMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRunActive = MutableStateFlow(false)
    val isRunActive = _isRunActive.asStateFlow()

    private var threadID: String? = null

    init {
        createThreadAndFetchMessages()
    }

    private fun createThreadAndFetchMessages() {
        // Check Firestore for an existing thread ID
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = db.collection("users").document(userId).collection("thread")
                    .document(userId).get().await()

                val existingThreadID = document.getString("threadID")
                if (existingThreadID != null) {
                    threadID = existingThreadID
                    fetchMessagesFromThread(existingThreadID, PrivateKeys.OPENAI_API_KEY)
                } else {
                    // Create a new thread if not found
                    val newThreadID: String? = createThread(PrivateKeys.OPENAI_API_KEY)
                    if (newThreadID != null) {
                        threadID = newThreadID
                        saveThreadToFirestore(newThreadID)
                        fetchMessagesFromThread(newThreadID, PrivateKeys.OPENAI_API_KEY)
                    }

                }
            } catch (e: Exception) {
                Log.e("AiMessagesManager", "Error creating or fetching thread: ${e.message}")
            }
        }
    }

    private fun fetchMessagesFromThread(threadID: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = fetchMessagesFromThreadApi(threadID, apiKey)
            if (messages != null) {
                val recentMessages = messages.takeLast(20).map {
                    AiMessage.fromChatGPTMessage(it)
                }.reversed()

                withContext(Dispatchers.Main) {
                    _aiMessages.value = recentMessages
                }
            }
        }
    }

    private fun saveThreadToFirestore(threadID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val threadData = mapOf("threadID" to threadID)
                db.collection("users").document(userId).collection("thread")
                    .document(userId).set(threadData).await()
            } catch (e: Exception) {
                Log.e("AiMessagesManager", "Error saving thread to Firestore: ${e.message}")
            }
        }
    }

    fun sendMessage(text: String) {
        if (threadID == null || isRunActive.value) {
            Log.e("AiMessagesManager", "Thread not initialized or run already active")
            return
        }

        // Add the sent message locally
        val newMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            received = false
        )
        _aiMessages.value = _aiMessages.value + newMessage

        // Send the message to ChatGPT
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { _isLoading.value = true }

                // Moderate the message
                val (flagged, categories) = moderateMessage(text)
                if (flagged) {
                    handleFlaggedMessage(text, categories)
                } else {
                    // Send the message if it's not flagged
                    val success = sendMessageInThread(threadID!!, text, role = "user", PrivateKeys.OPENAI_API_KEY)
                    if (success) {
                        createRunAndPoll(threadID!!, text)
                    } else {
                        withContext(Dispatchers.Main) { _isLoading.value = false }
                    }
                }
            } catch (e: Exception) {
                Log.e("AiMessagesManager", "Error sending message: ${e.message}")
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    private fun createRunAndPoll(threadID: String, text: String) {
        if (isRunActive.value) {
            Log.e("AiMessagesManager", "Run is already active")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isRunActive.value = true
            val runID = createRun(threadID, PrivateKeys.OPENAI_API_KEY)
            if (runID != null) {
                pollRun(threadID, runID, PrivateKeys.OPENAI_API_KEY)
            }
            _isRunActive.value = false
        }
    }

    private suspend fun createThread(apiKey: String): String? {
        val url = URL("https://api.openai.com/v1/threads")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2")

        return try {
            connection.doOutput = true
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getString("id") // Extract thread ID
            } else {
                Log.e("AiMessagesManager", "Error creating thread: HTTP $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Exception creating thread: ${e.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun sendMessageInThread(threadID: String, message: String, role: String = "user", apiKey: String): Boolean {
        val url = URL("https://api.openai.com/v1/threads/$threadID/messages")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2")

        val requestBody = JSONObject().apply {
            put("role", role)
            put("content", message)
        }

        return try {
            connection.doOutput = true
            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("AiMessagesManager", "Message sent successfully.")
                true
            } else {
                Log.e("AiMessagesManager", "Error sending message: HTTP $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Exception sending message: ${e.message}")
            false
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchMessagesFromThreadApi(threadID: String, apiKey: String): List<ChatGPTMessage>? {
        val url = URL("https://api.openai.com/v1/threads/$threadID/messages")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2")

        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val messagesJson = jsonResponse.getJSONArray("data")
                (0 until messagesJson.length()).map { i ->
                    val messageJson = messagesJson.getJSONObject(i)
                    ChatGPTMessage(
                        role = messageJson.getString("role"),
                        content = messageJson.getJSONArray("content").let { contentArray ->
                            (0 until contentArray.length()).map { j ->
                                val contentJson = contentArray.getJSONObject(j)
                                ChatGPTContent(
                                    type = contentJson.getString("type"),
                                    text = ChatGPTText(
                                        value = contentJson.getJSONObject("text").getString("value"),
                                        annotations = emptyList() // Adjust if annotations are needed
                                    )
                                )
                            }
                        }
                    )
                }
            } else {
                Log.e("AiMessagesManager", "Error fetching messages: HTTP $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Exception fetching messages: ${e.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun createRun(threadID: String, apiKey: String): String? {
        val url = URL("https://api.openai.com/v1/threads/$threadID/runs")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2")

        val requestBody = JSONObject().apply {
            put("assistant_id", "asst_DWHGiX09ixHu76jtmUfLMPoc")
        }

        return try {
            connection.doOutput = true
            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getString("id") // Extract run ID
            } else {
                Log.e("AiMessagesManager", "Error creating run: HTTP $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Exception creating run: ${e.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun pollRun(threadID: String, runID: String, apiKey: String) {
        val maxRetries = 30
        var retryCount = 0
        var pollCompleted = false

        while (!pollCompleted && retryCount < maxRetries) {
            val url = URL("https://api.openai.com/v1/threads/$threadID/runs/$runID")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2")

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    Log.d("AiMessagesManager", "Run status: $status")

                    if (status == "completed") {
                        val messages = fetchMessagesFromThreadApi(threadID, apiKey)
                        val assistantResponse = messages?.firstOrNull { it.role == "assistant" }
                        if (assistantResponse != null) {
                            val content = assistantResponse.content.firstOrNull()?.text?.value ?: ""
                            withContext(Dispatchers.Main) {
                                Log.d("AiMessagesManager", "New message received: $content")
                                _aiMessages.value = _aiMessages.value + AiMessage(
                                    id = UUID.randomUUID().toString(),
                                    text = content,
                                    received = true
                                )
                                _isLoading.value = false
                            }
                        }
                        pollCompleted = true
                    } else if (status == "pending" || status == "running" || status == "in_progress") {
                        retryCount++
                        Thread.sleep(2000) // Wait before polling again
                    } else {
                        Log.e("AiMessagesManager", "Run status: $status")
                        pollCompleted = true
                    }
                }
            } catch (e: Exception) {
                Log.e("AiMessagesManager", "Error polling run: ${e.message}")
                pollCompleted = true
            } finally {
                connection.disconnect()
            }
        }
        if (retryCount >= maxRetries) {
            Log.e("AiMessagesManager", "Polling timed out after $maxRetries retries.")
            withContext(Dispatchers.Main) {
                _aiMessages.value = _aiMessages.value + AiMessage(
                    id = UUID.randomUUID().toString(),
                    text = "The assistant could not respond in time. Please try again.",
                    received = true
                )
                _isLoading.value = false
            }
        }
    }

    private suspend fun moderateMessage(
        message: String
    ): Pair<Boolean, Map<String, Boolean>?> {
        val url = URL("https://api.openai.com/v1/moderations")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer ${PrivateKeys.OPENAI_API_KEY}")
        connection.setRequestProperty("Content-Type", "application/json")

        val requestBody = JSONObject().apply {
            put("input", message)
        }

        return try {
            connection.doOutput = true
            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val results = jsonResponse.getJSONArray("results").getJSONObject(0)
                val flagged = results.getBoolean("flagged")
                val categories = results.getJSONObject("categories").toMap()
                Pair(flagged, categories)
            } else {
                Log.e("AiMessagesManager", "Moderation API Error: HTTP $responseCode")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Moderation Exception: ${e.message}")
            Pair(false, null)
        } finally {
            connection.disconnect()
        }
    }

    // Helper extension to convert JSONObject to Map
    private fun JSONObject.toMap(): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        keys().forEach { key ->
            map[key] = getBoolean(key)
        }
        return map
    }

    private fun Date.toISO8601String(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(this)
    }

    private suspend fun callFirebaseFunction(
        messageData: Map<String, Any?>
    ): Result<String> {
        val firebaseFunctionUrl = "https://send-flagged-message-notification-5zmwi2nzna-uc.a.run.app"

        val sanitizedData = messageData.toMutableMap().apply {
            // Convert Timestamp to ISO8601 format if present
            val timestamp = messageData["timestamp"] as? Timestamp
            if (timestamp != null) {
                this["timestamp"] = timestamp.toDate().toISO8601String()
            }
        }

        return try {
            val url = URL(firebaseFunctionUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")

            val requestBody = JSONObject(sanitizedData as Map<String, *>).toString()
            connection.doOutput = true
            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Result.success(response)
            } else {
                Log.e("AiMessagesManager", "Firebase Function Error: HTTP $responseCode")
                Result.failure(Exception("HTTP Error: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Firebase Function Exception: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun handleFlaggedMessage(
        text: String,
        categories: Map<String, Boolean>?
    ) {
        // Add flagged message to Firestore
        val flaggedMessage = mapOf(
            "userID" to userId,
            "message" to text,
            "categories" to categories,
            "timestamp" to Timestamp.now()
        )

        try {
            db.collection("flagged").add(flaggedMessage).await()
            Log.d("AiMessagesManager", "Flagged message saved successfully.")

            // Invoke Firebase Function
            val result = callFirebaseFunction(flaggedMessage)
            if (result.isSuccess) {
                Log.d("AiMessagesManager", "Firebase Function Response: ${result.getOrNull()}")
            } else {
                Log.e("AiMessagesManager", "Firebase Function Error: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e("AiMessagesManager", "Error saving flagged message or invoking function: ${e.message}")
        }

        // Add flagged response to the conversation
        val flaggedResponse = "It sounds like you're going through a really tough time. It's important to talk to someone who can help, like a friend, family member, or a professional. You don't have to go through this alone. Please consider reaching out for support. You can also find helpful resources linked on the previous screen. For your safety, this message has been flagged for review."

        withContext(Dispatchers.Main) {
            _aiMessages.value = _aiMessages.value + AiMessage(
                id = UUID.randomUUID().toString(),
                text = flaggedResponse,
                received = true
            )
            _isLoading.value = false
        }

        // Send flagged response to the thread
        sendMessageInThread(threadID!!, flaggedResponse, role = "assistant", apiKey = PrivateKeys.OPENAI_API_KEY)
    }

}