package com.kanwich.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var products: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageviewproduct)
        val textViewName: TextView = itemView.findViewById(R.id.textviewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textviewprice)
        val textViewQuantity: TextView = itemView.findViewById(R.id.textviewquantity)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonedit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttondelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.textViewName.text = product.name
        holder.textViewPrice.text = "ราคา: ฿${String.format("%.2f", product.price)}"
        holder.textViewQuantity.text = "จำนวน: ${product.quantity} ชิ้น"

        // ถ้ามี URL รูปภาพ สามารถใช้ Glide หรือ Picasso โหลดรูป
        // สำหรับตัวอย่างนี้จะใช้รูป placeholder
        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)

        // คลิกที่รายการเพื่อดูรายละเอียด
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        // ปุ่มแก้ไข
        holder.buttonEdit.setOnClickListener {
            onEditClick(product)
        }

        // ปุ่มลบ
        holder.buttonDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    // อัพเดทรายการสินค้า
    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    // ลบสินค้า
    fun removeProduct(product: Product) {
        val position = products.indexOf(product)
        if (position != -1) {
            products.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}