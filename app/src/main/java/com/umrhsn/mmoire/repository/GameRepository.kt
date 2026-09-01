package com.umrhsn.mmoire.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umrhsn.mmoire.db.GameDao
import com.umrhsn.mmoire.db.GameEntity
import com.umrhsn.mmoire.db.RecordDao
import com.umrhsn.mmoire.db.RecordEntity
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.utils.LocalImageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val recordDao: RecordDao,
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

    suspend fun updateGame(oldName: String, newName: String, imageUrls: List<String>): Boolean {
        return try {
            val json = gson.toJson(imageUrls)
            if (oldName != newName) {
                gameDao.deleteGame(oldName)
                // Transfer records if name changed
                getRecord(oldName)?.let {
                    saveRecord(it.copy(boardId = newName))
                }
            }
            gameDao.insertGame(GameEntity(newName, json))
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
            UserImageListWithId(entity.name, images, entity.createdAt)
        }
    }

    suspend fun deleteGame(gameName: String) {
        imageManager.deleteGameImages(gameName)
        gameDao.deleteGame(gameName)
    }

    suspend fun getRecord(boardId: String): RecordEntity? {
        return recordDao.getRecord(boardId)
    }

    suspend fun saveRecord(record: RecordEntity) {
        recordDao.insertRecord(record)
    }
}

data class UserImageListWithId(
    val name: String,
    val images: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
