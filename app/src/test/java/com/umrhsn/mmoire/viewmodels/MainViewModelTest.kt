package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.PrefsManager
import com.umrhsn.mmoire.utils.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val repository: GameRepository = mock()
    private val soundManager: SoundManager = mock()
    private val prefs: PrefsManager = mock()
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        whenever(prefs.getLanguage()).thenReturn("en")
        whenever(prefs.isFirstTime()).thenReturn(false)
        whenever(repository.getRecord(any())).thenReturn(null)
        viewModel = MainViewModel(repository, soundManager, prefs)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui state is correct`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(BoardSize.SUPER_DUPER_EASY, state.boardSize)
            assertNotNull(state.memoryGame)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changeSize updates state`() = runTest(testDispatcher) {
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

            // Skip Loading state if it was emitted separately
            var lastState = awaitItem()
            while (lastState.isLoading) {
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
            while (lastState.isLoading) {
                lastState = awaitItem()
            }

            assertFalse(lastState.isLoading)
            assertNotNull(lastState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
