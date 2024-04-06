package com.example.sudokusolver

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.sudokusolver.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPreviewBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_preview
        )


        val photoUriString = intent.getStringExtra("PHOTO_URI")
        val photoUri = Uri.parse(photoUriString)
        Glide.with(this).load(photoUri).into(binding.photoImageView)
    }
}
