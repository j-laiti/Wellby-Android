package com.rcsi.wellby.chatTab.views
// Informational screen about health coaching for students

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoachingGuideView() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(all = 16.dp))
    ) {
        Text(
            text = "What is Health Coaching?",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Health coaching supports your lifestyle-related goals. Trained professionals, known as health coaches, provide support, guidance, and motivation toward improved health and wellbeing. Whether aiming to enhance your diet, increase exercise, or improve sleep habits, health coaches are there to support your journey.",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "What Health Coaching is Not",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Health coaching is distinct from therapy. Unlike therapy, which addresses emotional or psychological issues, health coaching focuses on setting and achieving lifestyle goals.",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "How Can a Health Coach Support You as a Student?",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "• Navigate student life challenges.\n• Support lifestyle changes for stress management, better sleep, improved time management, and digital wellbeing.\n• Guide you to resources like Aware and Spunout, if needed.",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "How Are You Expected to Engage with Health Coaches?",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Engagement with health coaches should be respectful, open, and in line with your school's code of behavior. These professionals volunteer their time to help you, aiming to build a supportive relationship focused on your well-being. Misconduct or disrespect can result in the loss of access to this support. The aim is to build a positive, supportive relationship focused on your wellbeing goals."
        )
    }
}