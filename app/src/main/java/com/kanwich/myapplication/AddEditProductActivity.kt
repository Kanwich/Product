package com.kanwich.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextQuantity: EditText
    private lateinit var imageViewProduct: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var dbHelper: ProductDbHelper

    private var productId: Int = -1
    private var isEditMode = false
    private var selectedImagePath: String = ""
    private var currentPhotoUri: Uri? = null

    // Camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                selectedImagePath = saveImageToInternalStorage(uri)
                displayImage(selectedImagePath)
            }
        }
    }

    // Gallery launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImagePath = saveImageToInternalStorage(it)
            displayImage(selectedImagePath)
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "ต้องการสิทธิ์กล้องเพื่อถ่ายรูป", Toast.LENGTH_SHORT).show()
        }
    }

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
        buttonSelectImage.setOnClickListener {
            showImagePickerDialog()
        }

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
        imageViewProduct = findViewById(R.id.imageViewProduct)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
    }

    private fun loadProductData() {
        val product = dbHelper.getProductById(productId)
        product?.let {
            editTextName.setText(it.name)
            editTextPrice.setText(it.price.toString())
            editTextQuantity.setText(it.quantity.toString())
            selectedImagePath = it.imageUrl
            if (selectedImagePath.isNotEmpty()) {
                displayImage(selectedImagePath)
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("ถ่ายรูป", "เลือกจากแกลเลอรี่", "ยกเลิก")
        AlertDialog.Builder(this)
            .setTitle("เลือกรูปภาพ")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let {
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureLauncher.launch(currentPhotoUri)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "PRODUCT_${timeStamp}_"
            val storageDir = getExternalFilesDir(null)
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize bitmap if too large
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            // Save to internal storage
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "product_${timeStamp}.jpg"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun displayImage(imagePath: String) {
        if (imagePath.isNotEmpty() && File(imagePath).exists()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageViewProduct.setImageBitmap(bitmap)
        } else {
            imageViewProduct.setImageResource(android.R.drawable.ic_menu_gallery)
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
            imageUrl = selectedImagePath
        )

        val result: Long = if (isEditMode) {
            dbHelper.updateProduct(product).toLong()
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