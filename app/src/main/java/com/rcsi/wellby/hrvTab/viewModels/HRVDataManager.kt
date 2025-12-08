package com.rcsi.wellby.hrvTab.viewModels
// view model to control the management of heart activity data which is recorded in the app and
// saved and retrieved from Firebase
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Spa
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.rcsi.wellby.hrvTab.RecordingType
import com.rcsi.wellby.hrvTab.models.HRVSessionData
import com.rcsi.wellby.hrvTab.models.DisplayMetric
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HRVDataManager: ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // List to hold raw data batches
    private var rawPpgBatch = mutableListOf<String>()
    private val maxBatchSize = 50

    val initialMetrics = listOf(
        DisplayMetric("Calming Response", Icons.Filled.Spa, "-- ms"),
        DisplayMetric("Return to Balance", Icons.Filled.Balance, "-- ms"),
        DisplayMetric("Heart Rate", Icons.Filled.Favorite, "-- bpm"),
        DisplayMetric("Recording Quality", Icons.Filled.AutoGraph, "--")
    )

    private val _latestMetrics = MutableStateFlow(initialMetrics)
    val latestMetrics = _latestMetrics.asStateFlow()

    private val _stressEstimator = MutableStateFlow<Double?>(null) // Nullable to handle no data cases
    val stressEstimator = _stressEstimator.asStateFlow()

    private val _hrvDataList = MutableStateFlow<List<HRVSessionData>>(emptyList())
    val hrvDataList = _hrvDataList.asStateFlow()

    private val _recordingType = MutableStateFlow<RecordingType?>(null)
    val recordingType = _recordingType.asStateFlow()

    private val firebaseFunctionURL = "https://process-ppg-5zmwi2nzna-uc.a.run.app"

    private val _isProcessingData = MutableStateFlow(false)
    val isProcessingData = _isProcessingData.asStateFlow()

    private var currentUserID = ""

    private val _calibrationRecordCount = MutableStateFlow(0)
    val calibrationRecordCount = _calibrationRecordCount.asStateFlow()

    fun setRecordingType(value: RecordingType?) {
        _recordingType.value = value
    }

    fun fetchLatestHRVData(userID: String) {
        currentUserID = userID

        viewModelScope.launch {
            val hrvDataRef = db.collection("users").document(userID).collection("HRV-inApp")
            hrvDataRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    documents.firstOrNull()?.let { document ->
                        val sessionData = document.toObject(HRVSessionData::class.java).copy(
                            sessionID = document.id // Assign Firestore document ID to sessionID
                        )
                        updateMetrics(sessionData)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HRVDataManager", "Error fetching latest HRV data: ${exception.message}")
                }
        }
    }

    fun fetchHRVData(userID: String, limit: Int = 5) {
        viewModelScope.launch {
            val hrvDataRef = db.collection("users").document(userID).collection("HRV-inApp")
            hrvDataRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    val dataList = documents.map { document ->
                        document.toObject(HRVSessionData::class.java).copy(
                            sessionID = document.id // Assign Firestore document ID to sessionID
                        )
                    }
                    _hrvDataList.value = dataList.distinctBy { it.sessionID } // Ensure no duplicates
                }
                .addOnFailureListener { exception ->
                    Log.e("HRVDataManager", "Error fetching HRV data: ${exception.message}")
                }
        }
    }

    private fun updateMetrics(data: HRVSessionData?) {
        data?.let {
            _latestMetrics.value = listOf(
                DisplayMetric("Calming Response", Icons.Filled.Spa, "${it.formattedRmssd} ms"),
                DisplayMetric("Return to Balance", Icons.Filled.Balance, "${it.formattedSdnn} ms"),
                DisplayMetric("Heart Rate", Icons.Filled.Favorite, "${it.formattedAverageHR} bpm"),
                DisplayMetric("Recording Quality", Icons.Filled.AutoGraph, it.signalQualityLabel)
            )
            _stressEstimator.value = it.stressProbability
        }
    }

