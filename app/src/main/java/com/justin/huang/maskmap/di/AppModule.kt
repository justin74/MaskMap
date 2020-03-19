package com.justin.huang.maskmap.di

import android.app.Application
import androidx.room.Room
import com.justin.huang.maskmap.api.MaskApiService
import com.justin.huang.maskmap.db.MaskMapDb
import com.justin.huang.maskmap.db.DrugstoreDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
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
    fun provideMaskApiService(moshi: Moshi): MaskApiService {
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/kiang/pharmacies/master/json/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MaskApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): MaskMapDb {
        return Room.databaseBuilder(app, MaskMapDb::class.java, "maskMap.db")
            .build()
    }

    @Singleton
    @Provides
    fun provideDrugstoreDao(db: MaskMapDb): DrugstoreDao {
        return db.drugstoreDao()
    }
}