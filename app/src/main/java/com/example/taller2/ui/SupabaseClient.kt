package com.example.taller2.ui

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val rol: String? = "usuario",
    val foto_url: String? = null
)
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://etjnzvgjfzeebthozvyi.supabase.co",
        supabaseKey = "sb_publishable_a6ScpW1-DMQfRwjg7oBUFg_Xmpw3e6J"
    ) {
        install(Auth){
            scheme = "supabase"
            host = "login-callback"
        }
        install(Postgrest)
        install(Storage)
    }
}

