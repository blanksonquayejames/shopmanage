package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

class ShopRepositoryImpl(
    private val productDao: ProductDao,
    private val saleDao: SaleDao
) : ShopRepository {

    override fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    override suspend fun getProductById(id: Long): ProductEntity? {
        return productDao.getProductById(id)
    }

    override suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    override suspend fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        saleDao.executeSale(sale, items)
    }

    override suspend fun addStock(productId: Long, amount: Int) {
        productDao.addStock(productId, amount)
    }

    override fun getAllSales(): Flow<List<SaleEntity>> {
        return saleDao.getAllSales()
    }

    override suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity> {
        return saleDao.getItemsForSale(saleId)
    }
}
