package com.example.sudokusolver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sudokusolver.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                CameraActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhotoButton.setOnClickListener {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(intent)
        }
    }
    companion object {
        init {
            if (!OpenCVLoader.initLocal()) {
                Log.d("My App", "Unable to load OpenCV")
            } else {
                Log.d("My App", "OpenCV loaded")
            }
        }
    }
}