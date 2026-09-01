package com.umrhsn.mmoire.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE boardId = :boardId")
    suspend fun getRecord(boardId: String): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)
}
