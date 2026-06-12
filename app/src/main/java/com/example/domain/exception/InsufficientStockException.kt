package com.example.domain.exception

class InsufficientStockException(
    val productName: String,
    val availableStock: Int,
    val requestedQty: Int
) : Exception("Insufficient stock for '$productName'. Requested: $requestedQty, Available: $availableStock.")
