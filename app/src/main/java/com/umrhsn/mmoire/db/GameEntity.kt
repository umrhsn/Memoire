package com.umrhsn.mmoire.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val name: String,
    val imageUrlsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
