package com.justin.huang.maskmap.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface DrugStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugStores(drugStores: List<DrugStore>)
}