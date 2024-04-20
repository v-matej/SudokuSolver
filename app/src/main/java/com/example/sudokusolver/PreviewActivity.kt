package com.example.sudokusolver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sudokusolver.databinding.ActivityPreviewBinding
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPreviewBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_preview
        )
        val photoUriString = intent.getStringExtra("PHOTO_URI")
        val photoUri = Uri.parse(photoUriString)

        val bmpFactoryOptions = BitmapFactory.Options()
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888

        val bmp = MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            photoUri
        )

        val matObj = Mat(bmp.width, bmp.height, CvType.CV_8UC4)
        Utils.bitmapToMat(bmp,matObj)

        val output = LineDetectionHelper.detectSudoku(matObj)

        val gridUri = saveMatAsJpeg(output)

        Glide.with(this)
            .load(gridUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.photoImageView)
    }


    private fun saveMatAsJpeg(mat: Mat): Uri? {

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

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }
}
