package com.umrhsn.mmoire.viewmodels

import android.content.ContentResolver
import android.net.Uri
import app.cash.turbine.test
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.PrefsManager
import com.umrhsn.mmoire.utils.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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
    private val prefs: PrefsManager = mock()
    private val contentResolver: ContentResolver = mock()
    private lateinit var viewModel: CreateViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        whenever(prefs.getTheme()).thenReturn(AppTheme.SYSTEM)
        whenever(prefs.getColorTheme()).thenReturn(AppColorTheme.DEFAULT)
        whenever(prefs.isBackgroundTintEnabled()).thenReturn(true)
        whenever(prefs.isHighVisibilityModeEnabled()).thenReturn(false)
        whenever(prefs.getLanguage()).thenReturn("en")
        whenever(prefs.themeFlow).thenReturn(MutableStateFlow(AppTheme.SYSTEM))
        whenever(prefs.colorThemeFlow).thenReturn(MutableStateFlow(AppColorTheme.DEFAULT))
        whenever(prefs.tintFlow).thenReturn(MutableStateFlow(true))
        whenever(prefs.localeFlow).thenReturn(MutableStateFlow("en"))
        whenever(prefs.highVisibilityModeFlow).thenReturn(MutableStateFlow(false))

        viewModel = CreateViewModel(repository, soundManager, prefs)
    }

    @After
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
    fun `loadGame with non-existent game sets error state`() = runTest(testDispatcher) {
        val gameName = "missingGame"
        whenever(repository.getGame(gameName)).thenReturn(null)

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.loadGame(gameName)

            var lastState = awaitItem()
            while (lastState.isLoading) {
                lastState = awaitItem()
            }

            assertEquals(com.umrhsn.mmoire.R.string.game_not_found, lastState.errorMessage)
            assertEquals(gameName, lastState.errorArg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadGame with empty images list sets error state`() = runTest(testDispatcher) {
        val gameName = "emptyGame"
        whenever(repository.getGame(gameName)).thenReturn(UserImageList(emptyList()))

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.loadGame(gameName)

            var lastState = awaitItem()
            while (lastState.isLoading) {
                lastState = awaitItem()
            }

            assertEquals(com.umrhsn.mmoire.R.string.game_not_found, lastState.errorMessage)
            assertEquals(gameName, lastState.errorArg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createGame success updates state`() = runTest(testDispatcher) {
        val gameName = "newGame"
        val uris = listOf(Uri.parse("content://media/external/images/media/1"))

        whenever(repository.checkGameExists(gameName)).thenReturn(false)
        whenever(repository.uploadImage(eq(gameName), any(), any())).thenReturn("url1")
        whenever(repository.createGame(eq(gameName), any())).thenReturn(true)

        viewModel.uiState.test {
            awaitItem() // Initial
            viewModel.createGame(contentResolver, gameName, uris)

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
            viewModel.createGame(contentResolver, gameName, emptyList())

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
