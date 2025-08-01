package com.github.jibbo.norwegiantraining

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme

@Composable
fun MainView(mainViewModel: MainViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Welcome!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = """ We are going to do a classic Norwegian training:\n
                1.10 minutes of warm-up\n
                2. 4 Minutes of high intensity cardio (85-95%) max heart rate\n
                3. 4 Minutes of low intensity cardio (55-65%) max heart rate\n
                We will repeat step 2 and 3  for 4 times\n.
                Are you ready?
            """.trimMargin(),
        )
        Button(onClick = { mainViewModel.scheduleNextAlarm() }) {
            Text(text = "Start/Stop")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        MainView(MainViewModel())
    }
}
