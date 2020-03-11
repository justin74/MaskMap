package com.justin.huang.maskmap.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.justin.huang.maskmap.data.DrugStore

@Dao
interface DrugStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugStores(properties: List<DrugStore>)
}