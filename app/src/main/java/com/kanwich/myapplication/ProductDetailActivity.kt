package com.kanwich.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textViewName: TextView
    private lateinit var textViewPrice: TextView
    private lateinit var textViewQuantity: TextView
    private lateinit var buttonClose: Button
    private lateinit var dbHelper: ProductDbHelper

    private var productId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize database helper
        dbHelper = ProductDbHelper(this)

        // Initialize views
        initViews()

        // Get product ID
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        if (productId != -1) {
            loadProductDetail()
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสินค้า", Toast.LENGTH_SHORT).show()
            finish()
        }

        buttonClose.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageViewProductDetail)
        textViewName = findViewById(R.id.textViewNameDetail)
        textViewPrice = findViewById(R.id.textViewPriceDetail)
        textViewQuantity = findViewById(R.id.textViewQuantityDetail)
        buttonClose = findViewById(R.id.buttonClose)
    }

    private fun loadProductDetail() {
        val product = dbHelper.getProductById(productId)

        product?.let {
            title = it.name
            textViewName.text = it.name
            textViewPrice.text = "ราคา: ฿${String.format("%.2f", it.price)}"
            textViewQuantity.text = "จำนวนในสต็อก: ${it.quantity} ชิ้น"

            // โหลดรูปภาพ
            if (it.imageUrl.isNotEmpty() && File(it.imageUrl).exists()) {
                val bitmap = BitmapFactory.decodeFile(it.imageUrl)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } ?: run {
            Toast.makeText(this, "ไม่พบข้อมูลสินค้า", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}