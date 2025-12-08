package com.rcsi.wellby.hrvTab
// View which displays the summary of HRV metrics, currently in block for each measure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rcsi.wellby.hrvTab.models.HRVSessionData
import com.rcsi.wellby.hrvTab.viewModels.HRVDataManager
import com.rcsi.wellby.signinSystem.AuthManager
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun HRVSummaryScreen(hrvDataManager: HRVDataManager, userManager: AuthManager) {
    val hrvDataList by hrvDataManager.hrvDataList.collectAsState()
    val userID = userManager.currentUser.collectAsState().value?.id ?: ""

    LaunchedEffect(userID) {
        hrvDataManager.fetchHRVData(userID, limit = 5)
        userManager.viewDidAppear("HRV Summary")
    }

    if (hrvDataList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Fetching HRV data...")
            CircularProgressIndicator()
        }
    } else {
        LazyColumn {
            items(hrvDataList) { hrvData ->
                HRVDataItem(hrvData)
            }
        }
    }
}

@Composable
fun HRVDataItem(hrvData: HRVSessionData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Date: ${formatDate(hrvData.timestamp)}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            Row {
                MetricView("Calming Response", hrvData.formattedRmssd)
                MetricView("Return to Balance", hrvData.formattedSdnn)
            }
            Row {
                MetricView("Heart Rate", hrvData.formattedAverageHR)
                MetricView("Signal Quality", hrvData.signalQualityLabel)
            }
        }
    }
}

@Composable
fun MetricView(title: String, value: String) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.headlineMedium)
    }
}

fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    return timestamp?.toDate()?.let { date ->
        SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault()).format(date)
    } ?: "Unknown"
}

