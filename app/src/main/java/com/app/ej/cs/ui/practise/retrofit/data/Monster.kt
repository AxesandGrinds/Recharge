package com.app.ej.cs.ui.practise.retrofit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.ej.cs.ui.practise.retrofit.IMAGE_BASE_URL

@Entity(tableName = "monsters")
data class Monster (
    @PrimaryKey(autoGenerate = true)
    val monsterId: Int,
    val monsterName: String,
    val imageFile: String,
    val caption: String,
    val description: String,
    val price: Double,
    val scariness: Int
) {
    val imageUrl
        get() = "$IMAGE_BASE_URL/$imageFile.webp"
    val thumbnailUrl
        get() = "$IMAGE_BASE_URL/${imageFile}_tn.webp"
}
