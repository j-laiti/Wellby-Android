package com.rcsi.wellby.toolkitTab.quote
// handler for managing the json of quotes to display

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class JSONHandler(private val context: Context) {
    fun getTodaysQuote(): Quote? {
        Log.d("JSONHandler", "Attempting to open quotes.json")
        val jsonFileString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
        Log.d("JSONHandler", "Successfully opened quotes.json")
        val gson = Gson()
        val listQuoteType = object : TypeToken<List<Quote>>() {}.type
        var quotes: List<Quote> = gson.fromJson(jsonFileString, listQuoteType)

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val targetID = (dayOfYear - 1) % 100

        return quotes.find { it.id == targetID + 1 }
    }
}