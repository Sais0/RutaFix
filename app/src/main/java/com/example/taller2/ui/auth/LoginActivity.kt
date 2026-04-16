package com.example.taller2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnGoogle = findViewById<View>(R.id.btnGoogle)
        val txtRegistrateLogin = findViewById<TextView>(R.id.txtRegistrateLogin)

        // Iniciar biometría automáticamente
        mostrarLectorHuella()

        btnIngresar.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val pass = etContrasena.text.toString()
            if (correo.isNotEmpty() && pass.isNotEmpty()) loginEmail(correo, pass)
        }

        btnGoogle.setOnClickListener { loginGoogle() }

        txtRegistrateLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginEmail(email: String, pass: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                irAlMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: Datos incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginGoogle() {
        lifecycleScope.launch {
            try {
                // Usamos el proveedor de Google pero forzando el selector de cuenta
                SupabaseClient.client.auth.signInWith(Google) {
                }
                irAlMain()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarLectorHuella() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    irAlMain()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Biométrico")
            .setSubtitle("Usa tu huella para entrar")
            .setNegativeButtonText("Usar clave")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun irAlMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}