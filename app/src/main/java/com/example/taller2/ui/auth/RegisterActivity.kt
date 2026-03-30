package com.example.taller2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiy_register)

        //Buscamos los elementos que queremos que nos devuelvan al LoginActivity
        val txtVolver = findViewById<TextView>(R.id.txtVolver)
        val txtIniciaSesion = findViewById<TextView>(R.id.txtIniciaSesion)

        //Programamos el clic de "Volver"
        txtVolver.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //Programamos el clic de "Inicia sesión"
        txtIniciaSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}