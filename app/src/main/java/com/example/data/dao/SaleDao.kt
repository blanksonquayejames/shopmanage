package com.example.data.dao

import androidx.room.*
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId")
    suspend fun decrementProductStock(productId: Long, quantity: Int)

    @Transaction
    suspend fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithSaleId)
        for (item in itemsWithSaleId) {
            decrementProductStock(item.productId, item.quantity)
        }
    }

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>
}
