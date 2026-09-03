package com.umrhsn.mmoire.repository

import com.umrhsn.mmoire.db.GameDao
import com.umrhsn.mmoire.db.GameEntity
import com.umrhsn.mmoire.db.RecordDao
import com.umrhsn.mmoire.utils.LocalImageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryTest {

    private val gameDao: GameDao = mock()
    private val recordDao: RecordDao = mock()
    private val imageManager: LocalImageManager = mock()
    private lateinit var repository: GameRepository

    @BeforeEach
    fun setUp() {
        repository = GameRepository(gameDao, recordDao, imageManager)
    }

    @Test
    fun `getGame returns UserImageList when entity exists`() = runTest {
        val gameName = "testGame"
        val imageUrls = listOf("url1")
        val json = "[\"url1\"]"
        val entity = GameEntity(gameName, json)

        whenever(gameDao.getGame(gameName)).thenReturn(entity)

        val result = repository.getGame(gameName)

        assertNotNull(result)
        assertEquals(imageUrls, result?.images)
    }

    @Test
    fun `checkGameExists returns true when entity exists`() = runTest {
        val gameName = "existingGame"

        whenever(gameDao.exists(gameName)).thenReturn(true)

        val result = repository.checkGameExists(gameName)

        assertTrue(result)
    }

    @Test
    fun `createGame inserts game into dao`() = runTest {
        val gameName = "newGame"
        val imageUrls = listOf("url1", "url2")

        val result = repository.createGame(gameName, imageUrls)

        assertTrue(result)
        verify(gameDao).insertGame(any())
    }

    @Test
    fun `deleteGame deletes from dao and image manager`() = runTest {
        val gameName = "toDelete"

        repository.deleteGame(gameName)

        verify(gameDao).deleteGame(gameName)
        verify(imageManager).deleteGameImages(gameName)
    }

    @Test
    fun `getAllLocalGames returns mapped entities`() = runTest {
        val entities = listOf(GameEntity("game1", "[]"), GameEntity("game2", "[]"))
        whenever(gameDao.getAllGames()).thenReturn(entities)

        val result = repository.getAllLocalGames()

        assertEquals(2, result.size)
        assertEquals("game1", result[0].name)
        assertEquals("game2", result[1].name)
    }

    @Test
    fun `updateGame deletes old and inserts new when name changed`() = runTest {
        val oldName = "old"
        val newName = "new"
        val urls = listOf("url")

        repository.updateGame(oldName, newName, urls)

        verify(gameDao).deleteGame(oldName)
        verify(gameDao).insertGame(any())
    }
}
