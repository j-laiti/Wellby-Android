package com.rcsi.wellby.toolkitTab.quote
// factory to handle the initialisation of the quote view model factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class QuoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuoteViewModel(JSONHandler(application)) as T
        }
        throw  IllegalArgumentException("Unknown ViewModel Class")
    }
}