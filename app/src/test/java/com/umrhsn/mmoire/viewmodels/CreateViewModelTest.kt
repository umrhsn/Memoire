package com.umrhsn.mmoire.viewmodels

import app.cash.turbine.test
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.repository.GameRepository
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CreateViewModelTest {

    private val repository: GameRepository = mock()
    private val soundManager: SoundManager = mock()
    private lateinit var viewModel: CreateViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateViewModel(repository, soundManager)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadGame updates state with image URIs`() = runTest(testDispatcher) {
        val gameName = "testGame"
        val images = listOf("file://img1", "file://img2")
        whenever(repository.getGame(gameName)).thenReturn(UserImageList(images))

        viewModel.uiState.test {
            awaitItem() // Initial state
            viewModel.loadGame(gameName)

            var state = awaitItem()
            // Skip until loading is finished or if it was so fast it skipped loading state
            while (state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(gameName, state.gameName)
            assertEquals(2, state.initialUris.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createGame success updates state`() = runTest(testDispatcher) {
        val gameName = "newGame"
        val imageBytes = listOf(byteArrayOf(1, 2))
        whenever(repository.checkGameExists(gameName)).thenReturn(false)
        whenever(repository.uploadImage(eq(gameName), any(), any())).thenReturn("url1")
        whenever(repository.createGame(eq(gameName), any())).thenReturn(true)

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.createGame(gameName, imageBytes)

            // Skip progress states
            var state = awaitItem()
            while (state.isUploading) {
                if (state.isSuccess) break
                state = awaitItem()
            }

            assertTrue(state.isSuccess)
            assertEquals(gameName, state.gameName)
            verify(soundManager).playSound(SoundManager.SoundType.SUCCESS_FANFARE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createGame with existing name sets nameTaken`() = runTest(testDispatcher) {
        val gameName = "existing"
        whenever(repository.checkGameExists(gameName)).thenReturn(true)

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.createGame(gameName, emptyList())

            var lastState = awaitItem()
            // We might get the combined result immediately or intermediate states
            while (!lastState.nameTaken) {
                lastState = awaitItem()
            }

            assertTrue(lastState.nameTaken)
            assertFalse(lastState.isUploading)
            verify(soundManager).playSound(SoundManager.SoundType.MATCH_FAIL)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
