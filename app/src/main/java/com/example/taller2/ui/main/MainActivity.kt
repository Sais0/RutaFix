package com.example.taller2.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.ui.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.taller2.ui.auth.LoginActivity
import com.example.taller2.ui.main.productos.HomeFragment
import com.example.taller2.ui.main.productos.CategoriasFragment
import com.example.taller2.ui.main.productos.CarritoFragment
import com.example.taller2.ui.main.productos.FavoritosFragment
import com.example.taller2.ui.main.perfil.PerfilFragment
import com.example.taller2.ui.main.admin.AdminFragment
import com.example.taller2.ui.main.admin.UsuariosFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Cargar el HomeFragment por defecto al entrar
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            navView.setCheckedItem(R.id.nav_home)
        }

        //LÓGICA DEL MENÚ INFERIOR
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_catalogo -> replaceFragment(CategoriasFragment())
                R.id.nav_carrito -> replaceFragment(CarritoFragment())
                R.id.nav_perfil -> replaceFragment(PerfilFragment())
            }
            true
        }

        //LÓGICA DEL MENÚ LATERAL
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favoritos -> replaceFragment(FavoritosFragment())
                R.id.nav_admin -> replaceFragment(AdminFragment())
                R.id.nav_usuarios -> replaceFragment(UsuariosFragment())
                R.id.nav_salir -> {
                    lifecycleScope.launch {
                        try {
                            SupabaseClient.client.auth.signOut()

                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            // Si falla el internet, al menos lo mandamos al Login
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}