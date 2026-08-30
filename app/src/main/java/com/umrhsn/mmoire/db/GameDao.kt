package com.umrhsn.mmoire.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE name = :name")
    suspend fun getGame(name: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM games WHERE name = :name)")
    suspend fun exists(name: String): Boolean
    
    @Query("SELECT * FROM games")
    suspend fun getAllGames(): List<GameEntity>

    @Query("DELETE FROM games WHERE name = :name")
    suspend fun deleteGame(name: String)
}
