package com.example.taller2.ui.main.perfil

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import com.example.taller2.ui.Usuario
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilFragment : Fragment() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmarPassword: EditText
    private lateinit var btnGuardar: Button
    private lateinit var ivPerfilFoto: ImageView
    private lateinit var btnCambiarFoto: ImageButton

    private var fotoUri: Uri? = null
    private var fotoUrlActual: String? = null
    private var rolActual: String? = "usuario"
    private var cameraImageFile: File? = null

    // Launcher para Galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoUri = it
            ivPerfilFoto.load(it)
        }
    }

    // Launcher para Cámara (Mejorado para evitar crash)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fotoUri?.let { uri ->
                ivPerfilFoto.load(uri)
            }
        }
    }

    // Launcher para pedir permiso de Cámara
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

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
        ivPerfilFoto = view.findViewById(R.id.ivPerfilFoto)
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto)

        cargarDatosUsuario()

        btnCambiarFoto.setOnClickListener { mostrarOpcionesImagen() }
        btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Cámara", "Galería")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar foto de perfil")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Se requiere el permiso para tomar fotos", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        try {
            val photoFile: File = crearArchivoImagen()
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            fotoUri = photoURI
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            cameraLauncher.launch(intent)
        } catch (_: IOException) {
            Toast.makeText(requireContext(), "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun crearArchivoImagen(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            cameraImageFile = this
        }
    }

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
                    fotoUrlActual = usuarioBD.foto_url
                    rolActual = usuarioBD.rol

                    if (!fotoUrlActual.isNullOrEmpty()) {
                        ivPerfilFoto.load(fotoUrlActual) {
                            crossfade(true)
                            placeholder(R.drawable.logo)
                            error(R.drawable.logo)
                            // Quitamos CircleCropTransformation porque el ShapeableImageView ya lo hace circular
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun subirImagenASupabase(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@withContext null
                val fileName = "perfil_${user.id}.jpg" // Sobrescribir para ahorrar espacio
                val bucket = SupabaseClient.client.storage.from("avatars")

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext null
                inputStream.close()

                bucket.upload(fileName, bytes) { upsert = true }
                
                val publicUrl = bucket.publicUrl(fileName)
                "$publicUrl?t=${System.currentTimeMillis()}"
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoApellido = etApellido.text.toString().trim()
        val nuevaPass = etPassword.text.toString()
        val reNuevaPass = etConfirmarPassword.text.toString()

        if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty()) {
            Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnGuardar.text = getString(R.string.guardando)

        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                var urlFinal = fotoUrlActual
                if (fotoUri != null && fotoUri.toString().startsWith("content")) {
                    val nuevaUrl = subirImagenASupabase(fotoUri!!)
                    if (nuevaUrl != null) urlFinal = nuevaUrl
                }

                val usuarioActualizado = Usuario(
                    id = user.id,
                    nombres = nuevoNombre,
                    apellidos = nuevoApellido,
                    correo = etCorreo.text.toString(),
                    rol = rolActual,
                    foto_url = urlFinal
                )

                SupabaseClient.client.postgrest["Usuarios"].update(usuarioActualizado) {
                    filter { eq("id", user.id) }
                }

                if (nuevaPass.isNotEmpty()) {
                    if (nuevaPass == reNuevaPass && nuevaPass.length >= 6) {
                        SupabaseClient.client.auth.updateUser { password = nuevaPass }
                        etPassword.text.clear()
                        etConfirmarPassword.text.clear()
                    } else {
                        Toast.makeText(requireContext(), "Contraseña inválida o no coinciden", Toast.LENGTH_SHORT).show()
                    }
                }
                
                Toast.makeText(requireContext(), getString(R.string.perfil_actualizado), Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnGuardar.isEnabled = true
                btnGuardar.text = getString(R.string.guardar_cambios)
            }
        }
    }
}
