package com.example.soutezapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initButtons()

        }

    private fun initButtons(){
        val skullButton = findViewById<Button>(R.id.skullActivityButton)
        skullButton.setOnClickListener {
            // Spustíme PuzzleActivity
            val intent = Intent(this, SkullActivity::class.java)
            startActivity(intent)
        }

        val tgmButton = findViewById<Button>(R.id.TGMActivityButton)
        tgmButton.setOnClickListener {
            // Spustíme PuzzleActivity
            val intent = Intent(this, TGMActivity::class.java)
            startActivity(intent)
        }

    }
    }