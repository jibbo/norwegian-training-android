package com.github.jibbo.norwegiantraining.sharetraining

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.ui.theme.Typography

@Composable
fun ShareTrainingView(
    state: ShareTrainingUiState,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onShare: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(R.string.share_training_title.localizable(), style = Typography.headlineLarge)
        Text(
            R.string.share_training_subtitle.localizable(),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        ShareTrainingPreview(state, Modifier.weight(1f).fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onPickGallery, modifier = Modifier.weight(1f)) {
                Text(R.string.gallery.localizable())
            }
            OutlinedButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
                Text(R.string.camera.localizable())
            }
        }
        Button(onClick = onShare, enabled = state.canShare, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text(R.string.share.localizable())
        }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(R.string.done.localizable())
        }
    }
}

@Composable
private fun ShareTrainingPreview(state: ShareTrainingUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.DarkGray)
    ) {
        val context = LocalContext.current
        val bitmap by remember(state.backgroundUri) {
            mutableStateOf(state.backgroundUri?.let { uriString ->
                runCatching {
                    val uri = Uri.parse(uriString)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
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
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))))
        )
        Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
            Text(R.string.app_name.localizable(), color = Color.White, style = Typography.titleMedium)
            Text(
                state.workoutName.ifBlank { R.string.workout_complete.localizable() },
                color = Color.White,
                style = Typography.headlineLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(state.formattedDate, color = Color.White, style = Typography.titleMedium)
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
