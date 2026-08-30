package com.umrhsn.mmoire.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val name: String,
    val imageUrlsJson: String // Storing as JSON string for simplicity, or we could use a separate table
)
