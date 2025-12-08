package com.rcsi.wellby.toolkitTab.quote
// view model to handle the daily quote call

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// The ViewModel extensions gives this class view model properties
class QuoteViewModel(
    private val jsonHandler: JSONHandler
) : ViewModel() {
    // mutable val in the background
    private val _todaysQuote = MutableStateFlow<Quote?>(null)
    val todaysQuote = _todaysQuote.asStateFlow()

    init {
        // fetches todays quote upon initialisation
        _todaysQuote.value = jsonHandler.getTodaysQuote()
    }
}