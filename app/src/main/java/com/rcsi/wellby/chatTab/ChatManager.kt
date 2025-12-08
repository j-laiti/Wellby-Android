package com.rcsi.wellby.chatTab

// Chat tab view model which keeps track of message operations and information

import android.util.Log
import androidx.lifecycle.ViewModel
import com.rcsi.wellby.signinSystem.AuthManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ChatManager(
    private val userManager: AuthManager
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _recentMessages = MutableStateFlow<List<RecentMessage>>(emptyList())
    val recentMessages = _recentMessages.asStateFlow()

    private val db = Firebase.firestore

    fun sendMessage(message: String) {
        val currentUser = userManager.currentUser.value
        val chatUser = userManager.chatUser.value

        if (message.isBlank() || currentUser?.id == null || chatUser?.id == null) return

        val newMessage = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "currentUserID" to currentUser.id,
            "receiverID" to chatUser.id,
            "text" to message,
            "timestamp" to Timestamp.now()
        )

        // Path for sender
        val senderPath = "messages/${currentUser.id}/${chatUser.id}"
        db.collection(senderPath).add(newMessage)

        // Path for receiver
        val receiverPath = "messages/${chatUser.id}/${currentUser.id}"
        db.collection(receiverPath).add(newMessage)

        // Update recent messages
        saveLastMessage(message, currentUser.id, chatUser.id)
    }

    private fun saveLastMessage(message: String, senderId: String, receiverId: String) {
        val currentUser = userManager.currentUser.value ?: return
        val chatUser = userManager.chatUser.value ?: return

        val senderName = currentUser.username
        val receiverName = chatUser.username

        // Data for the sender's view
        val lastMessageForSender = mapOf(
            "name" to receiverName,
            "timestamp" to Timestamp.now(),
            "message" to message,
            "currentID" to senderId,
            "chatUserID" to receiverId,
            "viewed" to true
        )

        // Update for the sender
        db.collection("recent_messages/$senderId/messages").document(receiverId)
            .set(lastMessageForSender)

        // Data for the receiver's view, note the swap of 'currentID' and 'chatUserID'
        val lastMessageForReceiver = mapOf(
            "name" to senderName,
            "timestamp" to Timestamp.now(),
            "message" to message,
            "currentID" to receiverId,
            "chatUserID" to senderId,
            "viewed" to false
        )

        // Update for the receiver
        db.collection("recent_messages/$receiverId/messages").document(senderId)
            .set(lastMessageForReceiver)
    }


    fun fetchMessages() {
        val currentUser = userManager.currentUser.value
        val chatUser = userManager.chatUser.value

        if (currentUser?.id == null || chatUser?.id == null) return

        db.collection("messages")
            .document(currentUser.id)
            .collection(chatUser.id)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w("ChatManager", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val fetchedMessages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.also {
                        Log.w("ChatManager", "successful")
                    } ?: run {
                        // Document conversion failed
                        Log.w("ChatManager", "Failed to convert doc: ${doc.id}")
                        null
                    }
                }

                _messages.value = fetchedMessages
            }
    }

    fun fetchRecentMessages() {
        val currentUser = userManager.currentUser.value

        if (currentUser?.id != null) {
            db.collection("recent_messages/${currentUser.id}/messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("ChatManager", "Error fetching recent messages", e)
                        return@addSnapshotListener
                    }

                    val fetchedMessages = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(RecentMessage::class.java)
                        } catch (exception: Exception) {
                            Log.w("ChatManager", "Error converting document to RecentMessage", exception)
                            null
                        }
                    } ?: emptyList()

                    // Update the state flow with the new list of messages
                    _recentMessages.value = fetchedMessages
                }
        }
    }

    fun messageAllStudents(message: String) {
        val users = userManager.userList.value

        users.forEach { user ->
            userManager.chatUser.value = user
            sendMessage(message)
        }
    }

    fun viewStatusTrue() {
        val currentUser = userManager.currentUser.value
        val chatUser = userManager.chatUser.value

        if (currentUser?.id == null || chatUser?.id == null) return

        db.collection("recent_messages/${currentUser.id}/messages").document(chatUser.id)
            .update("viewed", true)
            .addOnFailureListener { e ->
                Log.w("ChatManager", "Error updating message view status", e)
            }
    }

    fun reportObjectionableContent() {
        val currentUser = userManager.currentUser.value ?: return
        val chatUser = userManager.chatUser.value ?: return

        // Ensure we have valid user IDs to work with
        if (currentUser.id.isBlank() || chatUser.id.isBlank()) {
            Log.d("ChatManager", "CurrentUserID or ReceiverID is empty. Cannot report content.")
            return
        }

        // Define data for the report
        val reportData = hashMapOf(
            "reporterID" to currentUser.id,
            "reportedUserID" to chatUser.id,
            "timestamp" to Timestamp.now(),
            "resolved" to false
        )

        // Add the report to the objectionable content collection
        db.collection("objectionable_content").add(reportData)
            .addOnSuccessListener {
                Log.d("ChatManager", "Objectionable content reported successfully.")
            }
            .addOnFailureListener { e ->
                Log.w("ChatManager", "Failed to report objectionable content: ${e.localizedMessage}", e)
            }
    }


    fun blockChatUser() {
        val currentUser = userManager.currentUser.value ?: return
        val chatUser = userManager.chatUser.value ?: return

        // Ensure we have valid user IDs to work with
        if (currentUser.id.isBlank() || chatUser.id.isBlank()) {
            Log.d("ChatManager", "CurrentUserID or ReceiverID is empty. Cannot block user.")
            return
        }

        // Determine who is the student and who is the coach
        val studentID = if (currentUser.student) currentUser.id else chatUser.id

        // Update the student's 'assignedCoach' to 0 in Firestore, indicating they are blocked
        val studentDocument = db.collection("users").document(studentID)
        studentDocument.update("assignedCoach", 0)
            .addOnSuccessListener {
                Log.d("ChatManager", "User successfully blocked.")
            }
            .addOnFailureListener { e ->
                Log.w("ChatManager", "Error blocking user: ${e.localizedMessage}", e)
            }
    }
}