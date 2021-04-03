package com.app.ej.cs.ui.practise.retrofit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.ej.cs.ui.practise.retrofit.data.Monster

@Dao
interface MonsterDao {

    @Query("SELECT * from monsters")
    fun getAll(): List<Monster>

    @Insert
    suspend fun insertMonster(monster: Monster)

    @Insert
    suspend fun insertMonsters(monsters: List<Monster>)

    @Query("DELETE from monsters")
    suspend fun deleteAll()

}