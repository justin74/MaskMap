package com.justin.huang.maskmap.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrugStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugStores(drugStores: List<DrugStore>)

    @Query("SELECT * FROM Drugstore")
    fun getDrugStoreList(): LiveData<List<DrugStore>>

    @Query("SELECT * FROM DrugStore WHERE id =:id")
    fun getDrugStore(id: String): LiveData<DrugStore>
}