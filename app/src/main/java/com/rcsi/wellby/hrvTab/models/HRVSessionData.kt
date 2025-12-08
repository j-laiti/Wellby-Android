package com.rcsi.wellby.hrvTab.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class HRVSessionData(
    val sessionID: String = "", // Firestore document ID

    @get:PropertyName("sdnn") @set:PropertyName("sdnn")
    var sdnn: Double = 0.0, // Expecting Double from Firestore

    @get:PropertyName("rmssd") @set:PropertyName("rmssd")
    var rmssd: Double = 0.0, // Expecting Double from Firestore

    @get:PropertyName("HR_mean") @set:PropertyName("HR_mean")
    var HR_mean: Double = 0.0, // Expecting Double from Firestore

    @get:PropertyName("sqi") @set:PropertyName("sqi")
    var sqi: Double = -1.0, // Expecting Double from Firestore

    @get:PropertyName("stress_probability") @set:PropertyName("stress_probability")
    var stressProbability: Double = 0.5,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Timestamp = Timestamp.now()
) {
    // Computed properties for formatted display
    val formattedSdnn: String
        get() = "%.1f".format(sdnn)

    val formattedRmssd: String
        get() = "%.1f".format(rmssd)

    val formattedAverageHR: String
        get() = "%.1f".format(HR_mean)

    val signalQualityLabel: String
        get() = when (sqi) {
            in 0.0..0.3 -> "Low"
            in 0.3..0.7 -> "Good"
            in 0.7..1.0 -> "Excellent"
            else -> "--"
        }
}