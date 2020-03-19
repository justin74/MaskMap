package com.justin.huang.maskmap.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Drugstore::class], version = 1, exportSchema = false)
abstract class MaskMapDb : RoomDatabase() {
    abstract fun drugstoreDao(): DrugstoreDao
}