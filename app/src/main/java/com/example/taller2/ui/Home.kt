package com.example.taller2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R
import android.widget.Button
import android.content.Intent

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val btnComienza = findViewById<Button>(R.id.btnComienza)

        //Le decimos qué hacer al hacer clic
        btnComienza.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}