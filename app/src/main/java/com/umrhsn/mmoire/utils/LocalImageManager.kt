package com.umrhsn.mmoire.utils

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir = File(context.filesDir, "custom_images")

    init {
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
    }

    fun saveImage(gameName: String, index: Int, byteArray: ByteArray): String {
        val gameDir = File(imagesDir, gameName)
        if (!gameDir.exists()) {
            gameDir.mkdirs()
        }
        val fileName = "img_$index.jpg"
        val file = File(gameDir, fileName)

        FileOutputStream(file).use { out ->
            out.write(byteArray)
        }
        return Uri.fromFile(file).toString()
    }

    fun deleteGameImages(gameName: String) {
        val gameDir = File(imagesDir, gameName)
        if (gameDir.exists()) {
            gameDir.deleteRecursively()
        }
    }
}
