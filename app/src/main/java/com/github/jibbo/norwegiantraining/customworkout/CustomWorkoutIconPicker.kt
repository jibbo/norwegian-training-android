package com.github.jibbo.norwegiantraining.customworkout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jibbo.norwegiantraining.R

const val DEFAULT_CUSTOM_WORKOUT_ICON = "👟"

/** Small state boundary around icon selection and clearing. */
data class CustomWorkoutIconPickerState(
    val selectedIcon: String? = DEFAULT_CUSTOM_WORKOUT_ICON,
) {
    fun select(icon: String?): CustomWorkoutIconPickerState =
        copy(selectedIcon = icon?.takeIf { it.isNotBlank() })

    fun clear(): CustomWorkoutIconPickerState = copy(selectedIcon = null)
}

@Composable
fun CustomWorkoutIconPicker(
    selectedIcon: String?,
    onIconSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedIcon.orEmpty(),
            onValueChange = { onIconSelected(it.takeIf(String::isNotBlank)) },
            label = { Text(stringResource(R.string.custom_workout_icon)) },
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onIconSelected(null) },
            enabled = selectedIcon != null,
        ) {
            Text(stringResource(R.string.custom_workout_clear_icon))
        }
    }
}