//    fun uploadData(sessionData: HRVSessionData) {
//        viewModelScope.launch {
//            try {
//                val docRef = db.collection("users")
//                    .document(sessionData.userID)
//                    .collection("HRV")
//                    .document(sessionData.sessionID)
//                docRef.set(sessionData).addOnSuccessListener {
//                    Log.d("HRVDataManager", "Data uploaded successfully")
//                }.addOnFailureListener { e ->
//                    Log.e("HRVDataManager", "Error uploading data", e)
//                }
//            } catch (e: Exception) {
//                Log.e("HRVDataManager", "Error encoding session data", e)
//            }
//        }
//    }

    fun uploadRawDataToFirebase(sessionID: String, rawData: List<String>) {
        rawPpgBatch.addAll(rawData)

        // Check if the batch has reached the maximum size
        if (rawPpgBatch.size >= maxBatchSize) {
            val batchData = rawPpgBatch.joinToString(",") // Join data with commas
            rawPpgBatch.clear() // Clear the batch after preparing data

            // Reference the Firestore path for raw data
            val docRef = db.collection("users").document(currentUserID)
                .collection("HRV-inApp").document(sessionID)
                .collection("rawData").document()

            // Upload the data with a timestamp
            docRef.set(
                mapOf(
                    "rawData" to batchData,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            ).addOnSuccessListener {
                Log.d("HRVDataManager", "Raw PPG data uploaded successfully.")
            }.addOnFailureListener { e ->
                Log.e("HRVDataManager", "Failed to upload raw PPG data.", e)
            }
        }
    }

    fun remotePpgProcessing(participantID: String, hrvDocumentID: String, onCompletion: (Map<String, Any>?) -> Unit) {
        _isProcessingData.value = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$firebaseFunctionURL?participant_id=$participantID&hrv_document_id=$hrvDocumentID")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)

                    // Check for errors in the JSON response
                    if (jsonObject.has("error")) {
                        val errorMessage = jsonObject.getString("error")
                        Log.e("HRVDataManager", "Error from Firebase function: $errorMessage")
                        onCompletion(null)
                    } else {
                        val result = jsonObject.toMap()
                        onCompletion(result)
                    }
                } else {
                    Log.e("HRVDataManager", "Error calling Firebase function. Response code: $responseCode")
                    onCompletion(null)
                }
            } catch (e: Exception) {
                Log.e("HRVDataManager", "Exception while calling Firebase function: ${e.message}")
                onCompletion(null)
            } finally {
                _isProcessingData.value = false
            }
        }
    }

    // Helper function to convert JSONObject to a Map
    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        keys().forEach { key ->
            map[key] = this.get(key)
        }
        return map
    }

    fun uploadCalibrationData(userId: String, recordingId: String) {
        val userCollection = db.collection("users").document(userId).collection("HRV-inApp")

        // Fetch current calibration count
        if (calibrationRecordCount.value < 4) {
            userCollection
                .whereEqualTo("isCalibration", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val now = Timestamp.now()
                    val latestDoc = querySnapshot.documents.firstOrNull()

                    if (latestDoc != null) {
                        val timestamp = latestDoc.getTimestamp("timestamp")
                        val lastCalibrationDate = timestamp?.toDate()
                        val hoursSinceLastCalibration: Double = lastCalibrationDate?.let {
                            (now.toDate().time - it.time) / (1000.0 * 60 * 60)
                        } ?: Double.MAX_VALUE

                        if (hoursSinceLastCalibration < 12) {
                            Log.d("HRVDataManager", "Calibration update skipped: only $hoursSinceLastCalibration hours since last calibration.")
                            return@addOnSuccessListener
                        }
                    }

                    // If no recent calibration or enough time has passed, set isCalibration to true
                    val docRef = userCollection.document(recordingId)
                    val calibrationData = mapOf(
                        "isCalibration" to true,
                        "timestamp" to now
                    )
                    docRef.set(calibrationData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("HRVDataManager", "isCalibration field added successfully for recording $recordingId")
                            // Update calibration count
                            updateCalibrationRecordCount(userId)
                        }
                        .addOnFailureListener { error ->
                            Log.e("HRVDataManager", "Error setting document with isCalibration: ${error.message}")
                        }
                }
                .addOnFailureListener { error ->
                    Log.e("HRVDataManager", "Error fetching latest calibration entry: ${error.message}")
                }
        } else {
            Log.d("HRVDataManager", "Calibration limit reached; no update required for recording $recordingId")
        }
    }

    private fun updateCalibrationRecordCount(userId: String) {
        val userCollection = db.collection("users").document(userId).collection("HRV-inApp")

        userCollection
            .whereEqualTo("isCalibration", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.documents.size
                _calibrationRecordCount.value = count
                Log.d("HRVDataManager", "Updated calibration record count: $count")
            }
            .addOnFailureListener { error ->
                Log.e("HRVDataManager", "Error updating calibration record count: ${error.message}")
            }
    }

    fun checkCalibrationProgress(userId: String) {
        val hrvDataRef = db.collection("users").document(userId).collection("HRV-inApp")

        hrvDataRef.whereEqualTo("isCalibration", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot?.documents?.size ?: 0
                Log.d("HRVDataManager", "Calibration count: $count")
                _calibrationRecordCount.value = count
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "HRVDataManager",
                    "Error counting calibration documents: ${exception.message}"
                )
                _calibrationRecordCount.value = 0 // Reset to 0 on failure
            }
    }

}