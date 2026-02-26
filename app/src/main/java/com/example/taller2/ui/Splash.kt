package com.example.taller2.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Navegar a la pantalla Home
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            // Cerrar la pantalla Splash para que no se pueda volver atrás
            finish()
        }, 3000)
    }
}