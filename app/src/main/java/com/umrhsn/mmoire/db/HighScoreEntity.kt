package com.umrhsn.mmoire.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey val boardSizeValue: Int,
    val bestTimeSeconds: Long
)
