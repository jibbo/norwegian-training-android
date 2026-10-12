package com.github.jibbo.norwegiantraining.sharetraining

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.removeEmoji
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@AndroidEntryPoint
class ShareTrainingActivity : BaseActivity() {
    private val viewModel: ShareTrainingViewModel by viewModels()
    private var pendingCameraUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onBackgroundSelected(it.toString()) }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { viewModel.onBackgroundSelected(it.toString()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(
            workoutName = intent.getStringExtra(EXTRA_WORKOUT_NAME),
            completedAtMillis = intent.getLongExtra(EXTRA_COMPLETED_AT_MILLIS, System.currentTimeMillis()),
            totalPhases = intent.getIntExtra(EXTRA_TOTAL_PHASES, 0),
            workoutDurationMinutes = intent.getIntExtra(EXTRA_WORKOUT_DURATION_MINUTES, 0),
        )
        setContent {
            val state by viewModel.uiStates.collectAsState()
            NorwegianTrainingTheme(darkTheme = true) {
                Scaffold( modifier = Modifier
                    .fillMaxSize()) { _ ->
                    ShareTrainingView(
                        state = state,
                        onPickGallery = { pickImage.launch("image/*") },
                        onTakePhoto = { launchCamera() },
                        onShare = { share(state) },
                        onDone = { finish() }
                    )
                }

            }
        }
    }

    private fun launchCamera() {
        val uri = createCacheUri("camera_${UUID.randomUUID()}.jpg")
        pendingCameraUri = uri
        takePicture.launch(uri)
    }

    private fun share(state: ShareTrainingUiState) {
        lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                val bitmap = ShareTrainingBitmap.create(
                    context = this@ShareTrainingActivity,
                    contentResolver = contentResolver,
                    state = state,
                    text = ShareTrainingText(
                        brand = getString(R.string.app_name),
                        fallbackWorkoutName = getString(R.string.workout_complete),
                        completedPhases = resources.getQuantityString(
                            R.plurals.training_phases_completed,
                            state.totalPhases,
                            state.totalPhases,
                        ),
                        duration = state.workoutDurationMinutes.takeIf { it > 0 }?.let {
                            getString(R.string.workout_time, it.toString()).removeEmoji().replaceFirst(" ", "")
                        }.orEmpty(),
                    ),
                )
                val shareUri = createCacheUri("training_share.png")
                contentResolver.openOutputStream(shareUri)?.use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                shareUri
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching {
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_training_title)))
            }.onFailure {
                Toast.makeText(this@ShareTrainingActivity, R.string.no_share_app_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createCacheUri(fileName: String): Uri {
        val dir = File(cacheDir, "shared_training").apply { mkdirs() }
        val file = File(dir, fileName)
        if (!file.exists()) FileOutputStream(file).close()
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }

    companion object {
        const val EXTRA_WORKOUT_NAME = "extra_workout_name"
        const val EXTRA_COMPLETED_AT_MILLIS = "extra_completed_at_millis"
        const val EXTRA_TOTAL_PHASES = "extra_total_phases"
        const val EXTRA_WORKOUT_DURATION_MINUTES = "extra_workout_duration_minutes"
    }
}
