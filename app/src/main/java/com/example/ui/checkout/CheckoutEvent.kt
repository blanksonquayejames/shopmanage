package com.example.ui.checkout

import com.example.data.entity.ProductEntity

sealed interface CheckoutEvent {
    data class AddToCart(val product: ProductEntity) : CheckoutEvent
    data class RemoveFromCart(val product: ProductEntity) : CheckoutEvent
    data class UpdateCartQuantity(val productId: Long, val quantity: Int) : CheckoutEvent
    data class SelectPaymentMethod(val method: String) : CheckoutEvent
    object ConfirmCheckout : CheckoutEvent
    object ClearCart : CheckoutEvent
    object DismissConfirmation : CheckoutEvent
    
    // Inventory and Shop Management
    data class QuickAddStock(val productId: Long, val amount: Int) : CheckoutEvent
    data class CreateProduct(
        val sku: String,
        val name: String,
        val sellingPrice: Double,
        val costPrice: Double,
        val stockQuantity: Int,
        val lowStockThreshold: Int
    ) : CheckoutEvent
    object ResetAndSeedData : CheckoutEvent
}
