package com.rcsi.wellby.toolkitTab.checkIn
// view model for managing the operations associated with the check-in block including saving
// the check-in data to firebase and retrieving saved entries to display in a summary screen
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CheckInManager: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _checkInEntries = MutableStateFlow<List<CheckInData>>(emptyList())
    val checkInEntries = _checkInEntries.asStateFlow()

    private var lastVisible: DocumentSnapshot? = null
    private var firstVisible: DocumentSnapshot? = null

    private val _canFetchPrevious = MutableStateFlow(false)
    val canFetchPrevious = _canFetchPrevious.asStateFlow()

    // Note: Initialize this to false because the initial fetch gets the latest entries
    private val _canFetchNext = MutableStateFlow(false)
    val canFetchNext = _canFetchNext.asStateFlow()

    fun saveCheckInData(checkIn: CheckInData, userId: String) {
        val document = db.collection("users").document(userId).collection("checkIns").document()
        document.set(checkIn)
            .addOnSuccessListener { Log.d("CheckInManager", "Check-in successfully saved!") }
            .addOnFailureListener { e -> Log.w("CheckInManager", "Error saving check-in", e) }
    }

    fun fetchCheckInEntries(userId: String) {
        db.collection("users").document(userId).collection("checkIns")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val entries = documents.mapNotNull { it.toObject(CheckInData::class.java) }
                    _checkInEntries.value = entries
                    lastVisible = documents.documents.last()
                    firstVisible = documents.documents.first()
                    updateBackNav(userId)
                }
            }
            .addOnFailureListener { e ->
                Log.w("CheckInManager", "Error fetching check-ins", e)
            }
    }

    private fun updateBackNav(userId: String) {
        lastVisible?.let {
            db.collection("users").document(userId).collection("checkIns")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(it)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    _canFetchPrevious.value = documents.size() > 0
                }
        }
    }

    fun fetchPreviousCheckInEntries(userId: String) {
        lastVisible?.let {
            db.collection("users").document(userId).collection("checkIns")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(it)
                .limit(5)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val entries = documents.mapNotNull { it.toObject(CheckInData::class.java) }
                        _checkInEntries.value = entries
                        lastVisible = documents.documents.last()
                        firstVisible = documents.documents.first()
                        updateBackNav(userId)
                        _canFetchNext.value = true
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("CheckInManager", "Error fetching next check-ins", e)
                }
        }
    }

    private fun updateNextNav(userId: String) {
        firstVisible?.let {
            db.collection("users").document(userId).collection("checkIns")
                .orderBy("date", Query.Direction.DESCENDING)
                .endBefore(it)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    _canFetchNext.value = documents.size() > 0
                }
        }
    }

    fun fetchNextCheckInEntries(userId: String) {
        firstVisible?.let {
            db.collection("users").document(userId).collection("checkIns")
                .orderBy("date", Query.Direction.DESCENDING)
                .endBefore(it)
                .limitToLast(5) // This will fetch the documents before the first visible
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val entries = documents.mapNotNull { it.toObject(CheckInData::class.java) }
                        _checkInEntries.value = entries
                        lastVisible = documents.documents.last() // The 'oldest' document
                        firstVisible = documents.documents.first() // The 'newest' document in this set
                        updateNextNav(userId)
                        _canFetchPrevious.value = true
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("CheckInManager", "Error fetching previous check-ins", e)
                }
        }
    }


}
