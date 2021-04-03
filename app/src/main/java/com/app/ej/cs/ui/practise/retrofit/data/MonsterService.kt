package com.app.ej.cs.ui.practise.retrofit.data

import com.app.ej.cs.ui.practise.retrofit.data.Monster
import retrofit2.Response
import retrofit2.http.GET

interface MonsterService {
    @GET("/feed/monster_data.json")
    suspend fun getMonsterData(): Response<List<Monster>>
}