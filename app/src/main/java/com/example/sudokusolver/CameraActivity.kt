package com.example.sudokusolver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.sudokusolver.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    val storageHelper = StorageHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCameraBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_camera
        )

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera(binding)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera(binding: ActivityCameraBinding) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                // Handle any errors
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(
            storageHelper.getOutputDirectory(),
             "sudoku.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(this@CameraActivity, "Photo captured!", Toast.LENGTH_SHORT).show()

                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    val croppedBitmap = cropAndRotateBitmapToSquare(bitmap)

                    val croppedPhotoFile = File(
                        storageHelper.getOutputDirectory(),
                        "cropped_sudoku.jpg"
                    )

                    val outputStream = FileOutputStream(croppedPhotoFile)
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()

                    val croppedPhotoUri = Uri.fromFile(croppedPhotoFile)
                    navigateToPreviewActivity(croppedPhotoUri)
                }
            })
    }

    private fun navigateToPreviewActivity(photoUri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("PHOTO_URI", photoUri.toString())
        }
        startActivity(intent)
    }

    private fun cropAndRotateBitmapToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val size = if (width < height) width else height
        val left = (width - size) / 2
        val top = (height - size) / 2

        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, size, size)

        val matrix = Matrix().apply {
            postRotate(90f)
        }
        return Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true)
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
    }
}