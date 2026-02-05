package com.kanwich.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextQuantity: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var dbHelper: ProductDbHelper

    private var productId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)

        // Initialize database helper
        dbHelper = ProductDbHelper(this)

        // Initialize views
        initViews()

        // Check if editing existing product
        productId = intent.getIntExtra("PRODUCT_ID", -1)
        isEditMode = productId != -1

        if (isEditMode) {
            title = "แก้ไขสินค้า"
            loadProductData()
        } else {
            title = "เพิ่มสินค้า"
        }

        // Setup button listeners
        buttonSave.setOnClickListener {
            saveProduct()
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextQuantity = findViewById(R.id.editTextQuantity)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
    }

    private fun loadProductData() {
        val product = dbHelper.getProductById(productId)
        product?.let {
            editTextName.setText(it.name)
            editTextPrice.setText(it.price.toString())
            editTextQuantity.setText(it.quantity.toString())
        }
    }

    private fun saveProduct() {
        val name = editTextName.text.toString().trim()
        val priceStr = editTextPrice.text.toString().trim()
        val quantityStr = editTextQuantity.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            editTextName.error = "กรุณากรอกชื่อสินค้า"
            editTextName.requestFocus()
            return
        }

        if (priceStr.isEmpty()) {
            editTextPrice.error = "กรุณากรอกราคา"
            editTextPrice.requestFocus()
            return
        }

        if (quantityStr.isEmpty()) {
            editTextQuantity.error = "กรุณากรอกจำนวน"
            editTextQuantity.requestFocus()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price < 0) {
            editTextPrice.error = "ราคาไม่ถูกต้อง"
            editTextPrice.requestFocus()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity < 0) {
            editTextQuantity.error = "จำนวนไม่ถูกต้อง"
            editTextQuantity.requestFocus()
            return
        }

        // Save product
        val product = Product(
            id = if (isEditMode) productId else 0,
            name = name,
            price = price,
            quantity = quantity,
            imageUrl = ""
        )

        val result = if (isEditMode) {
            dbHelper.updateProduct(product)
        } else {
            dbHelper.addProduct(product)
        }

        if (result > 0) {
            val message = if (isEditMode) "แก้ไขสินค้าเรียบร้อย" else "เพิ่มสินค้าเรียบร้อย"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}