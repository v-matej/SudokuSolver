package com.example.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class StorageHelper(private val context: Context) {

     fun getOutputDirectory(): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: context.filesDir
    }

    fun saveSudokuGrid(mat: Mat): Uri? {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        val filePath = File(getOutputDirectory(), "grid_sudoku.jpg")

        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            return Uri.fromFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
        return null
    }

    fun saveCells(cells: List<Mat>) {
        val outputDirectory = getOutputDirectory()
        val directory = File(outputDirectory, "cells")

        // Check if the directory exists and delete its contents if it does
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { it.delete() }
        } else {
            directory.mkdirs() // Create the directory if it doesn't exist
        }

        for ((index, cell) in cells.withIndex()) {
            val croppedCell = formatCell(cell)

            val bitmap = Bitmap.createBitmap(croppedCell.cols(), croppedCell.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(croppedCell, bitmap)

            val fileName = "${index + 1}.jpg"
            val filePath = File(directory, fileName)

            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(filePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                Log.d("SaveImage", "Saved $fileName")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                outputStream?.close()
            }
        }
    }


    private fun formatCell(mat: Mat): Mat {
        val xOffset = (mat.cols() - 28) / 2
        val yOffset = (mat.rows() - 28) / 2
        return Mat(mat, Rect(xOffset, yOffset, 28, 28))
    }

    fun loadImages(): List<Bitmap> {
        val directoryPath = getOutputDirectory();
        val images = mutableListOf<Bitmap>()

        val dir = File(directoryPath, "cells")

        if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles()

            for (file in files) {
                if (file.isFile && isImageFile(file)) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    images.add(bitmap)
                }
            }
        }

        return images
    }

    fun isImageFile(file: File): Boolean {
        val fileName = file.name.toLowerCase()
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif") || fileName.endsWith(".bmp")
    }

}
