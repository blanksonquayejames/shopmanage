package com.example.domain.usecase

import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.domain.exception.InsufficientStockException
import com.example.domain.repository.ShopRepository

class ProcessSaleUseCase(
    private val repository: ShopRepository
) {
    suspend operator fun invoke(sale: SaleEntity, items: List<SaleItemEntity>) {
        if (items.isEmpty()) {
            throw IllegalArgumentException("Cannot process a sale with zero items.")
        }
        
        for (item in items) {
            val product = repository.getProductById(item.productId)
                ?: throw IllegalArgumentException("Product with ID ${item.productId} not found.")
            
            if (product.stockQuantity < item.quantity) {
                throw InsufficientStockException(
                    productName = product.name,
                    availableStock = product.stockQuantity,
                    requestedQty = item.quantity
                )
            }
        }
        
        repository.executeSale(sale, items)
    }
}
