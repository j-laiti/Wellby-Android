package com.rcsi.wellby.signinSystem
// view model for managing the users information during signin, ensuring authorisation
// and accessing the user information throughout the app to fetch their  data from Firebase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FieldValue

class AuthManager: ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // published user session info
    val userSession = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = MutableStateFlow<User?>(null)

    // chat variable
    var chatUser = MutableStateFlow<User?>(null)
    val assignedCoach = MutableStateFlow<User?>(null)
    val userList = MutableStateFlow<List<User>>(emptyList())

    init {
        // Check if the user is already logged in when the app starts and update userSession accordingly
        userSession.value = Firebase.auth.currentUser
        userSession.value?.let { user ->
            fetchUser(user.email ?: "")
        }
    }

    // Status management
    private val _codeStatus = MutableStateFlow<String?>(null)
    val codeStatus = _codeStatus.asStateFlow()
    val createAccountStatus = MutableStateFlow<String?>(null)
    val signInStatus = MutableStateFlow<String?>(null)
    val resetEmailStatus = MutableStateFlow<String?>(null)

    // user info saved during study code check
    var isStudent: Boolean = false
    var school = ""

    fun checkStudyCode(code: String, completion: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val codeFields = listOf("gorey", "wolstans", "coach", "rush", "tallaght")
                var codeExists = false

                for (codeField in codeFields) {
                    val dataSnapshot = db.collection("studyCodes")
                        .whereEqualTo(codeField, code)
                        .get()
                        .await()

                    if(dataSnapshot.documents.isNotEmpty()) {
                        codeExists = true
                        break
                    }
                }

                val localCodeExists = codeExists

                withContext(Dispatchers.Main) {
                    if (localCodeExists) {
                        println("code exists :D")
                        when {
                            code.endsWith("c") -> {
                                isStudent = false
                                school = ""
                            }

                            code.endsWith("g") -> {
                                isStudent = true
                                school = "Gorey"
                            }

                            code.endsWith("w") -> {
                                isStudent = true
                                school = "St. Wolstans"
                            }

                            code.endsWith("r") -> {
                                isStudent = true
                                school = "Youthreach Rush"
                            }

                            code.endsWith("t") -> {
                                isStudent = true
                                school = "Youthreach Tallaght"
                            }

                            else -> {
                                _codeStatus.value = "Error or codes have changed"
                            }
                        }
                    } else {
                        _codeStatus.value = "Couldn't find the entered code."
                    }

                    completion(localCodeExists)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _codeStatus.value = "Error accessing study codes: ${e.localizedMessage}"
                    completion(false)
                }
            }
        }
    }

    fun createAccount(email: String, password: String, firstName: String, surname: String,
                      username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Calculate assignedCoach based on the first letter of firstName
                val assignedCoach = if (isStudent) {
                    when (firstName.first().lowercase()) {
                        in "a".."g" -> 1
                        in "h".."m" -> 2
                        in "n".."z" -> 3
                        else -> 3
                    }
                } else 0

                // Construct user information
                val newUser = User(
                    id = result.user?.uid ?: "",
                    student = isStudent,
                    school = school,
                    firstName = firstName,
                    surname = surname,
                    username = username,
                    email = email,
                    assignedCoach = assignedCoach
                )

                // Save user information to Firestore
                db.collection("users").document(newUser.id).set(newUser).await()

                result.user?.uid?.let { userId ->
                    FirebaseMessaging.getInstance().subscribeToTopic(userId).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FCM", "Subscription to topic successful")
                        } else {
                            Log.w("FCM", "Subscription to topic failed", task.exception)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    userSession.value = result.user
                    createAccountStatus.value = null
                    currentUser.value = newUser
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    signInStatus.value =
                        "Unable to create an account. Please check the entered information."
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()

                result.user?.uid?.let { userId ->
                    FirebaseMessaging.getInstance().subscribeToTopic(userId).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FCM", "Subscription to topic successful")
                        } else {
                            Log.w("FCM", "Subscription to topic failed", task.exception)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    userSession.value = result.user
                    signInStatus.value = null
                    println("success with signin!! :DDDD")
                    fetchUser(email)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    signInStatus.value =
                        "Unable to sign in. Please check your internet connection or the entered information."
                    println("DEBUG: failed to login with error ${e.localizedMessage}")
                }
            }
        }
    }

    fun fetchUser(email: String) {
        if (email.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                Log.d("AuthManager", "Attempting to fetch user data for email: $email")
                try {
                    val documents = db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .await()  // Use .await() with coroutines to wait for the result

                    // Since we're already in a coroutine, we can check the result right away:
                    if (documents != null && !documents.isEmpty) {
                        val user = documents.documents.firstOrNull()?.toObject(User::class.java)
                        withContext(Dispatchers.Main) {
                            currentUser.value = user
                            Log.d("AuthManager", "User data fetched successfully: $user")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d("AuthManager", "User not found in database.")
                        }
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("AuthManager", "Error fetching user data: ${exception.localizedMessage}")
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentUser.value?.id?.let { userId ->
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(userId).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FCM", "Unsubscription from topic successful")
                        }
                    }
                }

                auth.signOut()
                withContext(Dispatchers.Main) {
                    userSession.value = null
                    currentUser.value = null
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    signInStatus.value =
                        "Unable to logout."
                    println("DEBUG: failed to login with error ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteUserAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = auth.currentUser
            if (user != null) {
                try {
                    // Delete user data from Firestore
                    db.collection("users").document(user.uid).delete().await()

                    Log.d("AuthManager", "User document successfully removed")

                    // Delete user authentication record
                    user.delete().await()

                    withContext(Dispatchers.Main) {
                        // Update UI-related variables
                        userSession.value = null
                        currentUser.value = null
                        Log.d("AuthManager", "User authentication record successfully deleted.")
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("AuthManager", "Error removing user data or authentication record: ${exception.localizedMessage}")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("AuthManager", "No user is currently logged in.")
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email)
                withContext(Dispatchers.Main) {
                    resetEmailStatus.value = "Email sent to reset password"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resetEmailStatus.value = "Error sending reset link"
                }
            }
        }
    }

    fun fetchUserById(userId: String, completion: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val docSnapshot = db.collection("users").document(userId).get().await()
                val user = docSnapshot.toObject(User::class.java)
                withContext(Dispatchers.Main) {
                    completion(user)
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Error fetching user by ID: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    completion(null)
                }
            }
        }
    }


    fun fetchAssignedCoach() {
        val currentUser = currentUser.value ?: return

        viewModelScope.launch {
            db.collection("users")
                .whereEqualTo("coachNumber", currentUser.assignedCoach)
                .limit(1) // Assuming each student has only one coach
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Assuming 'User' is your data model for users
                        val coach = document.toObject(User::class.java)
                        assignedCoach.value = coach
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthManager", "Error fetching assigned coach: $exception")
                }
        }
    }

    fun updateCoachStatus(newStatus: String) {
        val currentUserValue = currentUser.value ?: return // Return early if currentUser is null

        // Update Firestore first
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("users").document(currentUserValue.id)
                    .update("status", newStatus)
                    .await()

                val updatedUser = currentUserValue.copy(status = newStatus)

                withContext(Dispatchers.Main) {
                    // Update the local state to reflect the new status
                    currentUser.value = updatedUser
                    Log.d("AuthManager", "Status updated successfully")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle any errors, such as showing a toast to the user
                    Log.e("AuthManager", "Error updating status: ${e.localizedMessage}")
                }
            }
        }
    }

    fun fetchAssignedStudents() {
        currentUser.value?.let { currentUser ->
            currentUser.coachNumber.let { coachNumber ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val result = db.collection("users")
                            .whereEqualTo("assignedCoach", coachNumber)
                            .get()
                            .await()

                        val fetchedUsers = result.documents.mapNotNull { snapshot ->
                            snapshot.toObject(User::class.java)
                        }

                        withContext(Dispatchers.Main) {
                            userList.value = fetchedUsers
                        }
                    } catch (e: Exception) {
                        Log.e("AuthManager", "Error fetching assigned students: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    fun applyOptInChange(optIn: Boolean) {
        val currentUserValue = currentUser.value ?: return // Ensure currentUser is not null
        val userId = currentUserValue.id

        // Determine the assigned coach number based on the first name
        val coachAssignment = if (optIn) {
            when (currentUserValue.firstName.first().lowercase()) {
                in "a".."m" -> 1
                in "n".."z" -> 2
                else -> 1 // Default to 1 if the name is unrecognized
            }
        } else 0

        // Update Firestore
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("users").document(userId)
                    .update(
                        mapOf(
                            "isCoachingOptedIn" to optIn,
                            "assignedCoach" to coachAssignment
                        )
                    )
                    .await()

                // Update local user model
                val updatedUser = currentUserValue.copy(
                    isCoachingOptedIn = optIn,
                    assignedCoach = coachAssignment
                )
                withContext(Dispatchers.Main) {
                    currentUser.value = updatedUser
                    Log.d("AuthManager", "Opt-in status and coach assignment updated successfully")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AuthManager", "Error updating opt-in status: ${e.localizedMessage}")
                }
            }
        }
    }

    // Method to track screen views
    fun viewDidAppear(screen: String) {
        val currentUserID = currentUser.value?.id ?: return

        if (currentUserID.isEmpty()) {
            Log.e("AuthManager", "viewDidAppear Error: Current user ID is null or empty.")
            return
        }

        val document = db.collection("users").document(currentUserID)
            .collection("engagement").document()

        val screenView = mapOf(
            "screen_viewed" to screen,
            "timestamp" to FieldValue.serverTimestamp()
        )

        document.set(screenView)
            .addOnSuccessListener {
                Log.d("AuthManager", "Screen view logged for screen: $screen")
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Firestore write error: ${e.localizedMessage}")
            }
    }

    // Method to track feature clicks
    fun clickedOn(feature: String) {
        val currentUserID = currentUser.value?.id ?: return

        if (currentUserID.isEmpty()) {
            Log.e("AuthManager", "clickedOn Error: Current user ID is null or empty.")
            return
        }

        val document = db.collection("users").document(currentUserID)
            .collection("engagement").document()

        val featureClick = mapOf(
            "feature_clicked" to feature,
            "timestamp" to FieldValue.serverTimestamp()
        )

        document.set(featureClick)
            .addOnSuccessListener {
                Log.d("AuthManager", "Feature click logged for feature: $feature")
            }
            .addOnFailureListener { e ->
                Log.e("AuthManager", "Firestore write error: ${e.localizedMessage}")
            }
    }


}