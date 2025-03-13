package com.example.foodhealthrating.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodhealthrating.MainActivity
import com.example.foodhealthrating.R
import com.example.foodhealthrating.adapter.ProductAdapter
import com.example.foodhealthrating.model.Product
import android.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView
import android.view.LayoutInflater
import android.widget.Toast
import android.app.Activity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

private var selectedImageUri: Uri? = null

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf(
        Product("Apple", "52 kcal", imageResId = R.drawable.apple),
        Product("Banana", "96 kcal", imageResId = R.drawable.banana),
        Product("Milk", "42 kcal per 100ml", imageResId = R.drawable.milk),
        Product("Bread", "265 kcal", imageResId = R.drawable.bread),
        Product("Egg", "155 kcal", imageResId = R.drawable.egg),
        Product("Rice", "130 kcal per 100g", imageResId = R.drawable.rice),
        Product("Chicken", "239 kcal per 100g", imageResId = R.drawable.chicken),
        Product("Cheese", "402 kcal per 100g", imageResId = R.drawable.cheese),
        Product("Tomato", "18 kcal per 100g", imageResId = R.drawable.tomato),
        Product("Potato", "77 kcal per 100g", imageResId = R.drawable.potato),
        Product("Orange", "47 kcal", imageResId = R.drawable.orange),
        Product("Carrot", "41 kcal", imageResId = R.drawable.carrot)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        productAdapter = ProductAdapter(products) { selectedProduct ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        recyclerView.adapter = productAdapter

        val btnScan = findViewById<Button>(R.id.btnScan)
        btnScan.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnAddProduct = findViewById<Button>(R.id.btnAddProduct)
        btnAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun addProduct(name: String, calories: String, imageUri: Uri? = null, imageResId: Int? = null) {
        products.add(Product(name, calories, imageUri, imageResId)) // ✅ Supports both URI & Drawable
        productAdapter.notifyDataSetChanged()
    }




    private fun showAddProductDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Product")

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_product, null)
        builder.setView(dialogView)

        val etProductName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)
        val ivProductImage = dialogView.findViewById<ImageView>(R.id.ivProductImage)

        // Open Gallery when ImageView is clicked
        ivProductImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        builder.setPositiveButton("Add") { _, _ ->
            val name = etProductName.text.toString().trim()
            val calories = etCalories.text.toString().trim()

            if (name.isNotEmpty() && calories.isNotEmpty()) {
                if (selectedImageUri != null) {
                    addProduct(name, "$calories kcal", imageUri = selectedImageUri) // ✅ Pass URI
                } else {
                    addProduct(name, "$calories kcal", imageResId = R.drawable.apple) // ✅ Use an actual drawable

                }
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }



        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

}
