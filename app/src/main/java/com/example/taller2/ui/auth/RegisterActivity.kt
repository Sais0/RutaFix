package com.example.taller2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import com.example.taller2.ui.Usuario
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiy_register)

        val txtIniciaSesion = findViewById<TextView>(R.id.txtIniciaSesion)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo = findViewById<EditText>(R.id.etCorreoReg)
        val etPassword = findViewById<EditText>(R.id.etContrasenaReg)
        val etConfirmarPassword = findViewById<EditText>(R.id.etConfirmarContrasena)
        val cbTerminos = findViewById<CheckBox>(R.id.cbTerminos)

        txtIniciaSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegistrarse.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmarPassword.text.toString()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerminos.isChecked) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registro en Supabase
            registrarEnSupabase(nombre, apellido, correo, password)
        }
    }

    private fun registrarEnSupabase(nombre: String, apellido: String, correo: String, pass: String) {
        lifecycleScope.launch {
            try {
                // A. Crear usuario en Auth de Supabase
                val user = SupabaseClient.client.auth.signUpWith(Email) {
                    email = correo
                    password = pass
                }

                // B. Obtener el ID del usuario creado (Nota: en versiones recientes de Supabase-kt puede variar el acceso)
                // Usamos la sesión si el registro fue exitoso
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id 
                    ?: throw Exception("No se pudo obtener el ID del usuario")

                // C. Insertar datos adicionales en la tabla "Usuarios" (coincidiendo con tu Supabase)
                val nuevoUsuario = Usuario(
                    id = userId,
                    nombres = nombre,
                    apellidos = apellido,
                    correo = correo
                )

                SupabaseClient.client.postgrest["Usuarios"].insert(nuevoUsuario)

                Toast.makeText(this@RegisterActivity, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                
                // Volver al Login
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
