package com.example.soutezapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.utils.UIUtils

class DinoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dino)
        UIUtils.hideSystemUI(this);

        // Get the views
        val imageView = findViewById<ImageView>(R.id.imageView)
        val drawingView = findViewById<DrawingView>(R.id.drawingView)

        // Load an image from resources (or anywhere else)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.buri)

        // Set the image in the ImageView
        imageView.setImageBitmap(bitmap)

    }
}