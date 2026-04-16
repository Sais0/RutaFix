package com.example.taller2.ui.main.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import com.example.taller2.ui.Usuario
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmarPassword: EditText
    private lateinit var btnGuardar: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNombre = view.findViewById(R.id.etPerfilNombre)
        etApellido = view.findViewById(R.id.etPerfilApellido)
        etCorreo = view.findViewById(R.id.etPerfilCorreo)
        etPassword = view.findViewById(R.id.etPerfilContrasena)
        etConfirmarPassword = view.findViewById(R.id.etPerfilConfirmarContrasena)
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil)

        cargarDatosUsuario()

        btnGuardar.setOnClickListener {
            guardarCambios()
        }
    }

    // Carga los datos de Supabase al abrir el fragmento
    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user != null) {
                    val usuarioBD = SupabaseClient.client.postgrest["Usuarios"]
                        .select { filter { eq("id", user.id) } }
                        .decodeSingle<Usuario>()

                    etNombre.setText(usuarioBD.nombres)
                    etApellido.setText(usuarioBD.apellidos)
                    etCorreo.setText(usuarioBD.correo)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Actualiza los datos personales y/o contraseña
    private fun guardarCambios() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoApellido = etApellido.text.toString().trim()
        val nuevaPass = etPassword.text.toString()
        val reNuevaPass = etConfirmarPassword.text.toString()

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty()) {
            Toast.makeText(requireContext(), "Los nombres no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                val usuarioActualizado = Usuario(
                    id = user.id,
                    nombres = nuevoNombre,
                    apellidos = nuevoApellido,
                    correo = etCorreo.text.toString()
                )

                SupabaseClient.client.postgrest["Usuarios"].update(usuarioActualizado) {
                    filter { eq("id", user.id) }
                }

                if (nuevaPass.isNotEmpty()) {
                    if (nuevaPass == reNuevaPass && nuevaPass.length >= 6) {
                        SupabaseClient.client.auth.updateUser {
                            password = nuevaPass
                        }
                        Toast.makeText(requireContext(), "Perfil y contraseña actualizados", Toast.LENGTH_SHORT).show()
                        etPassword.text.clear()
                        etConfirmarPassword.text.clear()
                    } else {
                        Toast.makeText(requireContext(), "Las contraseñas no coinciden o son cortas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}