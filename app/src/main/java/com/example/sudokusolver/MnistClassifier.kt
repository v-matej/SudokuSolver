package com.example.sudokusolver

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MnistClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    fun initialize(): Task<Void?> {
        val task = TaskCompletionSource<Void?>()
        executorService.execute {
            try {
                initializeInterpreter()
                task.setResult(null)
            } catch (e: IOException) {
                task.setException(e)
            }
        }
        return task.task
    }

    @Throws(IOException::class)
    private fun initializeInterpreter() {
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "mnist.tflite")
        val interpreter = Interpreter(model)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth *
                inputImageHeight * PIXEL_SIZE

        this.interpreter = interpreter

        isInitialized = true
        Log.d(TAG, "Initialized TFLite interpreter.")
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classifySudokuGrid(cellImages: List<Bitmap>): Array<IntArray> {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        val grid = Array(9) { IntArray(9) }

        var index = 0
        var cellNumber = 1
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                Log.d("My App", "Cell: $cellNumber")
                val prediction = classify(cellImages[index])
                grid[i][j] = prediction
                cellNumber++
                index++
            }
        }

        return grid
    }

    private fun classify(bitmap: Bitmap): Int {
        val empty = isImageEmpty(bitmap)
        Log.d("My App", "Empty: $empty")

        if (empty) {
            return 0
        }

        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }

        interpreter?.run(byteBuffer, output)

        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1

        Log.d("My App", "Classified: $maxIndex")
        return maxIndex
    }
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    private fun isImageEmpty(bitmap: Bitmap): Boolean {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)

        val binaryMat = Mat()
        Imgproc.threshold(
            grayMat,
            binaryMat,
            0.0,
            255.0,
            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU
        )

        return Core.countNonZero(binaryMat) < (binaryMat.cols() * binaryMat.rows()) * 0.1
    }

    companion object {
        private const val TAG = "MnistClassifier"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 10
    }
}
