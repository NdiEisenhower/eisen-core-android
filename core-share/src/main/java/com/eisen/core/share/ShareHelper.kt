package com.eisen.core.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareText(context: Context, text: String, title: String? = "Share") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun shareLink(context: Context, url: String, text: String? = null, title: String? = "Share Link") {
        val message = if (!text.isNullOrBlank()) "$text\n$url" else url
        shareText(context, message, title)
    }

    fun shareScore(
        context: Context,
        gameName: String,
        score: Long,
        storeUrl: String? = null
    ) {
        val baseMsg = "I just scored $score in $gameName! Can you beat me?"
        val fullMsg = if (!storeUrl.isNullOrBlank()) "$baseMsg\nPlay now: $storeUrl" else baseMsg
        shareText(context, fullMsg, "Share High Score")
    }

    fun shareImage(
        context: Context,
        bitmap: Bitmap,
        title: String? = "Share",
        message: String? = null
    ) {
        try {
            val cacheFolder = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(cacheFolder, "share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val authority = "${context.packageName}.eisen.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (!message.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun captureView(
        activity: Activity,
        targetView: View? = null,
        onResult: (Bitmap?) -> Unit
    ) {
        val view = targetView ?: activity.window.decorView.rootView
        if (view.width <= 0 || view.height <= 0) {
            onResult(null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val rect = android.graphics.Rect(
                location[0],
                location[1],
                location[0] + view.width,
                location[1] + view.height
            )

            PixelCopy.request(
                activity.window,
                rect,
                bitmap,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        onResult(bitmap)
                    } else {
                        // Fallback to Canvas draw
                        onResult(captureViaCanvas(view))
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } else {
            onResult(captureViaCanvas(view))
        }
    }

    private fun captureViaCanvas(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun captureAndShare(
        activity: Activity,
        targetView: View? = null,
        title: String? = "Share",
        message: String? = null
    ) {
        captureView(activity, targetView) { bitmap ->
            if (bitmap != null) {
                shareImage(activity, bitmap, title, message)
            } else if (!message.isNullOrBlank()) {
                shareText(activity, message, title)
            }
        }
    }
}

