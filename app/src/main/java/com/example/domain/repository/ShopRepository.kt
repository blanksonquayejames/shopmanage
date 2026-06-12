package com.example.domain.repository

import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    fun getAllProducts(): Flow<List<ProductEntity>>
    suspend fun getProductById(id: Long): ProductEntity?
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>)
    suspend fun addStock(productId: Long, amount: Int)
    fun getAllSales(): Flow<List<SaleEntity>>
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>
}
