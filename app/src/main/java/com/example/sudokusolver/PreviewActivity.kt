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

    val storageHelper = StorageHelper(this)
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

        val sudokuGrid = SudokuDetectionHelper.detectSudoku(matObj)
        val cells = SudokuDetectionHelper.exitractCells(sudokuGrid)

        val gridUri = storageHelper.saveSudokuGrid(sudokuGrid)
        storageHelper.saveCells(cells)

        Glide.with(this)
            .load(gridUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.photoImageView)
    }

}
