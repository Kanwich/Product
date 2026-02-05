package com.kanwich.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddProduct: ExtendedFloatingActionButton
    private lateinit var productAdapter: ProductAdapter
    private lateinit var dbHelper: ProductDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database helper
        dbHelper = ProductDbHelper(this)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerviewProduct)
        fabAddProduct = findViewById(R.id.fabAddProduct)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup FAB
        fabAddProduct.text = "เพิ่มสินค้า"
        fabAddProduct.setIconResource(android.R.drawable.ic_input_add)
        fabAddProduct.setOnClickListener {
            val intent = Intent(this, AddEditProductActivity::class.java)
            startActivity(intent)
        }

        // Load products
        loadProducts()
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onEditClick = { product ->
                val intent = Intent(this, AddEditProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            },
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = productAdapter
        }
    }

    private fun loadProducts() {
        val products = dbHelper.getAllProducts()
        productAdapter.updateProducts(products)

        if (products.isEmpty()) {
            Toast.makeText(this, "ยังไม่มีสินค้า กรุณาเพิ่มสินค้า", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการลบ")
            .setMessage("คุณต้องการลบสินค้า \"${product.name}\" ใช่หรือไม่?")
            .setPositiveButton("ลบ") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        val result = dbHelper.deleteProduct(product.id)
        if (result > 0) {
            productAdapter.removeProduct(product)
            Toast.makeText(this, "ลบสินค้าเรียบร้อย", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการลบสินค้า", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}