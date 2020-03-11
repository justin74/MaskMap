package com.justin.huang.maskmap.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.justin.huang.maskmap.data.DrugStore

@Database(entities = [DrugStore::class], version = 1, exportSchema = false)
abstract class MaskMapDb : RoomDatabase() {
    abstract fun drugStoreDao(): DrugStoreDao
}