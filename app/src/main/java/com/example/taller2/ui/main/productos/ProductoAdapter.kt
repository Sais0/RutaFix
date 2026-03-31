package com.example.taller2.ui.main.productos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taller2.R

class ProductoAdapter(private val lista: List<Product>) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioProducto)
        val ivImagen: ImageView = view.findViewById(R.id.ivProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "$ ${item.precio}"
        holder.ivImagen.setImageResource(item.imageRes)
    }

    override fun getItemCount(): Int = lista.size
}