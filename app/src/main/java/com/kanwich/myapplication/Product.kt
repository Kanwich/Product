package com.kanwich.myapplication

data class Product(
    val id: Int,
    var name: String,
    var price: Double,
    var quantity: Int,
    var imageUrl: String = ""
)