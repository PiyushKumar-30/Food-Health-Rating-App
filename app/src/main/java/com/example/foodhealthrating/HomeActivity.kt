package com.example.foodhealthrating.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodhealthrating.MainActivity
import com.example.foodhealthrating.R
import com.example.foodhealthrating.adapter.ProductAdapter
import com.example.foodhealthrating.model.Product

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Verify that recyclerView and btnScan IDs match your XML
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val products = listOf(
            Product("Apple", "52 kcal", R.drawable.apple),
            Product("Banana", "96 kcal", R.drawable.banana),
            Product("Milk", "42 kcal per 100ml", R.drawable.milk),
            Product("Bread", "265 kcal", R.drawable.bread),
            Product("Egg", "155 kcal", R.drawable.egg)
        )

        recyclerView.adapter = ProductAdapter(products) { selectedProduct ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnScan = findViewById<Button>(R.id.btnScan)
        btnScan.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
