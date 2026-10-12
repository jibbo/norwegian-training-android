package com.github.jibbo.norwegiantraining.sharetraining

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography

@Composable
fun ShareTrainingView(
    state: ShareTrainingUiState,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onShare: () -> Unit,
    onDone: () -> Unit,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .imePadding()
                ) {
                    ShareTrainingHeader(onDone = onDone)
                    Text(
                        R.string.share_training_subtitle.localizable(),
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    ShareTrainingActions(
                        canShare = state.canShare,
                        onPickGallery = onPickGallery,
                        onShare = onShare,
                        onTakePhoto = onTakePhoto,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    ShareTrainingPreview(
                        state = state,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                ShareTrainingHeader(onDone = onDone)
                Text(
                    R.string.share_training_subtitle.localizable(),
                    style = Typography.bodyLarge,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ShareTrainingPreview(
                    state, Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                ShareTrainingActions(
                    canShare = state.canShare,
                    onPickGallery = onPickGallery,
                    onShare = onShare,
                    onTakePhoto = onTakePhoto,
                )
            }
        }
    }
}

@Composable
private fun ShareTrainingHeader(onDone: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            R.string.share_training_title.localizable(),
            style = Typography.headlineLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDone) {
            Icon(
                painter = painterResource(R.drawable.outline_close_24),
                contentDescription = R.string.close.localizable()
            )
        }
    }
}

@Composable
private fun ShareTrainingActions(
    canShare: Boolean,
    onPickGallery: () -> Unit,
    onShare: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        OutlinedIconButton(
            onClick = onPickGallery,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_photo_library_24),
                contentDescription = R.string.gallery.localizable(),
            )
        }
        OutlinedIconButton(
            onClick = onShare,
            enabled = canShare,
            colors = IconButtonDefaults.iconButtonColors(),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_share_24),
                contentDescription = R.string.share.localizable(),
            )
        }
        OutlinedIconButton(
            onClick = onTakePhoto,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_camera_24),
                contentDescription = R.string.camera.localizable(),
            )
        }
    }
}

@Composable
private fun ShareTrainingPreview(state: ShareTrainingUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 14f)
            .clip(RoundedCornerShape(28.dp))
            .background(DarkPrimary)
    ) {
        val context = LocalContext.current
        val bitmap by remember(state.backgroundUri) {
            mutableStateOf(state.backgroundUri?.let { uriString ->
                runCatching {
                    val uri = Uri.parse(uriString)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        context.contentResolver.openInputStream(uri)
                            .use { BitmapFactory.decodeStream(it) }
                    }
                }.getOrNull()
            })
        }
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = null,
            )
            Text(
                R.string.app_name.localizable(),
                color = Color.White,
                style = Typography.titleMedium
            )
            Text(
                state.workoutName.ifBlank { R.string.workout_complete.localizable() },
                color = Color.White,
                style = Typography.headlineLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(state.formattedDate, color = Color.White, style = Typography.titleMedium)
            if (state.workoutDurationMinutes > 0) {
                Text(
                    R.string.workout_time.localizable(state.workoutDurationMinutes.toString()),
                    color = Color.White,
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.totalPhases > 0) {
                Text(
                    pluralStringResource(
                        R.plurals.training_phases_completed,
                        state.totalPhases,
                        state.totalPhases,
                    ),
                    color = Color.White,
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ShareTrainingViewPreview() {
    NorwegianTrainingTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { _ ->
            ShareTrainingView(
                state = ShareTrainingUiState(
                    workoutName = "Beginner 1",
                    completedAtMillis = 1_700_000_000_000L,
                    totalPhases = 4,
                    workoutDurationMinutes = 25,
                    backgroundUri = null,
                ),
                onPickGallery = {},
                onTakePhoto = {},
                onShare = {},
                onDone = {},
            )
        }
    }
}
