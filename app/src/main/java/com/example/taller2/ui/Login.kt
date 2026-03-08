package com.example.taller2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Buscamos los elementos que nos lleven al Registro
        val txtRegistrateLogin = findViewById<TextView>(R.id.txtRegistrateLogin)

        //Programamos el clic de "Registrate"
        txtRegistrateLogin.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}