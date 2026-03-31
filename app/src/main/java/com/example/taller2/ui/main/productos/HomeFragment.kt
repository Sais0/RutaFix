package com.example.taller2.ui.main.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller2.R

class HomeFragment : Fragment() {

    private val listaProductos = listOf(
        Product(nombre = "Casco de Seguridad", precio = 120.000, imageRes = R.drawable.logo),
        Product(nombre = "Cinta Reflectiva", precio = 45.000, imageRes = R.drawable.logo),
        Product(nombre = "Chaleco Vial", precio = 30.000, imageRes = R.drawable.logo),
        Product(nombre = "Cono Señalización", precio = 25.000, imageRes = R.drawable.logo)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_productos)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        recyclerView.adapter = ProductoAdapter(listaProductos)

        return view
    }
}