package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val name: String,
    val sellingPrice: Double,
    val costPrice: Double,
    val stockQuantity: Int,
    val lowStockThreshold: Int
)
