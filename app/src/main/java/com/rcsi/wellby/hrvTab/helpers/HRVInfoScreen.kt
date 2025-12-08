package com.rcsi.wellby.hrvTab.helpers
// informational screen with dropdown questions and answers about HRV
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rcsi.wellby.R
import com.rcsi.wellby.signinSystem.AuthManager

@Composable
fun HRVInfoScreen(userManager: AuthManager) {

    var selectedQuestion by remember { mutableStateOf(HRVQuestion.WhatIsHRV)}

    LaunchedEffect(key1 = Unit) {
        userManager.viewDidAppear("HRV info")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue.copy(alpha = 0.3F))
                .padding(16.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            DropdownQuestionSelector(
                selectedQuestion = selectedQuestion,
                onQuestionSelected = { selectedQuestion = it },
                modifier = Modifier
                    .weight(4f)
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(id = R.drawable.figure_wave),
                contentDescription = "Human figure",
                modifier = Modifier
                    .size(65.dp)
                    .weight(1f)
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                HRVAnswerProvider.AnswerFor(question = selectedQuestion)
            }
        }
    }

}


@Composable
fun DropdownQuestionSelector(
    selectedQuestion: HRVQuestion,
    onQuestionSelected: (HRVQuestion) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(contentColor = Color.White)
        ) {
            Text(text = selectedQuestion.text, modifier = Modifier.padding(16.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Drop-down icon")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HRVQuestion.entries.forEach { question ->
                DropdownMenuItem(
                    text = {
                        Text(question.text)
                    },
                    onClick = {
                        onQuestionSelected(question)
                        expanded = false
                    }
                )
            }
        }
    }
}


enum class HRVQuestion(val text: String) {
    WhatIsHRV("What is HRV?"),
    LinkedToStress("How is HRV linked to stress?"),
    InfluenceOnHRV("What can influence my HRV?"),
    MeasurementMeaning("How is my HRV measured?"),
    BreathworkInfluence("How can breathwork influence HRV?"),
    DifferentBreathingExercises("What's the difference between breathing exercises?")
}

object HRVAnswerProvider {
    @Composable
    fun AnswerFor(question: HRVQuestion) {
        when (question) {
            HRVQuestion.WhatIsHRV -> WhatIsHRVAnswer()
            HRVQuestion.LinkedToStress -> LinkedToStressAnswer()
            HRVQuestion.InfluenceOnHRV -> InfluenceOnHRVAnswer()
            HRVQuestion.MeasurementMeaning -> MeasurementMeaningAnswer()
            HRVQuestion.BreathworkInfluence -> BreathworkInfluenceAnswer()
            HRVQuestion.DifferentBreathingExercises -> DifferentBreathingExercisesAnswer()
        }
    }

    @Composable
    fun WhatIsHRVAnswer() {
        Column {
            Text("Generally, HRV is a good indicator of our ability to adapt to changes throughout the day, but what exactly is it?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Heart rate variability (HRV) is a measure of the changing time between your heartbeats. For example, if your heart is beating at 60 beats per minute, it won’t beat exactly on the second every time.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Put simply, a healthy heart is NOT a metronome. Instead, think of your heart rate like waves on the beach. They can break more quickly, or more spaced apart all depending on what’s going on in the ocean.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Just like waves, our heart rate changes in response to daily events ranging from playing sports, taking a test, or taking a relaxing bath in the evening.", fontSize = 16.sp)
        }
    }

    @Composable
    fun LinkedToStressAnswer() {
        Column {
            Text("Variations in your heart rate (HRV) are due to the dynamic nature of our nervous systems. Within part of the nervous system, there is the sympathetic nervous system (SNS) and the parasympathetic nervous system (PNS).", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Activation of the SNS evokes the “fight or flight” response, whereas activation of the PNS promotes the “rest and digest” response. The PNS can be thought of as a brake pedal to your heart rate, while the SNS as an accelerator pedal.", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("There is a constant tug of war between the SNS and PNS which results in a balanced heart rate appropriate for the environment or situation you’re currently in. Our bodies need to be able to rapidly alternate between these two states in order to respond to environmental and psychological challenges.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("HRV is a measure of flexibility and adaptability of our heart and nervous system to adapt to environmental and internal physiological challenges throughout our day.", fontSize = 16.sp)
        }
    }

    @Composable
    fun InfluenceOnHRVAnswer() {
        Column {
            Text("HRV is influenced by a wide range of factors including your genetics, school environment, personality, fitness, stress levels, and caffeine intake.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("The average range for SDNN and RMSDD for adolescent females ranges 100.60 to 132.00 and 77.00 to 119.10 respectively. For males, 82.20 to 117.75 and 77.55 to 125.05 respectively.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("As you can tell, even the average in adolescents is highly variable so there is really no point in comparing HRVs with your friends. Different raw HRV numbers can mean different things for different people and these numbers change throughout the day and between days.", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Instead, it is more helpful to look at how your own HRV is changing over time and compare it to different lifestyle habits and events in your environment.", fontSize = 16.sp)
        }
    }

    @Composable
    fun MeasurementMeaningAnswer() {
        Column {
            Text("HRV can be measured outside of a clinical context via photoplethysmography (PPG). This means a small light source can detect blood volume changes at the surface of your skin and hence determine your HRV.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("PPG can be measured using green, red, or infrared light. Your wearable uses infrared light which penetrates deeper into your wrist and is less affected by different skin tones. Infrared is not in the visible light spectrum, so you won't be able to see it when it's on.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Calming Response (RMSSD - Root mean squared of standard differences)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("RMSSD is a mathematical calculation of the average difference between heartbeats that are next to each other. This indicates the activity of your body’s calming response. This is called vagal tone and it represents your systems ability to counterbalance stress activity and “rest and digest.”", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Return to Balance (SDNN - Standard deviation of peak-to-peak intervals)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("SDNN is a similar calculation of the average difference of all the heartbeats, but instead of only looking at how this changes for adjacent beats, it looks at the overall spread of beats within a recording time. This indicates the overall balance between your “fight or flight” and “rest and digest” response. Both healthy stress and relaxation are necessary for our bodies to respond to our environments, these just need to be in equilibrium. SDNN is a measure of this balance.", fontSize = 16.sp)
        }
    }

    @Composable
    fun BreathworkInfluenceAnswer() {
        Column {
            Text("Breathwork has a significant impact on HRV by directly influencing the parasympathetic nervous system (PNS), which helps to promote relaxation and recovery.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Practices such as slow, deep breathing increase HRV by activating the PNS, leading to a decrease in heart rate and an increase in heart rate variability. This is often seen as the heart's ability to shift gears efficiently, reflecting better stress management and resilience.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Breathwork can be used as a powerful tool for calming the mind and body, particularly before stressful events or as a regular practice to enhance overall well-being.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Regular engagement in breathwork exercises can lead to long-term improvements in HRV, indicating a robust and responsive nervous system.", fontSize = 16.sp)
        }
    }


    @Composable
    fun DifferentBreathingExercisesAnswer() {
        Column {
            Text("Different breathing exercises can have varying effects on HRV, as they engage the nervous system in different ways.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("For example, rhythmic breathing such as the 4-7-8 technique, where you inhale for 4 seconds, hold for 7 seconds, and exhale for 8 seconds, can significantly increase HRV by promoting relaxation.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Conversely, quick breathing techniques, often used in stress response training, might decrease HRV temporarily but are used to train the body's ability to handle stress more effectively.", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Choosing the right breathing exercise depends on your goals, whether you aim to calm down quickly, enhance focus, or improve your overall heart health and stress resilience.", fontSize = 16.sp)
        }
    }

}


