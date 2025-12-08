package com.rcsi.wellby.toolkitTab.quote
// block which displays a new quote each day based on the json of 100 quotes

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DailyQuoteBlock() {
    // Getting the application context
    val context = LocalContext.current
    val appContext = context.applicationContext as Application
    // Creating the ViewModel using the factory
    val viewModel: QuoteViewModel = viewModel(factory = QuoteViewModelFactory(appContext))
    val quote = viewModel.todaysQuote.collectAsState().value

    Surface(
        elevation = 10.dp,
        modifier = Modifier.padding(horizontal = 30.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSecondary
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            quote?.let {
                Text(
                    text = "\"${it.quote}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- ${it.author}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                    )
            } ?: Text(text = "No quote available")
        }
    }
}