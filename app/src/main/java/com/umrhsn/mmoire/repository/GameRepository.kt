package com.umrhsn.mmoire.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umrhsn.mmoire.db.GameDao
import com.umrhsn.mmoire.db.GameEntity
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.utils.LocalImageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val imageManager: LocalImageManager
) {
    private val gson = Gson()

    suspend fun getGame(gameName: String): UserImageList? {
        val entity = gameDao.getGame(gameName) ?: return null
        val type = object : TypeToken<List<String>>() {}.type
        val images: List<String> = gson.fromJson(entity.imageUrlsJson, type)
        return UserImageList(images)
    }

    suspend fun checkGameExists(gameName: String): Boolean {
        return gameDao.exists(gameName)
    }

    suspend fun uploadImage(gameName: String, imageIndex: Int, imageByteArray: ByteArray): String {
        // In local mode, we "upload" by saving to internal storage
        return imageManager.saveImage(gameName, imageIndex, imageByteArray)
    }

    suspend fun createGame(gameName: String, imageUrls: List<String>): Boolean {
        return try {
            val json = gson.toJson(imageUrls)
            gameDao.insertGame(GameEntity(gameName, json))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getAllLocalGames(): List<UserImageListWithId> {
        val entities = gameDao.getAllGames()
        val type = object : TypeToken<List<String>>() {}.type
        return entities.map { entity ->
            val images: List<String> = gson.fromJson(entity.imageUrlsJson, type)
            UserImageListWithId(entity.name, images)
        }
    }

    suspend fun deleteGame(gameName: String) {
        // Optional: logic to delete files from storage too
        // imageManager.deleteGameFolder(gameName)
        gameDao.deleteGame(gameName)
    }
}

data class UserImageListWithId(val name: String, val images: List<String>)
