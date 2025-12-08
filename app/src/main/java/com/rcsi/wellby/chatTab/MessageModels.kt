package com.rcsi.wellby.chatTab
// Message structures including conversion of the time to display associated with the recent message

import com.google.firebase.Timestamp
import android.text.format.DateUtils
import java.util.concurrent.TimeUnit

data class Message(
    val id: String = "",
    val currentUserID: String = "",
    val receiverID: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class RecentMessage(
    val currentID: String = "",
    val chatUserID: String = "",
    val name: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var viewed: Boolean = true
)

fun getTimeAgo(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val then = timestamp.toDate().time
    val diff = now - then

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> DateUtils.getRelativeTimeSpanString(then, now, DateUtils.DAY_IN_MILLIS).toString()
    }
}
