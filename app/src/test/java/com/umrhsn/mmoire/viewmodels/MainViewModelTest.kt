package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val repository: GameRepository = mock()
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui state is correct`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(BoardSize.SUPER_DUPER_EASY, initialState.boardSize)
            assertNotNull(initialState.memoryGame)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changeSize updates state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Skip initial state
            viewModel.changeSize(BoardSize.HARD)
            val updatedState = awaitItem()
            assertEquals(BoardSize.HARD, updatedState.boardSize)
            assertEquals(BoardSize.HARD.numCards, updatedState.memoryGame?.cards?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadGame success updates state`() = runTest(testDispatcher) {
        val gameName = "testGame"
        val customImages = listOf("url1", "url2", "url3") // 3 images = 6 cards = SUPER_DUPER_EASY
        whenever(repository.getGame(gameName)).thenReturn(UserImageList(customImages))

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.downloadGame(gameName)
            
            // It might emit loading then success, or just success if conflated.
            // With Unconfined, we should see both if they are distinct emissions.
            var lastState = awaitItem()
            if (lastState.isLoading) {
                lastState = awaitItem()
            }
            
            assertFalse(lastState.isLoading)
            assertEquals(gameName, lastState.gameName)
            assertEquals(customImages, lastState.customImages)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadGame failure sets error message`() = runTest(testDispatcher) {
        val gameName = "unknownGame"
        whenever(repository.getGame(gameName)).thenReturn(null)

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.downloadGame(gameName)
            
            var lastState = awaitItem()
            if (lastState.isLoading) {
                lastState = awaitItem()
            }
            
            assertFalse(lastState.isLoading)
            assertNotNull(lastState.errorMessage)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
