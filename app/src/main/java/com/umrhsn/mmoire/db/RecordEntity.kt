package com.umrhsn.mmoire.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey val boardId: String, // Game Name or BoardSize.name
    val bestTimeSeconds: Long,
    val bestMoves: Int
)
