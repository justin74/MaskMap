package com.justin.huang.maskmap.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrugstoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugstores(drugstores: List<Drugstore>)

    @Query("SELECT * FROM Drugstore")
    fun getDrugstoreList(): LiveData<List<Drugstore>>

    @Query("SELECT * FROM Drugstore WHERE id =:id")
    fun getDrugstore(id: String): LiveData<Drugstore>
}