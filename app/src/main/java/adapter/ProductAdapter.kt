package com.example.foodhealthrating.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodhealthrating.R
import com.example.foodhealthrating.model.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productCalories: TextView = itemView.findViewById(R.id.product_calories)
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
        holder.productCalories.text = product.calories

        // Load image from URI or Drawable
        if (product.imageUri != null) {
            Glide.with(holder.itemView.context)
                .load(product.imageUri)
                .into(holder.productImage)
        } else if (product.imageResId != null) {
            holder.productImage.setImageResource(product.imageResId)
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount() = products.size
}
