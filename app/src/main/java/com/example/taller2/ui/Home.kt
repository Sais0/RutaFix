package com.example.taller2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.R
import android.widget.Button
import android.content.Intent
import android.widget.TextView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Buscamos los elementos que nos lleven donde queremos
        val btnComienza = findViewById<Button>(R.id.btnComienza)
        val txtRegistrate = findViewById<TextView>(R.id.txtRegistrate)

        //Le decimos qué hacer al hacer clic en Comenzar
        btnComienza.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        //Le decimos qué hacer al hacer clic en Registrate
        txtRegistrate.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}