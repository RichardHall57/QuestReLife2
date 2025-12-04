package com.example.questrelife.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Convert any Drawable to Bitmap safely.
     * Works with BitmapDrawable or vector drawables.
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Load bitmap from internal path or content URI.
     */
    fun loadBitmap(context: Context, identifier: String): Bitmap? {
        return try {
            if (identifier.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(identifier))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                val file = File(identifier)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap", e)
            null
        }
    }

    /**
     * Save bitmap as PNG and return the path or URI.
     * Tries MediaStore (Q+), then external files, then internal fallback.
     */
    fun saveBitmapAsPng(context: Context, bitmap: Bitmap, baseName: String = "profile"): String? {
        val fileName = "${baseName}_${System.currentTimeMillis()}.png"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(context, bitmap, fileName)
                    ?: saveInternal(context, bitmap, baseName)
            } else {
                saveToExternalPath(context, bitmap, fileName)
                    ?: saveInternal(context, bitmap, baseName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Save failed: ${e.message}", e)
            saveInternal(context, bitmap, baseName)
        }
    }

    // Save to MediaStore (Android Q+)
    private fun saveToMediaStore(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QuestReLife")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: throw Exception("Unable to open output stream for MediaStore URI")

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            uri.toString()
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore save failed: ${e.message}", e)
            null
        }
    }

    // Save to external path (pre-Q)
    private fun saveToExternalPath(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "QuestReLife")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "External path save failed: ${e.message}", e)
            null
        }
    }

    // Internal storage fallback
    private fun saveInternal(context: Context, bitmap: Bitmap, baseName: String): String? {
        return try {
            val dir = File(context.filesDir, "profile_images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${baseName}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Internal save failed: ${e.message}", e)
            null
        }
    }
}
