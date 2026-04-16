package com.example.taller2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import com.example.taller2.ui.main.MainActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val txtRegistrateLogin = findViewById<TextView>(R.id.txtRegistrateLogin)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)

        // Solicitar huella al abrir la pantalla
        mostrarLectorHuella()

        txtRegistrateLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Login con correo y contraseña
        btnIngresar.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val password = etContrasena.text.toString()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            iniciarSesion(correo, password)
        }
    }

    private fun mostrarLectorHuella() {
        val executor: Executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(this@LoginActivity, "¡Huella reconocida!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión en RutaFix")
            .setSubtitle("Usa tu huella digital para acceder rápido")
            .setNegativeButtonText("Usar contraseña")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun iniciarSesion(correo: String, pass: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
                    password = pass
                }
                Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
            }
        }
    }
}