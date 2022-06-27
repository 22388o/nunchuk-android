package com.nunchuk.android.core.di

import com.nunchuk.android.core.data.api.PriceConverterAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Singleton
    @Provides
    fun providePriceConverterAPI(retrofit: Retrofit): PriceConverterAPI = retrofit.create(
        PriceConverterAPI::class.java)

}