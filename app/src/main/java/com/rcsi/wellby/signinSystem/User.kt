package com.rcsi.wellby.signinSystem

import com.google.firebase.firestore.PropertyName

// user model with the information about the user that is saved to firebase
data class User (
    val id: String = "",
    val student: Boolean = false,
    val school: String = "none",
    val firstName: String = "",
    val surname: String = "",
    val username: String = "",
    val email: String = "",
    val status: String = "",
    var assignedCoach: Int = 0,
    @get:PropertyName("isCoachingOptedIn") @set:PropertyName("isCoachingOptedIn")
    var isCoachingOptedIn: Boolean? = false
) {
    val coachNumber: Int
        get() = if (!student) {
            when (firstName.first().lowercase()) {
                in "a".."g" -> 1
                in "h".."m" -> 2
                in "n".."z" -> 3
                else -> 3
            }
        } else 0
}