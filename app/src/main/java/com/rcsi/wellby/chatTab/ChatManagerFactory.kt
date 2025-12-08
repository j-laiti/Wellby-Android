package com.rcsi.wellby.chatTab
// factory to handle the initialisation of the chat manager view model which takes an AuthManager
// as an input to keep track of the users information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rcsi.wellby.signinSystem.AuthManager
import java.lang.IllegalArgumentException

class ChatManagerFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatManager(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
