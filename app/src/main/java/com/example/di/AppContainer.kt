package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.ShopDatabase
import com.example.data.repository.ShopRepositoryImpl
import com.example.domain.repository.ShopRepository
import com.example.domain.usecase.ProcessSaleUseCase

interface AppContainer {
    val shopRepository: ShopRepository
    val processSaleUseCase: ProcessSaleUseCase
}

class AppContainerImpl(private val context: Context) : AppContainer {
    
    private val database: ShopDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            ShopDatabase::class.java,
            "shop_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    override val shopRepository: ShopRepository by lazy {
        ShopRepositoryImpl(
            productDao = database.productDao(),
            saleDao = database.saleDao()
        )
    }

    override val processSaleUseCase: ProcessSaleUseCase by lazy {
        ProcessSaleUseCase(shopRepository)
    }
}
