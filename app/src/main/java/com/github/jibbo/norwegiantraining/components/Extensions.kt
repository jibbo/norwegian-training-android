package com.github.jibbo.norwegiantraining.components

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import java.util.Locale

@Composable
fun Int.localizable(vararg args: Any): String = stringResource(this, *args)
