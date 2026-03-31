package com.example.taller2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R
import com.example.taller2.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Buscamos los elementos en la vista
        val txtRegistrateLogin = findViewById<TextView>(R.id.txtRegistrateLogin)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        // Clic de "Registrate"
        txtRegistrateLogin.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Clic de "Ingresar"
        btnIngresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}