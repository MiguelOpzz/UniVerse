package com.clerami.universe.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

fun compressAndResizeImage(uri: Uri, contentResolver: ContentResolver, maxWidth: Int = 800, quality: Int = 80): Bitmap? {
    return try {
        // Open the input stream for the image
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null

        // Decode the bitmap
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        // Rotate the image to fix its orientation (if needed)
        val rotatedBitmap = rotateImageIfRequired(originalBitmap, uri, contentResolver)

        // Resize the image while maintaining the aspect ratio
        val width = maxWidth
        val height = (rotatedBitmap.height * (width.toFloat() / rotatedBitmap.width.toFloat())).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, width, height, true)

        // Compress the image to reduce file size
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // Adjust quality as needed

        // Convert the byte array back to a Bitmap
        BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri, contentResolver: ContentResolver): Bitmap {
    val exif = ExifInterface(contentResolver.openInputStream(uri)!!)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
        else -> bitmap // No rotation needed
    }
}

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
