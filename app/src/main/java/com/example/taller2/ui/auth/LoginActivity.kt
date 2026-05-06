package com.example.taller2.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import com.example.taller2.ui.Usuario
import com.example.taller2.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        // TODO: Reemplazar con el ID de cliente WEB real desde Google Cloud Console
        private const val WEB_CLIENT_ID = "961817882269-0i8j4itidclst15f4o20ndrre71p76un.apps.googleusercontent.com"
    }

    // Launcher para el selector nativo de Google
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    Log.d(TAG, "Google ID Token obtenido con éxito")
                    loginWithSupabaseIDToken(idToken)
                } else {
                    Log.w(TAG, "Google Sign-In exitoso pero el ID Token es NULO. Verifica la configuración en Google Cloud Console.")
                    Toast.makeText(this, getString(R.string.error_google_token), Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Error Google Sign-In. Código de estado: ${e.statusCode}. Mensaje: ${e.message}", e)
                Toast.makeText(this, "Error Google (${e.statusCode}): ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Google Sign-In cancelado o fallido. ResultCode: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnGoogle = findViewById<View>(R.id.btnGoogle)
        val btnBiometrico = findViewById<ImageButton>(R.id.btnBiometrico)
        val txtRegistrateLogin = findViewById<TextView>(R.id.txtRegistrateLogin)

        val sharedPref = getSharedPreferences("RutaFixPrefs", Context.MODE_PRIVATE)
        val haIngresadoAntes = sharedPref.getBoolean("ha_ingresado", false)

        // Lógica de Huella: Solo si ya ha ingresado exitosamente antes
        if (haIngresadoAntes) {
            btnBiometrico.visibility = View.VISIBLE
            mostrarLectorHuella() // Se activa automáticamente solo si ya es usuario conocido
        } else {
            // Si nunca ha entrado, ocultamos o deshabilitamos visualmente el botón
            btnBiometrico.alpha = 0.5f
        }

        btnIngresar.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val pass = etContrasena.text.toString()
            if (correo.isNotEmpty() && pass.isNotEmpty()) {
                loginEmail(correo, pass)
            } else {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogle.setOnClickListener { loginGoogleNative() }

        btnBiometrico.setOnClickListener {
            if (sharedPref.getBoolean("ha_ingresado", false)) {
                mostrarLectorHuella()
            } else {
                Toast.makeText(this, getString(R.string.error_biometrico_primera_vez), Toast.LENGTH_LONG).show()
            }
        }

        txtRegistrateLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginEmail(email: String, pass: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Intentando inicio de sesión con correo: $email")
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                Log.d(TAG, "Inicio de sesión con correo exitoso")
                guardarRegistroExitoso()
                irAlMain()
            } catch (e: Exception) {
                Log.e(TAG, "Error en inicio de sesión con correo: ${e.message}", e)
                Toast.makeText(this@LoginActivity, getString(R.string.error_prefijo) + (e.message ?: getString(R.string.error_datos_incorrectos)), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginGoogleNative() {
        // --- CONFIGURACIÓN DE GOOGLE NATIVO ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Cerramos sesión previa para forzar el selector de cuentas cada vez
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Iniciando selector de cuentas de Google...")
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun loginWithSupabaseIDToken(idToken: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Intentando autenticación con Supabase usando ID Token...")
                // Intercambiamos el token de Google por una sesión de Supabase
                SupabaseClient.client.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                Log.d(TAG, "Autenticación con Supabase exitosa")
                
                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user != null) {
                    crearUsuarioSiNoExiste(user)
                }

                guardarRegistroExitoso()
                irAlMain()
            } catch (e: Exception) {
                Log.e(TAG, "Error vinculando con Supabase. Mensaje: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "${getString(R.string.error_google_general)}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarRegistroExitoso() {
        val sharedPref = getSharedPreferences("RutaFixPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("ha_ingresado", true)
            apply()
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

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.titulo_biometrico))
            .setSubtitle(getString(R.string.subtitulo_biometrico))
            .setNegativeButtonText(getString(R.string.boton_biometrico_negativo))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private suspend fun crearUsuarioSiNoExiste(user: UserInfo) {
        try {
            // Intentamos buscar si ya existe
            val existe = SupabaseClient.client.postgrest[getString(R.string.tabla_usuarios)]
                .select {
                    filter { eq("id", user.id) }
                }.data != "[]"

            if (!existe) {
                // Si no existe, lo creamos con datos de Google
                val metadata = user.userMetadata
                val fullNombre = metadata?.get("full_name")?.toString()?.split(" ") ?: listOf(getString(R.string.default_nombres), getString(R.string.default_apellidos))
                val nombres = fullNombre.firstOrNull() ?: getString(R.string.default_nombres)
                val apellidos = if (fullNombre.size > 1) fullNombre.subList(1, fullNombre.size).joinToString(" ") else ""
                
                val nuevoUsuario = Usuario(
                    id = user.id,
                    nombres = nombres,
                    apellidos = apellidos,
                    correo = user.email ?: "",
                    foto_url = metadata?.get("avatar_url")?.toString()
                )
                
                SupabaseClient.client.postgrest[getString(R.string.tabla_usuarios)].insert(nuevoUsuario)
                Log.d(TAG, "Nuevo usuario de Google creado en la tabla Usuarios")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar/crear usuario en BD: ${e.message}")
        }
    }

    private fun irAlMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
