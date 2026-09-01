package com.umrhsn.mmoire.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class, RecordEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun recordDao(): RecordDao
}
