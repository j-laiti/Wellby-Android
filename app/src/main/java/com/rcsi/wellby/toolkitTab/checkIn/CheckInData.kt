package com.rcsi.wellby.toolkitTab.checkIn
// model for outlining the data included in the check-in block
data class CheckInData(
    val mood: String = "",
    val alertness: Int = 0,
    val calmness: Int = 0,
    val moodReason: String? = "",
    val nextAction: String? = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(), // Import Firebase Timestamp
    val isLinkedToRecording: Boolean? = false
)

