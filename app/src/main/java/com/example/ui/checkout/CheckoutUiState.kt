package com.example.ui.checkout

import com.example.data.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val totalLinePrice: Double
        get() = product.sellingPrice * quantity
}

sealed interface CheckoutUiState {
    object Loading : CheckoutUiState
    
    data class Success(
        val products: List<ProductEntity>,
        val cart: List<CartItem>,
        val paymentMethod: String = "Cash",
        val error: String? = null,
        val showSaleConfirmed: Boolean = false,
        val lastConfirmedReceipt: CheckoutReceipt? = null
    ) : CheckoutUiState {
        val cartTotal: Double
            get() = cart.sumOf { it.totalLinePrice }
    }
    
    data class Error(val message: String) : CheckoutUiState
}

data class CheckoutReceipt(
    val saleId: Long = 0,
    val items: List<CartItem>,
    val total: Double,
    val paymentMethod: String,
    val timestamp: Long
)
