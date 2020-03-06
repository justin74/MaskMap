package com.justin.huang.maskmap.di

import com.justin.huang.maskmap.api.MaskService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideMaskService(moshi: Moshi): MaskService {
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/kiang/pharmacies/master/json/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MaskService::class.java)
    }
}