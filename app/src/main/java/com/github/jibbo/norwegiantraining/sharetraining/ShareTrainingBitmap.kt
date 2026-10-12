package com.github.jibbo.norwegiantraining.sharetraining

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.github.jibbo.norwegiantraining.R
import java.io.InputStream

data class ShareTrainingText(
    val brand: String,
    val fallbackWorkoutName: String,
    val completedPhases: String,
    val duration: String,
)

object ShareTrainingBitmap {
    const val WIDTH = 1080
    const val HEIGHT = 1920

    fun create(
        context: Context,
        contentResolver: ContentResolver,
        state: ShareTrainingUiState,
        text: ShareTrainingText,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBackground(canvas, contentResolver, state.backgroundUri)
        drawOverlay(canvas, state, text, context)
        return bitmap
    }

    private fun drawBackground(canvas: Canvas, contentResolver: ContentResolver, uriString: String?) {
        val background = uriString?.let { decodeBitmap(contentResolver, Uri.parse(it)) }
        if (background == null) {
            canvas.drawColor(Color.rgb(20, 20, 20))
            return
        }
        val src = centerCropSource(background.width, background.height, WIDTH, HEIGHT)
        canvas.drawBitmap(background, src, Rect(0, 0, WIDTH, HEIGHT), Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun decodeBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val stream: InputStream? = contentResolver.openInputStream(uri)
            stream.use { BitmapFactory.decodeStream(it) }
        }
    }.getOrNull()

    private fun centerCropSource(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Rect {
        val sourceRatio = width.toFloat() / height.toFloat()
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
        return if (sourceRatio > targetRatio) {
            val newWidth = (height * targetRatio).toInt()
            val left = (width - newWidth) / 2
            Rect(left, 0, left + newWidth, height)
        } else {
            val newHeight = (width / targetRatio).toInt()
            val top = (height - newHeight) / 2
            Rect(0, top, width, top + newHeight)
        }
    }

    private fun drawOverlay(
        canvas: Canvas,
        state: ShareTrainingUiState,
        text: ShareTrainingText,
        context: Context,
    ) {
        val gradient = Paint().apply {
            shader = LinearGradient(
                0f,
                HEIGHT * 0.45f,
                0f,
                HEIGHT.toFloat(),
                Color.TRANSPARENT,
                Color.argb(230, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), gradient)

        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            letterSpacing = 0.08f
        }
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 96f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
        }

        val left = 72f
        var y = HEIGHT - 420f
        drawBrand(canvas, context, text.brand, left, y, small)
        y += 120f
        canvas.drawText(state.workoutName.ifBlank { text.fallbackWorkoutName }, left, y, title)
        y += 78f
        canvas.drawText(state.formattedDate, left, y, subtitle)
        if (text.duration.isNotBlank()) {
            y += 68f
            canvas.drawText(text.duration, left, y, subtitle)
        }
        if (state.totalPhases > 0) {
            y += 68f
            canvas.drawText(text.completedPhases, left, y, subtitle)
        }
    }

    private fun drawBrand(
        canvas: Canvas,
        context: Context,
        brand: String,
        left: Float,
        baseline: Float,
        paint: Paint,
    ) {
        val logoHeight = 54
        val logoWidth = 68
        val logoTop = (baseline - logoHeight + 4).toInt()
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_monochrome_trimmed)?.mutate()?.apply {
            setTint(Color.WHITE)
            setBounds(left.toInt(), logoTop, left.toInt() + logoWidth, logoTop + logoHeight)
            draw(canvas)
        }
        canvas.drawText(brand, left + logoWidth + 18f, baseline, paint)
    }
}
