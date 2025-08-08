package com.github.jibbo.norwegiantraining.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.random.Random


@Composable
internal fun Logs(
    innerPadding: PaddingValues,
    uiState: UiState.Loaded
) {
    LazyColumn(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = R.string.title_activity_logs.localizable(),
                style = Typography.displayLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        items(12) { month ->
            Month(month, uiState)
        }
    }
}

@Composable
private fun Month(
    month: Int,
    uiState: UiState.Loaded
) {
    val dateFormat = SimpleDateFormat("MMMM")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    Text(
        text = dateFormat.format(calendar.time).capitalizeFirstLetter(),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    FlowRow(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (i in 0..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Day(calendar, i, uiState, month)
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    )
}

@Composable
private fun Day(
    calendar: Calendar,
    index: Int,
    uiState: UiState.Loaded,
    month: Int
) {
    calendar.set(
        Calendar.DAY_OF_MONTH,
        index + 1
    )
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val boxDate = calendar.time

    val item =
        uiState.logs[month]?.find { it.date.isSameDay(boxDate) }

    Box(
        modifier = Modifier
            .size(32.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(item?.getStatus()?.getColor() ?: Color.DarkGray)
    ) {
//        Text(index.toString())
    }
}

private fun Date.isSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = this
    val cal2 = Calendar.getInstance()
    cal2.time = other
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}


@Composable
@Preview
fun Preview() {
    val lol = UiState.Loaded(
        mapOf(1 to createSessions(10))
    )
    NorwegianTrainingTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Logs(innerPadding, lol)
        }
    }
}

private fun createSessions(sessionCount: Int): List<Session> {
    return buildList(capacity = sessionCount) {
        repeat(sessionCount) { index ->
            add(
                Session(
                    skipCount = Random.nextInt(0, 101),
                    date = Date()
                )
            )
        }
    }
}

private fun String.capitalizeFirstLetter() =
    if (this.isNotEmpty()) this[0].uppercase() + this.substring(1) else this
