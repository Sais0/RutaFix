package com.example.taller2.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Navegar a la pantalla InicioActivity
            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)

            // Cerrar la pantalla SplashActivity para que no se pueda volver atrás
            finish()
        }, 3000)
    }
}