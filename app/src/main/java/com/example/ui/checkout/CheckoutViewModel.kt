package com.example.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.domain.exception.InsufficientStockException
import com.example.domain.repository.ShopRepository
import com.example.domain.usecase.ProcessSaleUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val repository: ShopRepository,
    private val processSaleUseCase: ProcessSaleUseCase
) : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    private val _paymentMethod = MutableStateFlow("Cash")
    private val _error = MutableStateFlow<String?>(null)
    private val _showSaleConfirmed = MutableStateFlow(false)
    private val _lastReceipt = MutableStateFlow<CheckoutReceipt?>(null)

    val uiState: StateFlow<CheckoutUiState> = combine(
        repository.getAllProducts(),
        _cart,
        _paymentMethod,
        _error,
        _showSaleConfirmed,
        _lastReceipt
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        CheckoutUiState.Success(
            products = flows[0] as List<ProductEntity>,
            cart = flows[1] as List<CartItem>,
            paymentMethod = flows[2] as String,
            error = flows[3] as String?,
            showSaleConfirmed = flows[4] as Boolean,
            lastConfirmedReceipt = flows[5] as CheckoutReceipt?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState.Loading
    )

    fun onEvent(event: CheckoutEvent) {
        viewModelScope.launch {
            when (event) {
                is CheckoutEvent.AddToCart -> {
                    val currentCart = _cart.value.toMutableList()
                    val existingIndex = currentCart.indexOfFirst { it.product.id == event.product.id }
                    if (existingIndex >= 0) {
                        val currentQty = currentCart[existingIndex].quantity
                        currentCart[existingIndex] = currentCart[existingIndex].copy(quantity = currentQty + 1)
                    } else {
                        currentCart.add(CartItem(event.product, 1))
                    }
                    _cart.value = currentCart
                    _error.value = null
                }
                is CheckoutEvent.RemoveFromCart -> {
                    val currentCart = _cart.value.toMutableList()
                    currentCart.removeAll { it.product.id == event.product.id }
                    _cart.value = currentCart
                }
                is CheckoutEvent.UpdateCartQuantity -> {
                    val currentCart = _cart.value.toMutableList()
                    val index = currentCart.indexOfFirst { it.product.id == event.productId }
                    if (index >= 0) {
                        if (event.quantity <= 0) {
                            currentCart.removeAt(index)
                        } else {
                            currentCart[index] = currentCart[index].copy(quantity = event.quantity)
                        }
                        _cart.value = currentCart
                    }
                }
                is CheckoutEvent.SelectPaymentMethod -> {
                    _paymentMethod.value = event.method
                }
                CheckoutEvent.ConfirmCheckout -> {
                    val cartItems = _cart.value
                    if (cartItems.isEmpty()) {
                        _error.value = "Your checkout cart is empty."
                        return@launch
                    }
                    val totalAmount = cartItems.sumOf { it.totalLinePrice }
                    val currentTimestamp = System.currentTimeMillis()
                    
                    val saleEntity = SaleEntity(
                        timestamp = currentTimestamp,
                        totalAmount = totalAmount,
                        paymentMethod = _paymentMethod.value
                    )
                    
                    val saleItemEntities = cartItems.map { item ->
                        SaleItemEntity(
                            saleId = 0,
                            productId = item.product.id,
                            quantity = item.quantity,
                            priceAtSale = item.product.sellingPrice
                        )
                    }

                    try {
                        processSaleUseCase(saleEntity, saleItemEntities)
                        
                        _lastReceipt.value = CheckoutReceipt(
                            items = cartItems,
                            total = totalAmount,
                            paymentMethod = _paymentMethod.value,
                            timestamp = currentTimestamp
                        )
                        _cart.value = emptyList()
                        _error.value = null
                        _showSaleConfirmed.value = true
                    } catch (e: InsufficientStockException) {
                        _error.value = e.message
                    } catch (e: Exception) {
                        _error.value = "Transaction failed: ${e.localizedMessage ?: "Unknown error"}"
                    }
                }
                CheckoutEvent.ClearCart -> {
                    _cart.value = emptyList()
                    _error.value = null
                }
                CheckoutEvent.DismissConfirmation -> {
                    _showSaleConfirmed.value = false
                    _lastReceipt.value = null
                }
                is CheckoutEvent.QuickAddStock -> {
                    repository.addStock(event.productId, event.amount)
                }
                is CheckoutEvent.CreateProduct -> {
                    repository.insertProduct(
                        ProductEntity(
                            sku = event.sku,
                            name = event.name,
                            sellingPrice = event.sellingPrice,
                            costPrice = event.costPrice,
                            stockQuantity = event.stockQuantity,
                            lowStockThreshold = event.lowStockThreshold
                        )
                    )
                }
                CheckoutEvent.ResetAndSeedData -> {
                    seedDatabase()
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        val sampleProducts = listOf(
            ProductEntity(sku = "COF-001", name = "Premium Coffee Beans (1kg)", sellingPrice = 24.99, costPrice = 12.50, stockQuantity = 35, lowStockThreshold = 10),
            ProductEntity(sku = "TEA-002", name = "Organic Green Tea (50 bags)", sellingPrice = 8.50, costPrice = 3.20, stockQuantity = 20, lowStockThreshold = 5),
            ProductEntity(sku = "BAK-003", name = "Artisan Sourdough Bread", sellingPrice = 6.00, costPrice = 1.80, stockQuantity = 12, lowStockThreshold = 4),
            ProductEntity(sku = "MIL-004", name = "Organic Whole Milk (2L)", sellingPrice = 4.50, costPrice = 1.90, stockQuantity = 4, lowStockThreshold = 5),
            ProductEntity(sku = "APP-005", name = "Fuji Apples (Bag of 6)", sellingPrice = 5.99, costPrice = 2.00, stockQuantity = 2, lowStockThreshold = 4)
        )
        for (prod in sampleProducts) {
            repository.insertProduct(prod)
        }
    }
}

class CheckoutViewModelFactory(
    private val repository: ShopRepository,
    private val processSaleUseCase: ProcessSaleUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(repository, processSaleUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class.")
    }
}
