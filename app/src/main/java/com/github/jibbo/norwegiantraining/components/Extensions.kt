package com.github.jibbo.norwegiantraining.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun Int.localizable() = stringResource(this)
