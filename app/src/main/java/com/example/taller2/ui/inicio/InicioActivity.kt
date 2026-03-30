package com.example.taller2.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R
import com.example.taller2.ui.auth.LoginActivity
import com.example.taller2.ui.auth.RegisterActivity

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Buscamos los elementos que nos lleven donde queremos
        val btnComienza = findViewById<Button>(R.id.btnComienza)
        val txtRegistrate = findViewById<TextView>(R.id.txtRegistrate)

        //Le decimos qué hacer al hacer clic en Comenzar
        btnComienza.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //Le decimos qué hacer al hacer clic en Registrate
        txtRegistrate.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}